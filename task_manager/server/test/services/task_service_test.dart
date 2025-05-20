import 'package:test/test.dart';
import 'package:shared/src/models/task.dart' as shared;
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/services/task_service.dart';
import '../helpers/test_base.dart';

void main() {
  late TaskServiceImpl taskService;
  late TaskRepository taskRepository;
  late TestBase testBase;
  late String testUserId;
  late DateTime testDueDate;

  setUp(() async {
    testBase = TestBase();
    await testBase.setUp();
    taskRepository = TaskRepository(testBase.connection);
    taskService = TaskServiceImpl(taskRepository);
    // Create a UTC DateTime for consistent testing
    testDueDate = DateTime.utc(2025, 5, 20, 10, 30);

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

  group('TaskService', () {
    group('createTask', () {
      test('should create a new task successfully', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        final result = await taskService.createTask(testTask);
        expect(result.id, equals(testTask.id));
        expect(result.title, equals(testTask.title));
        expect(result.description, equals(testTask.description));
        expect(result.status, equals(testTask.status));
        expect(result.priority, equals(testTask.priority));
        expect(result.dueDate?.toUtc().toIso8601String(),
            equals(testTask.dueDate?.toUtc().toIso8601String()));
        expect(result.projectId, equals(testTask.projectId));
        expect(result.assigneeId, equals(testTask.assigneeId));
        expect(result.creatorId, equals(testTask.creatorId));
      });
    });

    group('getTaskById', () {
      test('should return task when found and user is authorized', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        await taskService.createTask(testTask);
        final result = await taskService.getTaskById(testTask.id, testUserId);
        expect(result, isNotNull);
        expect(result?.id, equals(testTask.id));
        expect(result?.title, equals(testTask.title));
        expect(result?.description, equals(testTask.description));
        expect(result?.status, equals(testTask.status));
        expect(result?.priority, equals(testTask.priority));
        expect(result?.dueDate?.toUtc().toIso8601String(),
            equals(testTask.dueDate?.toUtc().toIso8601String()));
        expect(result?.projectId, equals(testTask.projectId));
        expect(result?.assigneeId, equals(testTask.assigneeId));
        expect(result?.creatorId, equals(testTask.creatorId));
      });

      test('should return null when task not found', () async {
        final result =
            await taskService.getTaskById('nonexistent_id', testUserId);
        expect(result, isNull);
      });

      test('should return null when user is not authorized', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        await taskService.createTask(testTask);
        final result =
            await taskService.getTaskById(testTask.id, 'unauthorized_user');
        expect(result, isNull);
      });
    });

    group('updateTask', () {
      test('should update task when found and user is authorized', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        await taskService.createTask(testTask);
        final updatedTask = testTask.copyWith(
          title: 'Updated Title',
          description: 'Updated Description',
          status: shared.TaskStatus.inProgress,
          priority: shared.Priority.high,
        );
        final result = await taskService.updateTask(
          testTask.id,
          updatedTask,
          testUserId,
        );
        expect(result.id, equals(testTask.id));
        expect(result.title, equals('Updated Title'));
        expect(result.description, equals('Updated Description'));
        expect(result.status, equals(shared.TaskStatus.inProgress));
        expect(result.priority, equals(shared.Priority.high));
        expect(result.dueDate?.toUtc().toIso8601String(),
            equals(testTask.dueDate?.toUtc().toIso8601String()));
        expect(result.projectId, equals(testTask.projectId));
        expect(result.assigneeId, equals(testTask.assigneeId));
        expect(result.creatorId, equals(testTask.creatorId));
      });

      test('should throw exception when task not found', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        final updatedTask = testTask.copyWith(
          title: 'Updated Title',
          description: 'Updated Description',
        );
        expect(
          () => taskService.updateTask(
            'nonexistent_id',
            updatedTask,
            testUserId,
          ),
          throwsException,
        );
      });

      test('should throw exception when user is not authorized', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        await taskService.createTask(testTask);
        final updatedTask = testTask.copyWith(
          title: 'Updated Title',
          description: 'Updated Description',
        );
        expect(
          () => taskService.updateTask(
            testTask.id,
            updatedTask,
            'unauthorized_user',
          ),
          throwsException,
        );
      });
    });

    group('deleteTask', () {
      test('should delete task when found and user is authorized', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        await taskService.createTask(testTask);
        await taskService.deleteTask(testTask.id, testUserId);
        final result = await taskService.getTaskById(testTask.id, testUserId);
        expect(result, isNull);
      });

      test('should throw exception when task not found', () async {
        expect(
          () => taskService.deleteTask('nonexistent_id', testUserId),
          throwsException,
        );
      });

      test('should throw exception when user is not authorized', () async {
        final testTask = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        await taskService.createTask(testTask);
        expect(
          () => taskService.deleteTask(testTask.id, 'unauthorized_user'),
          throwsException,
        );
      });
    });

    group('getAllTasks', () {
      test('should return all tasks for a user', () async {
        final task1 = shared.Task(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: shared.TaskStatus.todo,
          priority: shared.Priority.medium,
          dueDate: testDueDate,
          projectId: null,
          assigneeId: testUserId,
          creatorId: testUserId,
        );
        final task2 = task1.copyWith(
          id: '2',
          title: 'Test Task 2',
        );
        final task3 = task1.copyWith(
          id: '3',
          title: 'Test Task 3',
        );
        await taskService.createTask(task1);
        await taskService.createTask(task2);
        await taskService.createTask(task3);
        final results = await taskService.getAllTasks(testUserId);
        expect(results.length, equals(3));
        expect(results.map((t) => t.id), containsAll(['1', '2', '3']));
        expect(
          results.map((t) => t.title),
          containsAll([
            'Test Task',
            'Test Task 2',
            'Test Task 3',
          ]),
        );
      });

      test('should return empty list when user has no tasks', () async {
        final results = await taskService.getAllTasks(testUserId);
        expect(results, isEmpty);
      });
    });
  });
}
