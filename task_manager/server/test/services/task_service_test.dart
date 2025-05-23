import 'package:test/test.dart';
import 'package:shared/models.dart';
import '../../lib/src/services/task_service.dart';
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/exceptions/custom_exceptions.dart';
import '../helpers/test_base.dart';

void main() {
  group('TaskService Integration Tests', () {
    late TestBase testBase;
    late TaskService taskService;
    late TaskRepository taskRepository;

    setUpAll(() async {
      testBase = TestBase();
      await testBase.setUp();
      taskRepository = TaskRepository(testBase.connection);
      taskService = TaskServiceImpl(taskRepository);
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
          'name': 'Test User',
          'email': 'test@example.com',
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
      
      // Create test projects
      await testBase.connection.execute(
        'INSERT INTO projects (id, name, description, creator_id) VALUES (@id, @name, @description, @creatorId)',
        substitutionValues: {
          'id': 'project-1',
          'name': 'Test Project',
          'description': 'Test Description',
          'creatorId': 'user-1',
        },
      );

      // Create project memberships
      await testBase.connection.execute(
        'INSERT INTO project_members (project_id, user_id) VALUES (@projectId, @userId)',
        substitutionValues: {
          'projectId': 'project-1',
          'userId': 'user-1',
        },
      );

      await testBase.connection.execute(
        'INSERT INTO project_members (project_id, user_id) VALUES (@projectId, @userId)',
        substitutionValues: {
          'projectId': 'project-1',
          'userId': 'user-2',
        },
      );
    }

    setUp(() async {
      await testBase.clearTables();
      await _setupTestData();
    });

    group('createTask', () {
      test('should create a new task successfully', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'New Task',
          description: 'Task Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );

        // Act
        final result = await taskService.createTask(task);

        // Assert
        expect(result.id, equals('task-1'));
        expect(result.title, equals('New Task'));
        expect(result.description, equals('Task Description'));
        expect(result.status, equals(TaskStatus.todo));
        expect(result.priority, equals(Priority.medium));
        expect(result.projectId, equals('project-1'));
        expect(result.creatorId, equals('user-1'));
      });

      test('should throw exception for invalid project', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'New Task',
          description: 'Task Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'invalid-project',
          creatorId: 'user-1',
        );

        // Act & Assert - Database constraint should catch this
        expect(
          () async => await taskService.createTask(task),
          throwsException,
        );
      });
    });

    group('getTaskById', () {
      test('should return task when user has access', () async {
        // Arrange - Create a task first
        const task = Task(
          id: 'task-1',
          title: 'Test Task',
          description: 'Test Description',
          status: TaskStatus.todo,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );
        await taskRepository.create(task);

        // Act
        final result = await taskService.getTaskById('task-1', 'user-1');

        // Assert
        expect(result, isNotNull);
        expect(result!.id, equals('task-1'));
        expect(result.title, equals('Test Task'));
        expect(result.assigneeId, equals('user-2'));
      });

      test('should return task when user is assignee', () async {
        // Arrange - Create a task assigned to user-2
        const task = Task(
          id: 'task-1',
          title: 'Assigned Task',
          description: 'Test Description',
          status: TaskStatus.inProgress,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );
        await taskRepository.create(task);

        // Act - user-2 accessing the task they're assigned to
        final result = await taskService.getTaskById('task-1', 'user-2');

        // Assert
        expect(result, isNotNull);
        expect(result!.id, equals('task-1'));
        expect(result.assigneeId, equals('user-2'));
      });

      test('should return null when task does not exist', () async {
        // Act
        final result = await taskService.getTaskById('non-existent', 'user-1');

        // Assert
        expect(result, isNull);
      });
    });

    group('getTasks', () {
      test('should return tasks filtered by project', () async {
        // Arrange - Create multiple tasks
        const task1 = Task(
          id: 'task-1',
          title: 'Project Task 1',
          description: 'Description 1',
          status: TaskStatus.todo,
          priority: Priority.low,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        const task2 = Task(
          id: 'task-2',
          title: 'Project Task 2',
          description: 'Description 2',
          status: TaskStatus.inProgress,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );

        await taskRepository.create(task1);
        await taskRepository.create(task2);

        // Act
        final result = await taskService.getTasks(
          projectId: 'project-1',
          page: 0,
          size: 10,
        );

        // Assert
        expect(result.length, equals(2));
        expect(result.map((t) => t.id), containsAll(['task-1', 'task-2']));
        expect(result.every((t) => t.projectId == 'project-1'), isTrue);
      });

      test('should return tasks filtered by assignee', () async {
        // Arrange
        const task1 = Task(
          id: 'task-1',
          title: 'Assigned Task',
          description: 'Description 1',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );
        const task2 = Task(
          id: 'task-2',
          title: 'Unassigned Task',
          description: 'Description 2',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );

        await taskRepository.create(task1);
        await taskRepository.create(task2);

        // Act
        final result = await taskService.getTasks(
          assigneeId: 'user-2',
          page: 0,
          size: 10,
        );

        // Assert
        expect(result.length, equals(1));
        expect(result.first.id, equals('task-1'));
        expect(result.first.assigneeId, equals('user-2'));
      });

      test('should support text search in title and description', () async {
        // Arrange
        const task1 = Task(
          id: 'task-1',
          title: 'Important Bug Fix',
          description: 'Fix critical issue',
          status: TaskStatus.todo,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        const task2 = Task(
          id: 'task-2',
          title: 'Feature Development',
          description: 'Add new feature',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );

        await taskRepository.create(task1);
        await taskRepository.create(task2);

        // Act - Search for "bug"
        final result = await taskService.getTasks(
          query: 'bug',
          page: 0,
          size: 10,
        );

        // Assert
        expect(result.length, equals(1));
        expect(result.first.id, equals('task-1'));
        expect(result.first.title.toLowerCase(), contains('bug'));
      });
    });

    group('updateTask', () {
      test('should update task when user is creator', () async {
        // Arrange - Create original task
        const originalTask = Task(
          id: 'task-1',
          title: 'Original Title',
          description: 'Original Description',
          status: TaskStatus.todo,
          priority: Priority.low,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(originalTask);

        const updatedTask = Task(
          id: 'task-1',
          title: 'Updated Title',
          description: 'Updated Description',
          status: TaskStatus.done,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );

        // Act
        final result = await taskService.updateTask('task-1', updatedTask, 'user-1');

        // Assert
        expect(result.title, equals('Updated Title'));
        expect(result.description, equals('Updated Description'));
        expect(result.status, equals(TaskStatus.done));
        expect(result.priority, equals(Priority.high));
        expect(result.assigneeId, equals('user-2'));
      });

      test('should throw ForbiddenException when user is not creator', () async {
        // Arrange - Create task with user-1 as creator
        const task = Task(
          id: 'task-1',
          title: 'Original Title',
          description: 'Original Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(task);

        const updatedTask = Task(
          id: 'task-1',
          title: 'Hacked Title',
          description: 'Updated Description',
          status: TaskStatus.done,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
        );

        // Act & Assert - user-2 tries to update user-1's task
        expect(
          () async => await taskService.updateTask('task-1', updatedTask, 'user-2'),
          throwsA(isA<ForbiddenException>()),
        );
      });
    });

    group('deleteTask', () {
      test('should delete task when user is creator', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'Task to Delete',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(task);

        // Act
        await taskService.deleteTask('task-1', 'user-1');

        // Assert - Task should be deleted
        final deletedTask = await taskRepository.findById('task-1');
        expect(deletedTask, isNull);
      });

      test('should throw ForbiddenException when user is not creator', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'Task to Delete',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(task);

        // Act & Assert - user-2 tries to delete user-1's task
        expect(
          () async => await taskService.deleteTask('task-1', 'user-2'),
          throwsA(isA<ForbiddenException>()),
        );
      });
    });

    group('assignTask', () {
      test('should assign task to user successfully', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'Task to Assign',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(task);

        // Act
        final result = await taskService.assignTask('task-1', 'user-2');

        // Assert
        expect(result.id, equals('task-1'));
        expect(result.assigneeId, equals('user-2'));
      });
    });

    group('changeTaskStatus', () {
      test('should change task status successfully', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'Task Status Change',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(task);

        // Act
        final result = await taskService.changeTaskStatus('task-1', TaskStatus.inProgress);

        // Assert
        expect(result.id, equals('task-1'));
        expect(result.status, equals(TaskStatus.inProgress));

        // Act again - Change to done
        final finalResult = await taskService.changeTaskStatus('task-1', TaskStatus.done);

        // Assert
        expect(finalResult.status, equals(TaskStatus.done));
      });
    });
  });
} 