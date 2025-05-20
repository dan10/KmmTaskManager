import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:shared/src/models/task.dart' as shared;
import '../services/task_service.dart';
import '../middleware/auth_middleware.dart';

class TaskRoutes {
  final TaskService _service;

  TaskRoutes(this._service);

  Router get router {
    final router = Router();

    // GET /tasks - Get all tasks
    router.get('/tasks', (Request request) async {
      final tasks = await _service.getAllTasks();
      return Response.ok(
        jsonEncode(tasks.map((t) => t.toJson()).toList()),
        headers: {'content-type': 'application/json'},
      );
    });

    // GET /tasks/<id> - Get a task by ID
    router.get('/tasks/<id>', (Request request, String id) async {
      final task = await _service.getTaskById(id);
      if (task == null) {
        return Response.notFound('Task not found');
      }
      return Response.ok(
        jsonEncode(task.toJson()),
        headers: {'content-type': 'application/json'},
      );
    });

    // POST /tasks - Create a new task
    router.post('/tasks', (Request request) async {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      final task = shared.Task.fromJson(data);
      final createdTask = await _service.createTask(task);
      return Response.ok(
        jsonEncode(createdTask.toJson()),
        headers: {'content-type': 'application/json'},
      );
    });

    // PUT /tasks/<id> - Update a task
    router.put('/tasks/<id>', (Request request, String id) async {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      final task = shared.Task.fromJson(data);
      final updatedTask = await _service.updateTask(task);
      return Response.ok(
        jsonEncode(updatedTask.toJson()),
        headers: {'content-type': 'application/json'},
      );
    });

    // DELETE /tasks/<id> - Delete a task
    router.delete('/tasks/<id>', (Request request, String id) async {
      await _service.deleteTask(id);
      return Response.ok('Task deleted');
    });

    return router;
  }
}
