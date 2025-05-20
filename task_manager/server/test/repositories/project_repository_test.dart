import 'package:test/test.dart';
import 'package:shared/src/models/project.dart' as shared;
import 'package:shared/src/models/user.dart' as shared;
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../helpers/test_base.dart';

void main() {
  late TestBase testBase;
  late ProjectRepository repository;
  late shared.User testUser;
  late shared.User testMember;

  setUp(() async {
    testBase = TestBase();
    await testBase.setUp();
    repository = ProjectRepository(testBase.connection);

    // Create test users
    final authRepo = AuthRepository(testBase.connection);
    testUser = shared.User(
      id: '1',
      name: 'Test User',
      email: 'test@example.com',
      passwordHash: 'hashed_password',
    );
    testMember = shared.User(
      id: '2',
      name: 'Test Member',
      email: 'member@example.com',
      passwordHash: 'hashed_password',
    );
    await authRepo.createUser(testUser);
    await authRepo.createUser(testMember);
  });

  tearDown(() async {
    await testBase.clearTables();
    await testBase.tearDown();
  });

  group('ProjectRepository', () {
    test('should create a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );

      final createdProject = await repository.createProject(project);
      expect(createdProject.id, equals(project.id));
      expect(createdProject.name, equals(project.name));
      expect(createdProject.description, equals(project.description));
      expect(createdProject.creatorId, equals(project.creatorId));
      expect(createdProject.memberIds, equals(project.memberIds));
    });

    test('should find a project by id', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );

      await repository.createProject(project);
      final foundProject = await repository.findProjectById(project.id);
      expect(foundProject, isNotNull);
      expect(foundProject!.id, equals(project.id));
      expect(foundProject.name, equals(project.name));
      expect(foundProject.description, equals(project.description));
      expect(foundProject.creatorId, equals(project.creatorId));
      expect(foundProject.memberIds, equals(project.memberIds));
    });

    test('should find projects by creator id', () async {
      final project1 = shared.Project(
        id: '1',
        name: 'Project 1',
        description: 'Description 1',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );
      final project2 = shared.Project(
        id: '2',
        name: 'Project 2',
        description: 'Description 2',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );

      await repository.createProject(project1);
      await repository.createProject(project2);

      final projects = await repository.findProjectsByCreatorId(testUser.id);
      expect(projects.length, equals(2));
      expect(
          projects.map((p) => p.id), containsAll([project1.id, project2.id]));
    });

    test('should find projects by member id', () async {
      final project1 = shared.Project(
        id: '1',
        name: 'Project 1',
        description: 'Description 1',
        creatorId: testUser.id,
        memberIds: [testUser.id, testMember.id],
      );
      final project2 = shared.Project(
        id: '2',
        name: 'Project 2',
        description: 'Description 2',
        creatorId: testUser.id,
        memberIds: [testUser.id, testMember.id],
      );

      await repository.createProject(project1);
      await repository.createProject(project2);

      final projects = await repository.findProjectsByMemberId(testMember.id);
      expect(projects.length, equals(2));
      expect(
          projects.map((p) => p.id), containsAll([project1.id, project2.id]));
    });

    test('should update a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );

      await repository.createProject(project);

      final updatedProject = shared.Project(
        id: project.id,
        name: 'Updated Project',
        description: 'Updated Description',
        creatorId: project.creatorId,
        memberIds: [testUser.id, testMember.id],
      );

      await repository.updateProject(updatedProject);
      final foundProject = await repository.findProjectById(project.id);
      expect(foundProject, isNotNull);
      expect(foundProject!.name, equals(updatedProject.name));
      expect(foundProject.description, equals(updatedProject.description));
      expect(foundProject.memberIds, equals(updatedProject.memberIds));
    });

    test('should delete a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );

      await repository.createProject(project);
      await repository.deleteProject(project.id);
      final foundProject = await repository.findProjectById(project.id);
      expect(foundProject, isNull);
    });

    test('should add a member to a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: testUser.id,
        memberIds: [testUser.id],
      );

      await repository.createProject(project);
      await repository.addMember(project.id, testMember.id);

      final updatedProject = await repository.findProjectById(project.id);
      expect(updatedProject, isNotNull);
      expect(updatedProject!.memberIds, contains(testMember.id));
    });

    test('should remove a member from a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: testUser.id,
        memberIds: [testUser.id, testMember.id],
      );

      await repository.createProject(project);
      await repository.removeMember(project.id, testMember.id);

      final updatedProject = await repository.findProjectById(project.id);
      expect(updatedProject, isNotNull);
      expect(updatedProject!.memberIds, isNot(contains(testMember.id)));
    });
  });
}
