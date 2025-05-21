import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../middleware/auth_middleware.dart';
import '../services/task_service.dart';
import 'package:shared/models.dart';
import '../util/shelf_helpers.dart';
import '../dto/task/task_assign_request_dto.dart';
import '../dto/task/task_status_change_request_dto.dart';
import '../exceptions/custom_exceptions.dart'; // Import new exceptions
import '../dto/error_response_dto.dart';      // Import ErrorResponseDto

class TaskRoutes {
  final TaskService _service;
  final AuthMiddleware _authMiddleware;

  TaskRoutes(this._service, this._authMiddleware);

  Router get router {
    final router = Router();

    router.get('/tasks', _authMiddleware.middleware(_getAllTasks)); // Will be updated
    router.get('/tasks/created-by-me', _authMiddleware.middleware(_getTasksCreatedByMe)); // New
    router.get('/tasks/project/<projectId>', _authMiddleware.middleware(_getTasksByProject));
    router.get('/tasks/<id>', _authMiddleware.middleware(_getTaskById));
    router.post('/tasks', _authMiddleware.middleware(_createTask));
    router.put('/tasks/<id>', _authMiddleware.middleware(_updateTask));
    router.delete('/tasks/<id>', _authMiddleware.middleware(_deleteTask));

    // New routes for assigning and changing status
    router.post('/tasks/<id>/assign', _authMiddleware.middleware(_assignTask));
    router.post('/tasks/<id>/status', _authMiddleware.middleware(_changeTaskStatus));

    return router;
  }

  Future<Response> _getAllTasks(Request request) async {
    final userId = request.context['userId'] as String;
    final params = request.url.queryParameters;

    final pageStr = params['page'] ?? '0';
    int page;
    try {
      page = int.parse(pageStr);
      if (page < 0) throw ValidationException(message: 'Query parameter "page" must be non-negative.');
    } catch (e) {
      throw ValidationException(message: 'Query parameter "page" must be a valid integer.');
    }

    final sizeStr = params['size'] ?? '10';
    int size;
    try {
      size = int.parse(sizeStr);
      if (size <= 0) throw ValidationException(message: 'Query parameter "size" must be positive.');
      if (size > 100) size = 100; // Cap size
    } catch (e) {
      throw ValidationException(message: 'Query parameter "size" must be a valid integer.');
    }

    final query = params['query'];
    final projectId = params['projectId']; // Optional projectId filter

    final tasks = await _service.getTasks(
      assigneeId: userId,
      projectId: projectId,
      query: query,
      page: page,
      size: size,
    );
    return okJsonResponse(tasks.map((t) => t.toJson()).toList());
  }

  Future<Response> _assignTask(Request request) async {
    final taskId = request.params['id'];
    if (taskId == null || taskId.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for task cannot be null or empty.');
    }
    final requestBody = await request.readJsonBody();
    final assignDto = TaskAssignRequestDto.fromJson(requestBody);
    // Validation for assignDto will be added in Step 3

    final updatedTask = await _service.assignTask(taskId, assignDto.assigneeId);
    return okJsonResponse(updatedTask.toJson());
  }

  Future<Response> _changeTaskStatus(Request request) async {
    final taskId = request.params['id'];
     if (taskId == null || taskId.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for task cannot be null or empty.');
    }
    final requestBody = await request.readJsonBody();
    final statusDto = TaskStatusChangeRequestDto.fromJson(requestBody);
    // Validation for statusDto will be added in a later step for DTOs in this file if needed

    final updatedTask = await _service.changeTaskStatus(taskId, statusDto.status);
    return okJsonResponse(updatedTask.toJson());
  }

  Future<Response> _getTasksCreatedByMe(Request request) async {
    final userId = request.context['userId'] as String;
    final params = request.url.queryParameters;
    
    final pageStr = params['page'] ?? '0';
    int page;
    try {
      page = int.parse(pageStr);
      if (page < 0) throw ValidationException(message: 'Query parameter "page" must be non-negative.');
    } catch (e) {
      throw ValidationException(message: 'Query parameter "page" must be a valid integer.');
    }

    final sizeStr = params['size'] ?? '10';
    int size;
    try {
      size = int.parse(sizeStr);
      if (size <= 0) throw ValidationException(message: 'Query parameter "size" must be positive.');
      if (size > 100) size = 100; // Cap size
    } catch (e) {
      throw ValidationException(message: 'Query parameter "size" must be a valid integer.');
    }
    
    final query = params['query'];
    final projectId = params['projectId']; // Optional projectId filter

    final tasks = await _service.getTasks(
      creatorId: userId,
      projectId: projectId,
      query: query,
      page: page,
      size: size,
    );
    return okJsonResponse(tasks.map((t) => t.toJson()).toList());
  }

  Future<Response> _getTasksByProject(Request request) async {
    final projectId = request.params['projectId'];
    if (projectId == null || projectId.isEmpty) {
      throw ValidationException(message: 'Path parameter <projectId> cannot be null or empty.');
    }
    final params = request.url.queryParameters;

    final pageStr = params['page'] ?? '0';
    int page;
    try {
      page = int.parse(pageStr);
      if (page < 0) throw ValidationException(message: 'Query parameter "page" must be non-negative.');
    } catch (e) {
      throw ValidationException(message: 'Query parameter "page" must be a valid integer.');
    }

    final sizeStr = params['size'] ?? '10';
    int size;
    try {
      size = int.parse(sizeStr);
      if (size <= 0) throw ValidationException(message: 'Query parameter "size" must be positive.');
      if (size > 100) size = 100; // Cap size
    } catch (e) {
      throw ValidationException(message: 'Query parameter "size" must be a valid integer.');
    }

    final query = params['query'];
    final assigneeId = params['assigneeId']; // Optional filter

    final tasks = await _service.getTasks(
      projectId: projectId,
      assigneeId: assigneeId,
      query: query,
      page: page,
      size: size,
    );
    return okJsonResponse(tasks.map((t) => t.toJson()).toList());
  }

  Future<Response> _getTaskById(Request request) async {
    final id = request.params['id'];
    if (id == null || id.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for task cannot be null or empty.');
    }
    final userId = request.context['userId'] as String;
    final task = await _service.getTaskById(id, userId);
    if (task == null) {
      throw NotFoundException(message: 'Task with ID $id not found or user not authorized.');
    }
    return okJsonResponse(task.toJson());
  }

  Future<Response> _createTask(Request request) async {
    final taskData = await request.readJsonBody();
    final userId = request.context['userId'] as String;
    var task = Task.fromJson(taskData);
    if (task.title.isEmpty) throw ValidationException(message: 'Task title is required.');
    task = task.copyWith(creatorId: userId);

    final createdTask = await _service.createTask(task);
    return okJsonResponse(createdTask.toJson());
  }

  Future<Response> _updateTask(Request request) async {
    final id = request.params['id'];
    if (id == null || id.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for task cannot be null or empty.');
    }
    final userId = request.context['userId'] as String;
    final taskData = await request.readJsonBody();
    var task = Task.fromJson(taskData);
    if (task.title.isEmpty) throw ValidationException(message: 'Task title is required.');
    task = task.copyWith(id: id, creatorId: userId);

    final updatedTask = await _service.updateTask(id, task, userId);
    return okJsonResponse(updatedTask.toJson());
  }

  Future<Response> _deleteTask(Request request) async {
    final id = request.params['id'];
    if (id == null || id.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for task cannot be null or empty.');
    }
    final userId = request.context['userId'] as String;
    await _service.deleteTask(id, userId);
    return okJsonResponse({'message': 'Task deleted successfully'});
    }
  }
}
