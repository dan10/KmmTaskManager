import 'dart:convert';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import 'package:shelf_test_handler/shelf_test_handler.dart';
import 'package:shared/src/models/task.dart' as shared;
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/services/task_service.dart';
import '../../lib/src/routes/task_routes.dart';

void main() {
  late InMemoryDatabase db;
  late TaskRepository repository;
  late TaskService service;
  late TaskRoutes routes;
  late Handler handler;

  setUp(() {
    db = InMemoryDatabase();
    repository = TaskRepositoryImpl(db);
    service = TaskServiceImpl(repository);
    routes = TaskRoutes(service);
    handler = routes.router;
  });

  group('TaskRoutes', () {
    test('GET /tasks returns all tasks', () async {
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

      final request = Request('GET', Uri.parse('http://localhost/tasks'));
      final response = await handler(request);
      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final tasks = (jsonDecode(body) as List)
          .map((t) => shared.Task.fromJson(t))
          .toList();
      expect(tasks.length, equals(1));
      expect(tasks.first.id, equals(task.id));
    });

    test('GET /tasks/<id> returns a task by ID', () async {
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

      final request = Request('GET', Uri.parse('http://localhost/tasks/1'));
      final response = await handler(request);
      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final foundTask = shared.Task.fromJson(jsonDecode(body));
      expect(foundTask.id, equals(task.id));
    });

    test('POST /tasks creates a new task', () async {
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

      final request = Request(
        'POST',
        Uri.parse('http://localhost/tasks'),
        body: jsonEncode(task.toJson()),
        headers: {'content-type': 'application/json'},
      );
      final response = await handler(request);

      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final createdTask = shared.Task.fromJson(jsonDecode(body));
      expect(createdTask.id, equals(task.id));
    });

    test('PUT /tasks/<id> updates a task', () async {
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

      final request = Request(
        'PUT',
        Uri.parse('http://localhost/tasks/1'),
        body: jsonEncode(updatedTask.toJson()),
        headers: {'content-type': 'application/json'},
      );
      final response = await handler(request);

      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final result = shared.Task.fromJson(jsonDecode(body));
      expect(result.title, equals(updatedTask.title));
    });

    test('DELETE /tasks/<id> deletes a task', () async {
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

      final request = Request('DELETE', Uri.parse('http://localhost/tasks/1'));
      final response = await handler(request);
      expect(response.statusCode, equals(200));
      expect(response.readAsString(), completion(equals('Task deleted')));
    });
  });
}
