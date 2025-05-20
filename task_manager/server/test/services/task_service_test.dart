import 'package:test/test.dart';
import 'package:shared/src/models/task.dart' as shared;
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/services/task_service.dart';

void main() {
  late InMemoryDatabase db;
  late TaskRepository repository;
  late TaskService service;

  setUp(() {
    db = InMemoryDatabase();
    repository = TaskRepositoryImpl(db);
    service = TaskServiceImpl(repository);
  });

  group('TaskService', () {
    test('creates a new task', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        dueDate: DateTime.now(),
        projectId: 'project1',
        assigneeId: 'user1',
        creatorId: 'user1',
      );

      final createdTask = await service.createTask(task);
      expect(createdTask.id, equals(task.id));
      expect(createdTask.title, equals(task.title));
    });

    test('finds a task by id', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        dueDate: DateTime.now(),
        projectId: 'project1',
        assigneeId: 'user1',
        creatorId: 'user1',
      );

      await service.createTask(task);
      final foundTask = await service.getTaskById(task.id);
      expect(foundTask, isNotNull);
      expect(foundTask?.id, equals(task.id));
    });

    test('updates a task', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        dueDate: DateTime.now(),
        projectId: 'project1',
        assigneeId: 'user1',
        creatorId: 'user1',
      );

      await service.createTask(task);
      final updatedTask = shared.Task(
        id: task.id,
        title: 'Updated Task',
        description: 'Updated Description',
        status: shared.TaskStatus.inProgress,
        priority: shared.Priority.high,
        dueDate: DateTime.now(),
        projectId: 'project1',
        assigneeId: 'user1',
        creatorId: 'user1',
      );

      final result = await service.updateTask(updatedTask);
      expect(result.title, equals(updatedTask.title));
    });

    test('deletes a task', () async {
      final task = shared.Task(
        id: '1',
        title: 'Test Task',
        description: 'Test Description',
        status: shared.TaskStatus.todo,
        priority: shared.Priority.medium,
        dueDate: DateTime.now(),
        projectId: 'project1',
        assigneeId: 'user1',
        creatorId: 'user1',
      );

      await service.createTask(task);
      await service.deleteTask(task.id);
      final foundTask = await service.getTaskById(task.id);
      expect(foundTask, isNull);
    });
  });
}
