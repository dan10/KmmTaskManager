import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../middleware/auth_middleware.dart';
import '../services/task_service.dart';
import 'package:shared/src/models/task.dart';

class TaskRoutes {
  final TaskService _service;
  final AuthMiddleware _authMiddleware;

  TaskRoutes(this._service, this._authMiddleware);

  Router get router {
    final router = Router();

    router.get('/tasks', _authMiddleware.middleware(_getAllTasks));
    router.get('/tasks/<id>', _authMiddleware.middleware(_getTaskById));
    router.post('/tasks', _authMiddleware.middleware(_createTask));
    router.put('/tasks/<id>', _authMiddleware.middleware(_updateTask));
    router.delete('/tasks/<id>', _authMiddleware.middleware(_deleteTask));

    return router;
  }

  Future<Response> _getAllTasks(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final tasks = await _service.getAllTasks(userId);
      return Response.ok(jsonEncode(tasks.map((t) => t.toJson()).toList()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _getTaskById(Request request) async {
    try {
      final id = request.params['id']!;
      final userId = request.context['userId'] as String;
      final task = await _service.getTaskById(id, userId);
      if (task == null) {
        return Response.notFound('Task not found');
      }
      return Response.ok(jsonEncode(task.toJson()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _createTask(Request request) async {
    try {
      final body = await request.readAsString();
      final taskData = jsonDecode(body) as Map<String, dynamic>;
      final userId = request.context['userId'] as String;
      final task = Task(
        id: taskData['id'] as String,
        title: taskData['title'] as String,
        description: taskData['description'] as String,
        status: TaskStatus.values.firstWhere(
          (e) => e.toString() == taskData['status'] as String,
        ),
        priority: Priority.values.firstWhere(
          (e) => e.toString() == taskData['priority'] as String,
        ),
        dueDate: taskData['dueDate'] != null
            ? DateTime.parse(taskData['dueDate'] as String)
            : null,
        projectId: taskData['projectId'] as String?,
        assigneeId: taskData['assigneeId'] as String?,
        creatorId: userId,
      );
      final createdTask = await _service.createTask(task);
      return Response.ok(jsonEncode(createdTask.toJson()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _updateTask(Request request) async {
    try {
      final id = request.params['id']!;
      final userId = request.context['userId'] as String;
      final body = await request.readAsString();
      final taskData = jsonDecode(body) as Map<String, dynamic>;
      final task = Task(
        id: id,
        title: taskData['title'] as String,
        description: taskData['description'] as String,
        status: TaskStatus.values.firstWhere(
          (e) => e.toString() == taskData['status'] as String,
        ),
        priority: Priority.values.firstWhere(
          (e) => e.toString() == taskData['priority'] as String,
        ),
        dueDate: taskData['dueDate'] != null
            ? DateTime.parse(taskData['dueDate'] as String)
            : null,
        projectId: taskData['projectId'] as String?,
        assigneeId: taskData['assigneeId'] as String?,
        creatorId: userId,
      );
      final updatedTask = await _service.updateTask(id, task, userId);
      return Response.ok(jsonEncode(updatedTask.toJson()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _deleteTask(Request request) async {
    try {
      final id = request.params['id']!;
      final userId = request.context['userId'] as String;
      await _service.deleteTask(id, userId);
      return Response.ok('Task deleted');
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }
}
