import 'package:test/test.dart';
import 'package:shared/src/models/task.dart' as shared;
import 'package:shared/src/models/user.dart' as shared;
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../helpers/test_base.dart';

void main() {
  late TestBase testBase;
  late TaskRepository repository;
  late shared.User testUser;
  late shared.User testAssignee;

  setUp(() async {
    testBase = TestBase();
    await testBase.setUp();
    repository = TaskRepository(testBase.connection);

    // Create test users
    final authRepo = AuthRepository(testBase.connection);
    testUser = shared.User(
      id: '1',
      name: 'Test User',
      email: 'test@example.com',
      passwordHash: 'hashed_password',
    );
    testAssignee = shared.User(
      id: '2',
      name: 'Test Assignee',
      email: 'assignee@example.com',
      passwordHash: 'hashed_password',
    );
    await authRepo.createUser(testUser);
    await authRepo.createUser(testAssignee);
  });

  tearDown(() async {
    await testBase.clearTables();
    await testBase.tearDown();
  });

  group('TaskRepository', () {
    test('should create a task', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      final createdTask = await repository.createTask(task);
      expect(createdTask.id, equals(task.id));
      expect(createdTask.title, equals(task.title));
      expect(createdTask.description, equals(task.description));
      expect(createdTask.status, equals(task.status));
      expect(createdTask.priority, equals(task.priority));
      expect(createdTask.creatorId, equals(task.creatorId));
    });

    test('should find a task by id', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      await repository.createTask(task);
      final foundTask = await repository.findTaskById(task.id);
      expect(foundTask, isNotNull);
      expect(foundTask!.id, equals(task.id));
      expect(foundTask.title, equals(task.title));
      expect(foundTask.description, equals(task.description));
      expect(foundTask.status, equals(task.status));
      expect(foundTask.priority, equals(task.priority));
      expect(foundTask.creatorId, equals(task.creatorId));
    });

    test('should find all tasks', () async {
      final task1 = shared.Task(
        id: '1',
        title: 'Test Task 1',
        description: 'Test Description 1',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      final task2 = shared.Task(
        id: '2',
        title: 'Test Task 2',
        description: 'Test Description 2',
        status: shared.TaskStatus.inProgress,
        priority: shared.Priority.high,
        creatorId: testUser.id,
      );

      await repository.createTask(task1);
      await repository.createTask(task2);

      final tasks = await repository.findAllTasks();
      expect(tasks.length, equals(2));
      expect(tasks.any((t) => t.id == task1.id), isTrue);
      expect(tasks.any((t) => t.id == task2.id), isTrue);
    });

    test('should find tasks by project id', () async {
      final task1 = shared.Task(
        id: '1',
        title: 'Task 1',
        description: 'Description 1',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );
      final task2 = shared.Task(
        id: '2',
        title: 'Task 2',
        description: 'Description 2',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      await repository.createTask(task1);
      await repository.createTask(task2);

      final tasks = await repository.findTasksByProjectId('project1');
      expect(tasks.length, equals(0));
    });

    test('should find tasks by assignee id', () async {
      final task1 = shared.Task(
        id: '1',
        title: 'Task 1',
        description: 'Description 1',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        assigneeId: testAssignee.id,
        creatorId: testUser.id,
      );
      final task2 = shared.Task(
        id: '2',
        title: 'Task 2',
        description: 'Description 2',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        assigneeId: testAssignee.id,
        creatorId: testUser.id,
      );

      await repository.createTask(task1);
      await repository.createTask(task2);

      final tasks = await repository.findTasksByAssigneeId(testAssignee.id);
      expect(tasks.length, equals(2));
      expect(tasks.map((t) => t.id), containsAll([task1.id, task2.id]));
    });

    test('should find tasks by creator id', () async {
      final task1 = shared.Task(
        id: '1',
        title: 'Task 1',
        description: 'Description 1',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );
      final task2 = shared.Task(
        id: '2',
        title: 'Task 2',
        description: 'Description 2',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      await repository.createTask(task1);
      await repository.createTask(task2);

      final tasks = await repository.findTasksByCreatorId(testUser.id);
      expect(tasks.length, equals(2));
      expect(tasks.map((t) => t.id), containsAll([task1.id, task2.id]));
    });

    test('should update a task', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      await repository.createTask(task);

      final updatedTask = shared.Task(
        id: task.id,
        title: 'Updated Task',
        description: 'Updated Description',
        status: shared.TaskStatus.inProgress,
        priority: shared.Priority.high,
        assigneeId: testAssignee.id,
        creatorId: task.creatorId,
      );

      await repository.updateTask(updatedTask);
      final foundTask = await repository.findTaskById(task.id);
      expect(foundTask, isNotNull);
      expect(foundTask!.title, equals(updatedTask.title));
      expect(foundTask.description, equals(updatedTask.description));
      expect(foundTask.status, equals(updatedTask.status));
      expect(foundTask.priority, equals(updatedTask.priority));
      expect(foundTask.assigneeId, equals(updatedTask.assigneeId));
    });

    test('should delete a task', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        creatorId: testUser.id,
      );

      await repository.createTask(task);
      await repository.deleteTask(task.id);
      final foundTask = await repository.findTaskById(task.id);
      expect(foundTask, isNull);
    });
  });
}
