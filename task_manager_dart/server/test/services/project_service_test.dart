import 'package:test/test.dart';
import 'package:task_manager_shared/models.dart';
import '../../lib/src/services/project_service.dart';
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/exceptions/custom_exceptions.dart';
import '../helpers/test_base.dart';

void main() {
  group('ProjectService Integration Tests', () {
    late TestBase testBase;
    late ProjectService projectService;
    late ProjectRepository projectRepository;

    setUpAll(() async {
      testBase = TestBase();
      await testBase.setUp();
      projectRepository = ProjectRepositoryImpl(testBase.connection);
      projectService = ProjectServiceImpl(projectRepository);
    });

    tearDownAll(() async {
      await testBase.tearDown();
    });

    Future<void> _setupTestData() async {
      // Create test users
      await testBase.connection.execute(
        'INSERT INTO users (id, display_name, email, google_id, created_at) VALUES (@id, @name, @email, @googleId, @createdAt)',
        substitutionValues: {
          'id': 'user-1',
          'name': 'Test User 1',
          'email': 'test1@example.com',
          'googleId': 'google-123',
          'createdAt': DateTime.now().toIso8601String(),
        },
      );

      await testBase.connection.execute(
        'INSERT INTO users (id, display_name, email, google_id, created_at) VALUES (@id, @name, @email, @googleId, @createdAt)',
        substitutionValues: {
          'id': 'user-2',
          'name': 'Test User 2',
          'email': 'test2@example.com',
          'googleId': 'google-456',
          'createdAt': DateTime.now().toIso8601String(),
        },
      );

      await testBase.connection.execute(
        'INSERT INTO users (id, display_name, email, google_id, created_at) VALUES (@id, @name, @email, @googleId, @createdAt)',
        substitutionValues: {
          'id': 'user-3',
          'name': 'Test User 3',
          'email': 'test3@example.com',
          'googleId': 'google-789',
          'createdAt': DateTime.now().toIso8601String(),
        },
      );
    }

    setUp(() async {
      await testBase.clearTables();
      await _setupTestData();
    });

    group('createProject', () {
      test('should create a new project successfully', () async {
        // Arrange
        const project = Project(
          id: 'project-1',
          name: 'Test Project',
          description: 'Test Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );

        // Act
        final result = await projectService.createProject(project);

        // Assert
        expect(result.id, equals('project-1'));
        expect(result.name, equals('Test Project'));
        expect(result.description, equals('Test Description'));
        expect(result.creatorId, equals('user-1'));
        expect(result.memberIds, containsAll(['user-1', 'user-2']));
      });

      test('should create project with just creator as member', () async {
        // Arrange
        const project = Project(
          id: 'project-2',
          name: 'Solo Project',
          description: 'Solo Description',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );

        // Act
        final result = await projectService.createProject(project);

        // Assert
        expect(result.id, equals('project-2'));
        expect(result.name, equals('Solo Project'));
        expect(result.memberIds, equals(['user-1']));
      });
    });

    group('getProjectById', () {
      test('should return project when user is creator', () async {
        // Arrange - Create a project first
        const project = Project(
          id: 'project-1',
          name: 'Creator Project',
          description: 'Test Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        await projectRepository.create(project);

        // Act - Creator accessing project
        final result = await projectService.getProjectById('project-1', 'user-1');

        // Assert
        expect(result, isNotNull);
        expect(result!.id, equals('project-1'));
        expect(result.name, equals('Creator Project'));
        expect(result.creatorId, equals('user-1'));
      });

      test('should return project when user is member', () async {
        // Arrange - Create a project where user-2 is a member
        const project = Project(
          id: 'project-1',
          name: 'Member Project',
          description: 'Test Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        await projectRepository.create(project);

        // Act - Member accessing project
        final result = await projectService.getProjectById('project-1', 'user-2');

        // Assert
        expect(result, isNotNull);
        expect(result!.id, equals('project-1'));
        expect(result.memberIds, contains('user-2'));
      });

      test('should return null when user is not member or creator', () async {
        // Arrange - Create a project without user-3
        const project = Project(
          id: 'project-1',
          name: 'Private Project',
          description: 'Test Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        await projectRepository.create(project);

        // Act - Non-member trying to access
        final result = await projectService.getProjectById('project-1', 'user-3');

        // Assert
        expect(result, isNull);
      });

      test('should return null when project does not exist', () async {
        // Act
        final result = await projectService.getProjectById('non-existent', 'user-1');

        // Assert
        expect(result, isNull);
      });
    });

    group('getProjects', () {
      test('should return projects filtered by creator', () async {
        // Arrange - Create multiple projects
        const project1 = Project(
          id: 'project-1',
          name: 'Project 1',
          description: 'Description 1',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );
        const project2 = Project(
          id: 'project-2',
          name: 'Project 2',
          description: 'Description 2',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        const project3 = Project(
          id: 'project-3',
          name: 'Project 3',
          description: 'Description 3',
          creatorId: 'user-2',
          memberIds: ['user-2'],
        );

        await projectRepository.create(project1);
        await projectRepository.create(project2);
        await projectRepository.create(project3);

        // Act - Get projects created by user-1
        final result = await projectService.getProjects(creatorId: 'user-1');

        // Assert
        expect(result.length, equals(2));
        expect(result.map((p) => p.id), containsAll(['project-1', 'project-2']));
        expect(result.every((p) => p.creatorId == 'user-1'), isTrue);
      });

      test('should return projects with text search', () async {
        // Arrange
        const project1 = Project(
          id: 'project-1',
          name: 'Bug Fix Project',
          description: 'Fix critical bugs',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );
        const project2 = Project(
          id: 'project-2',
          name: 'Feature Development',
          description: 'Add new features',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );

        await projectRepository.create(project1);
        await projectRepository.create(project2);

        // Act - Search for "bug" with creatorId filter
        final result = await projectService.getProjects(
          creatorId: 'user-1',
          query: 'bug',
        );

        // Assert
        expect(result.length, equals(1));
        expect(result.first.id, equals('project-1'));
        expect(result.first.name.toLowerCase(), contains('bug'));
      });

      test('should support pagination', () async {
        // Arrange - Create multiple projects
        for (int i = 1; i <= 5; i++) {
          final project = Project(
            id: 'project-$i',
            name: 'Project $i',
            description: 'Description $i',
            creatorId: 'user-1',
            memberIds: ['user-1'],
          );
          await projectRepository.create(project);
        }

        // Act - Get first page
        final page1 = await projectService.getProjects(
          creatorId: 'user-1',
          page: 0,
          size: 3,
        );

        // Assert
        expect(page1.length, equals(3));

        // Act - Get second page
        final page2 = await projectService.getProjects(
          creatorId: 'user-1',
          page: 1,
          size: 3,
        );

        // Assert
        expect(page2.length, equals(2));
      });
    });

    group('updateProject', () {
      test('should update project when user is creator', () async {
        // Arrange - Create original project
        const originalProject = Project(
          id: 'project-1',
          name: 'Original Project',
          description: 'Original Description',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );
        await projectRepository.create(originalProject);

        const updatedProject = Project(
          id: 'project-1',
          name: 'Updated Project',
          description: 'Updated Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2', 'user-3'],
        );

        // Act
        final result = await projectService.updateProject('project-1', 'user-1', updatedProject);

        // Assert
        expect(result.name, equals('Updated Project'));
        expect(result.description, equals('Updated Description'));
        expect(result.memberIds, containsAll(['user-1', 'user-2', 'user-3']));
      });

      test('should throw ForbiddenException when user is not creator', () async {
        // Arrange - Create project with user-1 as creator
        const project = Project(
          id: 'project-1',
          name: 'Original Project',
          description: 'Original Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        await projectRepository.create(project);

        const updatedProject = Project(
          id: 'project-1',
          name: 'Hacked Project',
          description: 'Hacked Description',
          creatorId: 'user-1',
          memberIds: ['user-2'],
        );

        // Act & Assert - user-2 tries to update user-1's project
        expect(
          () async => await projectService.updateProject('project-1', 'user-2', updatedProject),
          throwsA(isA<ForbiddenException>()),
        );
      });

      test('should throw ProjectNotFoundException when project does not exist', () async {
        // Arrange
        const project = Project(
          id: 'non-existent',
          name: 'Test Project',
          description: 'Test Description',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );

        // Act & Assert
        expect(
          () async => await projectService.updateProject('non-existent', 'user-1', project),
          throwsA(isA<ProjectNotFoundException>()),
        );
      });
    });

    group('deleteProject', () {
      test('should delete project when user is creator', () async {
        // Arrange
        const project = Project(
          id: 'project-1',
          name: 'Project to Delete',
          description: 'Description',
          creatorId: 'user-1',
          memberIds: ['user-1'],
        );
        await projectRepository.create(project);

        // Act
        final result = await projectService.deleteProject('project-1', 'user-1');

        // Assert
        expect(result, isTrue);

        // Verify project was deleted
        final deletedProject = await projectRepository.findById('project-1');
        expect(deletedProject, isNull);
      });

      test('should throw ForbiddenException when user is not creator', () async {
        // Arrange
        const project = Project(
          id: 'project-1',
          name: 'Project to Delete',
          description: 'Description',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        await projectRepository.create(project);

        // Act & Assert - user-2 tries to delete user-1's project
        expect(
          () async => await projectService.deleteProject('project-1', 'user-2'),
          throwsA(isA<ForbiddenException>()),
        );
      });

      test('should throw ProjectNotFoundException when project does not exist', () async {
        // Act & Assert
        expect(
          () async => await projectService.deleteProject('non-existent', 'user-1'),
          throwsA(isA<ProjectNotFoundException>()),
        );
      });
    });

    group('getProjectsByMember', () {
      test('should return all projects where user is a member', () async {
        // Arrange - Create projects with user-2 as member
        const project1 = Project(
          id: 'project-1',
          name: 'Project 1',
          description: 'Description 1',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        const project2 = Project(
          id: 'project-2',
          name: 'Project 2',
          description: 'Description 2',
          creatorId: 'user-3',
          memberIds: ['user-2', 'user-3'],
        );
        const project3 = Project(
          id: 'project-3',
          name: 'Project 3',
          description: 'Description 3',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-3'],
        );

        await projectRepository.create(project1);
        await projectRepository.create(project2);
        await projectRepository.create(project3);

        // Act - Get projects where user-2 is member
        final result = await projectService.getProjectsByMember('user-2');

        // Assert
        expect(result.length, equals(2));
        expect(result.map((p) => p.id), containsAll(['project-1', 'project-2']));
        expect(result.every((p) => p.memberIds.contains('user-2')), isTrue);
      });

      test('should return empty list when user is not member of any project', () async {
        // Arrange - Create a project without user-3
        const project = Project(
          id: 'project-1',
          name: 'Project 1',
          description: 'Description 1',
          creatorId: 'user-1',
          memberIds: ['user-1', 'user-2'],
        );
        await projectRepository.create(project);

        // Act
        final result = await projectService.getProjectsByMember('user-3');

        // Assert
        expect(result, isEmpty);
      });
    });
  });
} 