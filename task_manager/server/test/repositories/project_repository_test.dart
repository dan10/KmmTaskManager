import 'package:test/test.dart';
import 'package:shared/models.dart';
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../helpers/test_base.dart';

void main() {
  late TestBase testBase;
  late ProjectRepository repository;
  late AuthRepository authRepository;
  late User testUser1;
  late User testUser2;

  setUpAll(() async {
    testBase = TestBase();
    await testBase.setUp();
    repository = ProjectRepositoryImpl(testBase.connection);
    authRepository = AuthRepository(testBase.connection);

    // Create test users
    testUser1 = User(
      id: 'user-1',
      displayName: 'Test User 1',
      email: 'user1@test.com',
      googleId: 'google-1',
      createdAt: DateTime.now().toIso8601String(),
    );
    testUser2 = User(
      id: 'user-2',
      displayName: 'Test User 2',
      email: 'user2@test.com',
      googleId: 'google-2',
      createdAt: DateTime.now().toIso8601String(),
    );

    await authRepository.createUser(testUser1);
    await authRepository.createUser(testUser2);
  });

  tearDownAll(() async {
    await testBase.tearDown();
  });

  setUp(() async {
    // Clear projects and project_members before each test
    await testBase.connection.execute('DELETE FROM project_members');
    await testBase.connection.execute('DELETE FROM projects');
  });

  group('ProjectRepository Integration Tests', () {
    test('create should create a project with creator as member', () async {
      final project = Project(
        id: 'project-1',
        name: 'Test Project',
        description: 'A test project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id],
      );

      final result = await repository.create(project);
      
      expect(result.id, equals(project.id));
      expect(result.name, equals(project.name));
      expect(result.description, equals(project.description));
      expect(result.creatorId, equals(testUser1.id));
      expect(result.memberIds, contains(testUser1.id));
    });

    test('findById should return project when it exists', () async {
      final project = Project(
        id: 'project-1',
        name: 'Test Project',
        description: 'A test project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id],
      );

      await repository.create(project);
      final result = await repository.findById('project-1');

      expect(result, isNotNull);
      expect(result!.id, equals('project-1'));
      expect(result.name, equals('Test Project'));
      expect(result.creatorId, equals(testUser1.id));
    });

    test('findById should return null when project does not exist', () async {
      final result = await repository.findById('non-existent');
      expect(result, isNull);
    });

    test('update should modify project details', () async {
      final project = Project(
        id: 'project-1',
        name: 'Test Project',
        description: 'A test project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id],
      );

      await repository.create(project);

      final updatedProject = project.copyWith(
        name: 'Updated Project',
        description: 'Updated description',
        memberIds: [testUser1.id, testUser2.id],
      );

      final result = await repository.update(updatedProject);

      expect(result.name, equals('Updated Project'));
      expect(result.description, equals('Updated description'));
      expect(result.memberIds, containsAll([testUser1.id, testUser2.id]));
    });

    test('delete should remove project and its memberships', () async {
      final project = Project(
        id: 'project-1',
        name: 'Test Project',
        description: 'A test project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id, testUser2.id],
      );

      await repository.create(project);
      final success = await repository.delete('project-1');

      expect(success, isTrue);

      final result = await repository.findById('project-1');
      expect(result, isNull);

      // Verify memberships are also deleted
      final membershipsResult = await testBase.connection.query(
        'SELECT * FROM project_members WHERE project_id = @id',
        substitutionValues: {'id': 'project-1'},
      );
      expect(membershipsResult, isEmpty);
    });

    test('getProjects should filter by creator', () async {
      final project1 = Project(
        id: 'project-1',
        name: 'User1 Project',
        description: 'Created by user1',
        creatorId: testUser1.id,
        memberIds: [testUser1.id],
      );

      final project2 = Project(
        id: 'project-2',
        name: 'User2 Project',
        description: 'Created by user2',
        creatorId: testUser2.id,
        memberIds: [testUser2.id],
      );

      await repository.create(project1);
      await repository.create(project2);

      final results = await repository.getProjects(
        creatorId: testUser1.id,
        page: 0,
        size: 10,
      );

      expect(results, hasLength(1));
      expect(results.first.id, equals('project-1'));
      expect(results.first.creatorId, equals(testUser1.id));
    });

    test('getProjects should support text search', () async {
      final project1 = Project(
        id: 'project-1',
        name: 'Alpha Project',
        description: 'First project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id],
      );

      final project2 = Project(
        id: 'project-2',
        name: 'Beta Project',
        description: 'Second project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id],
      );

      await repository.create(project1);
      await repository.create(project2);

      final results = await repository.getProjects(
        creatorId: testUser1.id,
        query: 'Alpha',
        page: 0,
        size: 10,
      );

      expect(results, hasLength(1));
      expect(results.first.name, equals('Alpha Project'));
    });

    test('getProjects should support pagination', () async {
      // Create multiple projects
      for (int i = 1; i <= 5; i++) {
        final project = Project(
          id: 'project-$i',
          name: 'Project $i',
          description: 'Description $i',
          creatorId: testUser1.id,
          memberIds: [testUser1.id],
        );
        await repository.create(project);
      }

      final page1 = await repository.getProjects(
        creatorId: testUser1.id,
        page: 0,
        size: 2,
      );

      final page2 = await repository.getProjects(
        creatorId: testUser1.id,
        page: 1,
        size: 2,
      );

      expect(page1, hasLength(2));
      expect(page2, hasLength(2));
      
      // Ensure different results
      final page1Ids = page1.map((p) => p.id).toSet();
      final page2Ids = page2.map((p) => p.id).toSet();
      expect(page1Ids.intersection(page2Ids), isEmpty);
    });

    test('findByMemberId should return projects where user is a member', () async {
      final project1 = Project(
        id: 'project-1',
        name: 'Project 1',
        description: 'First project',
        creatorId: testUser1.id,
        memberIds: [testUser1.id, testUser2.id],
      );

      final project2 = Project(
        id: 'project-2',
        name: 'Project 2',
        description: 'Second project',
        creatorId: testUser2.id,
        memberIds: [testUser2.id], // Only user2 is member
      );

      await repository.create(project1);
      await repository.create(project2);

      final results = await repository.findByMemberId(testUser2.id);

      expect(results, hasLength(2)); // user2 is member of both projects
      expect(results.map((p) => p.id), containsAll(['project-1', 'project-2']));
    });
  });
} 