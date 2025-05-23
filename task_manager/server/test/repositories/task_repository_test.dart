import 'package:test/test.dart';
import 'package:shared/models.dart';
import '../../lib/src/repositories/task_repository.dart';
import '../helpers/test_base.dart';

void main() {
  group('TaskRepository Integration Tests', () {
    late TestBase testBase;
    late TaskRepository taskRepository;

    setUpAll(() async {
      testBase = TestBase();
      await testBase.setUp();
      taskRepository = TaskRepository(testBase.connection);
    });

    tearDownAll(() async {
      await testBase.tearDown();
    });

    Future<void> _setupTestData() async {
      // Create test user
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

      // Create additional test users
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

      await testBase.connection.execute(
        'INSERT INTO projects (id, name, description, creator_id) VALUES (@id, @name, @description, @creatorId)',
        substitutionValues: {
          'id': 'project-2',
          'name': 'Test Project 2',
          'description': 'Test Description 2',
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
          'projectId': 'project-2',
          'userId': 'user-1',
        },
      );
    }

    setUp(() async {
      await testBase.clearTables();
      await _setupTestData();
    });

    group('create and findById', () {
      test('should create a new task successfully', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'Test Task',
          description: 'Test Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );

        // Act
        final result = await taskRepository.create(task);

        // Assert
        expect(result.id, equals('task-1'));
        expect(result.title, equals('Test Task'));
        expect(result.description, equals('Test Description'));
        expect(result.status, equals(TaskStatus.todo));
        expect(result.priority, equals(Priority.medium));
        expect(result.projectId, equals('project-1'));
        expect(result.creatorId, equals('user-1'));
      });

      test('should return null when task does not exist', () async {
        // Act
        final result = await taskRepository.findById('non-existent');

        // Assert
        expect(result, isNull);
      });
    });

    group('getTasks', () {
      test('should filter tasks by projectId', () async {
        // Arrange
        const task1 = Task(
          id: 'task-1',
          title: 'Task 1',
          description: 'Description 1',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        const task2 = Task(
          id: 'task-2',
          title: 'Task 2',
          description: 'Description 2',
          status: TaskStatus.inProgress,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        const task3 = Task(
          id: 'task-3',
          title: 'Task 3',
          description: 'Description 3',
          status: TaskStatus.done,
          priority: Priority.low,
          projectId: 'project-2',
          creatorId: 'user-1',
        );

        await taskRepository.create(task1);
        await taskRepository.create(task2);
        await taskRepository.create(task3);

        // Act
        final result = await taskRepository.getTasks(projectId: 'project-1');

        // Assert
        expect(result.length, equals(2));
        expect(result.map((t) => t.id), containsAll(['task-1', 'task-2']));
        expect(result.every((t) => t.projectId == 'project-1'), isTrue);
      });

      test('should filter tasks by assigneeId', () async {
        // Arrange
        const task1 = Task(
          id: 'task-1',
          title: 'Assigned Task 1',
          description: 'Description 1',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );
        const task2 = Task(
          id: 'task-2',
          title: 'Assigned Task 2',
          description: 'Description 2',
          status: TaskStatus.inProgress,
          priority: Priority.high,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-2',
        );
        const task3 = Task(
          id: 'task-3',
          title: 'Other Task',
          description: 'Description 3',
          status: TaskStatus.done,
          priority: Priority.low,
          projectId: 'project-1',
          creatorId: 'user-1',
          assigneeId: 'user-3',
        );

        await taskRepository.create(task1);
        await taskRepository.create(task2);
        await taskRepository.create(task3);

        // Act
        final result = await taskRepository.getTasks(assigneeId: 'user-2');

        // Assert
        expect(result.length, equals(2));
        expect(result.map((t) => t.id), containsAll(['task-1', 'task-2']));
        expect(result.every((t) => t.assigneeId == 'user-2'), isTrue);
      });

      test('should support pagination', () async {
        // Arrange - Create multiple tasks
        for (int i = 1; i <= 10; i++) {
          final task = Task(
            id: 'task-$i',
            title: 'Task $i',
            description: 'Description $i',
            status: TaskStatus.todo,
            priority: Priority.medium,
            projectId: 'project-1',
            creatorId: 'user-1',
          );
          await taskRepository.create(task);
        }

        // Act - Get first page
        final page1 = await taskRepository.getTasks(
          projectId: 'project-1',
          page: 0,
          size: 5,
        );

        // Assert
        expect(page1.length, equals(5));

        // Act - Get second page
        final page2 = await taskRepository.getTasks(
          projectId: 'project-1',
          page: 1,
          size: 5,
        );

        // Assert
        expect(page2.length, equals(5));
      });
    });

    group('update', () {
      test('should update existing task successfully', () async {
        // Arrange
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
        final result = await taskRepository.update(updatedTask);

        // Assert
        expect(result.title, equals('Updated Title'));
        expect(result.description, equals('Updated Description'));
        expect(result.status, equals(TaskStatus.done));
        expect(result.priority, equals(Priority.high));
        expect(result.assigneeId, equals('user-2'));

        // Verify the update persisted
        final fetchedTask = await taskRepository.findById('task-1');
        expect(fetchedTask!.title, equals('Updated Title'));
        expect(fetchedTask.status, equals(TaskStatus.done));
      });
    });

    group('delete', () {
      test('should delete existing task successfully', () async {
        // Arrange
        const task = Task(
          id: 'task-1',
          title: 'To Delete',
          description: 'To Delete Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project-1',
          creatorId: 'user-1',
        );
        await taskRepository.create(task);

        // Act
        await taskRepository.delete('task-1');

        // Verify task was deleted
        final fetchedTask = await taskRepository.findById('task-1');
        expect(fetchedTask, isNull);
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
        final result = await taskRepository.assignTask('task-1', 'user-2');

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
        final result = await taskRepository.changeTaskStatus('task-1', TaskStatus.done);

        // Assert
        expect(result.id, equals('task-1'));
        expect(result.status, equals(TaskStatus.done));
      });
    });
  });
} 