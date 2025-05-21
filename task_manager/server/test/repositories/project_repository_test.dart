import 'package:test/test.dart';
import 'package:shared/models.dart' as shared_models;
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/exceptions/custom_exceptions.dart';
import '../helpers/test_base.dart';

void main() {
  late TestBase testBase;
  late ProjectRepository repository;
  late AuthRepository authRepository; // To create users
  late shared_models.User user1;
  late shared_models.User user2;
  late shared_models.User user3;


  setUpAll(() async { // Changed to setUpAll for one-time user creation
    testBase = TestBase();
    await testBase.setUp();
    repository = ProjectRepository(testBase.connection);
    authRepository = AuthRepository(testBase.connection);

    // Create test users
    user1 = shared_models.User(id: 'user-id-1', name: 'User One', email: 'user1@example.com', passwordHash: 'hash1');
    user2 = shared_models.User(id: 'user-id-2', name: 'User Two', email: 'user2@example.com', passwordHash: 'hash2');
    user3 = shared_models.User(id: 'user-id-3', name: 'User Three', email: 'user3@example.com', passwordHash: 'hash3');
    await authRepository.createUser(user1);
    await authRepository.createUser(user2);
    await authRepository.createUser(user3);
  });

  tearDownAll(() async { // Changed to tearDownAll
    // Tables are dropped by TestBase's main tearDown, but clearTables can be called if needed earlier
    await testBase.tearDown();
  });
  
  // Clear projects and project_members before each test in the group
  setUp(() async {
    await testBase.connection.execute('DELETE FROM project_members');
    await testBase.connection.execute('DELETE FROM projects');
  });


  group('ProjectRepository', () {
    final projectData1 = shared_models.Project(
      id: 'proj-id-1',
      name: 'Alpha Project',
      description: 'First test project',
      creatorId: user1.id,
      memberIds: [user1.id], // Creator is a member by default in create method
    );
     final projectData2 = shared_models.Project(
      id: 'proj-id-2',
      name: 'Beta Project',
      description: 'Second test project, user1 creator',
      creatorId: user1.id,
      memberIds: [user1.id],
    );
     final projectData3 = shared_models.Project(
      id: 'proj-id-3',
      name: 'Gamma Project',
      description: 'Third test project, user2 creator',
      creatorId: user2.id,
      memberIds: [user2.id],
    );


    test('create should create a project with initial member (creator)', () async {
      final createdProject = await repository.create(projectData1);
      expect(createdProject.name, equals(projectData1.name));
      expect(createdProject.creatorId, equals(user1.id));
      
      final foundProject = await repository.findById(createdProject.id);
      expect(foundProject, isNotNull);
      expect(foundProject!.memberIds, contains(user1.id));
    });

    test('findById should find a project by id', () async {
      await repository.create(projectData1);
      final foundProject = await repository.findById(projectData1.id);
      expect(foundProject, isNotNull);
      expect(foundProject!.name, equals(projectData1.name));
    });

    test('update should update a project details and members', () async {
      final initialProject = await repository.create(projectData1);
      final updatedData = initialProject.copyWith(
        name: 'Updated Alpha Name',
        description: 'Updated description.',
        memberIds: [user1.id, user2.id], // Add user2 as member
      );

      final result = await repository.update(updatedData);
      expect(result.name, 'Updated Alpha Name');
      expect(result.description, 'Updated description.');
      
      final foundAfterUpdate = await repository.findById(initialProject.id);
      expect(foundAfterUpdate!.memberIds, containsAll([user1.id, user2.id]));
      expect(foundAfterUpdate.memberIds.length, 2);
    });
    
    test('update should throw ProjectNotFoundException if project does not exist', () async {
      final nonExistentProject = projectData1.copyWith(id: 'fake-id');
      expect(
        () => repository.update(nonExistentProject),
        throwsA(isA<ProjectNotFoundException>()),
      );
    });


    test('delete should remove a project and its memberships', () async {
      final project = await repository.create(projectData1);
      await repository.assignUserToProject(project.id, user2.id); // Add another member

      await repository.delete(project.id);
      
      final foundProject = await repository.findById(project.id);
      expect(foundProject, isNull);

      // Check memberships are gone
      final membersResult = await testBase.connection.query(
        'SELECT * FROM project_members WHERE project_id = @id',
        substitutionValues: {'id': project.id},
      );
      expect(membersResult, isEmpty);
    });

    group('assignUserToProject', () {
      late shared_models.Project project;
      setUp(() async {
        project = await repository.create(projectData1);
      });

      test('should assign a user to a project', () async {
        await repository.assignUserToProject(project.id, user2.id);
        final updatedProject = await repository.findById(project.id);
        expect(updatedProject!.memberIds, containsAll([user1.id, user2.id]));
      });

      test('should throw ProjectNotFoundException if project does not exist', () {
        expect(
          () => repository.assignUserToProject('fake-proj-id', user2.id),
          throwsA(isA<ProjectNotFoundException>()),
        );
      });

      test('should throw UserNotFoundException if user does not exist', () {
        expect(
          () => repository.assignUserToProject(project.id, 'fake-user-id'),
          throwsA(isA<UserNotFoundException>()),
        );
      });

      test('should throw AlreadyAssignedException if user is already assigned', () async {
        await repository.assignUserToProject(project.id, user2.id); // First assignment
        expect(
          () => repository.assignUserToProject(project.id, user2.id), // Second assignment
          throwsA(isA<AlreadyAssignedException>()),
        );
      });
    });

    group('removeUserFromProject', () {
       late shared_models.Project project;
      setUp(() async {
        project = await repository.create(projectData1);
        await repository.assignUserToProject(project.id, user2.id); // Ensure user2 is a member
      });

      test('should remove a user from a project', async () {
        final success = await repository.removeUserFromProject(project.id, user2.id);
        expect(success, isTrue);
        final updatedProject = await repository.findById(project.id);
        expect(updatedProject!.memberIds, isNot(contains(user2.id)));
        expect(updatedProject.memberIds, contains(user1.id)); // Creator should still be there
      });
      
      test('should return false if user is not a member (or project/user not found)', async () {
        // User3 is not a member
        final success = await repository.removeUserFromProject(project.id, user3.id);
        expect(success, isFalse);
      });
       test('should return false if trying to remove from non-existent project', async () {
        final success = await repository.removeUserFromProject('fake-proj-id', user2.id);
        expect(success, isFalse);
      });
    });
    
    group('getAllProjects (user-specific, paginated, searchable)', () {
      setUp(() async {
        await repository.create(projectData1); // Creator user1
        await repository.create(projectData2); // Creator user1
        final p3 = await repository.create(projectData3); // Creator user2
        await repository.assignUserToProject(p3.id, user1.id); // Add user1 as member to p3
      });

      test('should return projects created by or member of for user1', async () {
        final projects = await repository.getAllProjects(user1.id, 0, 10, null);
        expect(projects.length, 3);
        expect(projects.map((p) => p.id), containsAll([projectData1.id, projectData2.id, projectData3.id]));
      });
      
      test('should return only projects created by user2', async () {
        final projects = await repository.getAllProjects(user2.id, 0, 10, null);
        expect(projects.length, 1);
        expect(projects.first.id, projectData3.id);
      });

      test('should apply pagination: page 0, size 1 for user1', async () {
        final projects = await repository.getAllProjects(user1.id, 0, 1, null);
        expect(projects.length, 1);
        // Order is by name: Alpha, Beta, Gamma
        expect(projects.first.name, 'Alpha Project');
      });
      
      test('should apply pagination: page 1, size 1 for user1', async () {
        final projects = await repository.getAllProjects(user1.id, 1, 1, null);
        expect(projects.length, 1);
        expect(projects.first.name, 'Beta Project');
      });
      
      test('should apply search query for user1', async () {
        final projects = await repository.getAllProjects(user1.id, 0, 10, 'Gamma');
        expect(projects.length, 1);
        expect(projects.first.id, projectData3.id);
      });
    });

    group('getAllSystemProjects (paginated, searchable)', () {
       setUp(() async {
        await repository.create(projectData1); // Alpha
        await repository.create(projectData2); // Beta
        await repository.create(projectData3); // Gamma
      });
      test('should return all projects with pagination', async () {
        final projects = await repository.getAllSystemProjects(0, 2, null);
        expect(projects.length, 2);
        expect(projects[0].name, 'Alpha Project'); // Order is by name
        expect(projects[1].name, 'Beta Project');
      });
      test('should return all projects matching search query', async () {
        final projects = await repository.getAllSystemProjects(0, 10, 'Gamma');
        expect(projects.length, 1);
        expect(projects.first.id, projectData3.id);
      });
    });

    group('getUsersByProject', () {
      late shared_models.Project project;
      setUp(() async {
        project = await repository.create(projectData1); // user1 is creator
        await repository.assignUserToProject(project.id, user2.id);
      });
      test('should return users assigned to a project', async () {
        final users = await repository.getUsersByProject(project.id);
        expect(users.length, 2);
        expect(users.map((u) => u.id), containsAll([user1.id, user2.id]));
      });
      test('should throw ProjectNotFoundException if project does not exist', () async {
        expect(
          () => repository.getUsersByProject('fake-project-id'),
          throwsA(isA<ProjectNotFoundException>()),
        );
      });
    });

    group('getProjectsByUser', () {
       setUp(() async {
        await repository.create(projectData1); // user1 creator
        await repository.create(projectData2); // user1 creator
        final p3 = await repository.create(projectData3); // user2 creator
        await repository.assignUserToProject(p3.id, user1.id); // user1 member of p3
      });
      test('should return projects where user is creator or member', async () {
        final projects = await repository.getProjectsByUser(user1.id);
        expect(projects.length, 3);
        expect(projects.map((p) => p.id), containsAll([projectData1.id, projectData2.id, projectData3.id]));
      });
      test('should throw UserNotFoundException if user does not exist', () async {
        expect(
          () => repository.getProjectsByUser('fake-user-id'),
          throwsA(isA<UserNotFoundException>()),
        );
      });
    });

  });
}
