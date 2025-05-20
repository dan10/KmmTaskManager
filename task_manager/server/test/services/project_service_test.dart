import 'package:test/test.dart';
import 'package:shared/src/models/project.dart' as shared;
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/services/project_service.dart';
import '../helpers/test_base.dart';

void main() {
  late ProjectServiceImpl projectService;
  late ProjectRepository projectRepository;
  late TestBase testBase;
  late String testUserId;

  setUp(() async {
    testBase = TestBase();
    await testBase.setUp();
    projectRepository = ProjectRepository(testBase.connection);
    projectService = ProjectServiceImpl(projectRepository);

    // Create a test user in the database
    await testBase.connection.execute(
      'INSERT INTO users (id, email, password_hash, name) VALUES (@id, @email, @passwordHash, @name)',
      substitutionValues: {
        'id': 'test_user_id',
        'email': 'test@example.com',
        'passwordHash': 'hashed_password',
        'name': 'Test User',
      },
    );
    testUserId = 'test_user_id';
  });

  tearDown(() async {
    await testBase.clearTables();
    await testBase.tearDown();
  });

  group('ProjectService', () {
    final testProject = shared.Project(
      id: '1',
      name: 'Test Project',
      description: 'Test Description',
      creatorId: 'test_user_id',
      memberIds: ['test_user_id'],
    );

    group('createProject', () {
      test('should create a new project successfully', () async {
        final result = await projectService.createProject(testProject);

        expect(result.id, equals(testProject.id));
        expect(result.name, equals(testProject.name));
        expect(result.description, equals(testProject.description));
        expect(result.creatorId, equals(testProject.creatorId));
        expect(result.memberIds, containsAll(testProject.memberIds));
      });
    });

    group('getProjectById', () {
      test('should return project when found and user is authorized', () async {
        // First create a project
        await projectService.createProject(testProject);

        final result =
            await projectService.getProjectById(testProject.id, testUserId);

        expect(result, isNotNull);
        expect(result?.id, equals(testProject.id));
        expect(result?.name, equals(testProject.name));
        expect(result?.description, equals(testProject.description));
        expect(result?.creatorId, equals(testProject.creatorId));
        expect(result?.memberIds, containsAll(testProject.memberIds));
      });

      test('should return null when project not found', () async {
        final result =
            await projectService.getProjectById('nonexistent_id', testUserId);
        expect(result, isNull);
      });

      test('should return null when user is not authorized', () async {
        // First create a project
        await projectService.createProject(testProject);

        final result = await projectService.getProjectById(
            testProject.id, 'unauthorized_user');
        expect(result, isNull);
      });
    });

    group('updateProject', () {
      test('should update project when found and user is authorized', () async {
        // First create a project
        await projectService.createProject(testProject);

        final updatedProject = testProject.copyWith(
          name: 'Updated Name',
          description: 'Updated Description',
        );

        final result = await projectService.updateProject(
          testProject.id,
          updatedProject,
          testUserId,
        );

        expect(result.id, equals(testProject.id));
        expect(result.name, equals('Updated Name'));
        expect(result.description, equals('Updated Description'));
        expect(result.creatorId, equals(testProject.creatorId));
        expect(result.memberIds, containsAll(testProject.memberIds));
      });

      test('should throw exception when project not found', () async {
        final updatedProject = testProject.copyWith(
          name: 'Updated Name',
          description: 'Updated Description',
        );

        expect(
          () => projectService.updateProject(
            'nonexistent_id',
            updatedProject,
            testUserId,
          ),
          throwsException,
        );
      });

      test('should throw exception when user is not authorized', () async {
        // First create a project
        await projectService.createProject(testProject);

        final updatedProject = testProject.copyWith(
          name: 'Updated Name',
          description: 'Updated Description',
        );

        expect(
          () => projectService.updateProject(
            testProject.id,
            updatedProject,
            'unauthorized_user',
          ),
          throwsException,
        );
      });
    });

    group('deleteProject', () {
      test('should delete project when found and user is authorized', () async {
        // First create a project
        await projectService.createProject(testProject);

        await projectService.deleteProject(testProject.id, testUserId);

        // Verify project is deleted
        final result =
            await projectService.getProjectById(testProject.id, testUserId);
        expect(result, isNull);
      });

      test('should throw exception when project not found', () async {
        expect(
          () => projectService.deleteProject('nonexistent_id', testUserId),
          throwsException,
        );
      });

      test('should throw exception when user is not authorized', () async {
        // First create a project
        await projectService.createProject(testProject);

        expect(
          () =>
              projectService.deleteProject(testProject.id, 'unauthorized_user'),
          throwsException,
        );
      });
    });

    group('getAllProjects', () {
      test('should return all projects for a user', () async {
        // Create multiple projects
        final project1 = testProject;
        final project2 = testProject.copyWith(
          id: '2',
          name: 'Test Project 2',
        );
        final project3 = testProject.copyWith(
          id: '3',
          name: 'Test Project 3',
        );

        await projectService.createProject(project1);
        await projectService.createProject(project2);
        await projectService.createProject(project3);

        final results = await projectService.getAllProjects(testUserId);

        expect(results.length, equals(3));
        expect(results.map((p) => p.id), containsAll(['1', '2', '3']));
        expect(
            results.map((p) => p.name),
            containsAll([
              'Test Project',
              'Test Project 2',
              'Test Project 3',
            ]));
      });

      test('should return empty list when user has no projects', () async {
        final results = await projectService.getAllProjects(testUserId);
        expect(results, isEmpty);
      });
    });
  });
}
