import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../constants/api_routes.dart';
import '../middleware/auth_middleware.dart';
import '../services/task_service.dart';
import '../utils/uuid_utils.dart';
import 'package:task_manager_shared/models.dart';
import '../util/shelf_helpers.dart';
import '../exceptions/custom_exceptions.dart';

class TaskRoutes {
  final TaskService _service;
  final AuthMiddleware _authMiddleware;

  TaskRoutes(this._service, this._authMiddleware);

  Router get router {
    final baseRouter = Router();

    baseRouter.get('/', _getAllTasks);
    baseRouter.get(ApiRoutes.tasksStats, _getTaskProgress);
    baseRouter.get(ApiRoutes.tasksCreatedByMe, _getTasksCreatedByMe);
    baseRouter.get(ApiRoutes.tasksByProject, _getTasksByProject);
    baseRouter.get('/assigned/due-on', _getAssignedDueOnTasks);
    baseRouter.get(ApiRoutes.taskById, _getTaskById);
    baseRouter.post('/', _createTask);
    baseRouter.put(ApiRoutes.taskById, _updateTask);
    baseRouter.delete(ApiRoutes.taskById, _deleteTask);

    // New routes for assigning and changing status
    baseRouter.post(ApiRoutes.taskAssign, _assignTask);
    baseRouter.post(ApiRoutes.taskStatus, _changeTaskStatus);

    // Wrap the router with auth middleware using Pipeline
    final handler = Pipeline()
        .addMiddleware(_authMiddleware.middleware())
        .addHandler(baseRouter.call);
    
    // Create a new router that delegates to the pipeline
    final router = Router();
    router.mount('/', handler);
    
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

  Future<Response> _getTaskProgress(Request request) async {
    final userId = request.context['userId'] as String;
    final progress = await _service.getTaskProgress(userId);
    return okJsonResponse(progress.toJson());
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

  Future<Response> _getAssignedDueOnTasks(Request request) async {
    final userId = request.context['userId'] as String;
    final params = request.url.queryParameters;

    // Required date parameter (YYYY-MM-DD)
    final dateStr = params['date'];
    if (dateStr == null || dateStr.isEmpty) {
      throw ValidationException(message: 'Query parameter "date" is required (format: YYYY-MM-DD).');
    }

    // Optional timezone offset in minutes
    final tzOffsetStr = params['tzOffsetMinutes'] ?? '0';
    int tzOffsetMinutes;
    try {
      tzOffsetMinutes = int.parse(tzOffsetStr);
    } catch (e) {
      throw ValidationException(message: 'Query parameter "tzOffsetMinutes" must be a valid integer.');
    }

    // Parse page and size
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

    // Convert local date to UTC range
    // Local date at 00:00:00 minus offset = UTC start
    final localStart = DateTime.parse('$dateStr 00:00:00');
    final startUtc = localStart.subtract(Duration(minutes: tzOffsetMinutes));
    final endUtc = startUtc.add(const Duration(days: 1));

    final tasks = await _service.getTasksAssignedDueOn(
      userId,
      startUtc,
      endUtc,
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
    
    // Use shared DTO for request validation
    final createRequest = TaskCreateRequestDto.fromJson(taskData);
    
    // Validate the request
    if (!createRequest.isValid) {
      final errors = createRequest.validate();
      final errorMessage = errors.values.join(', ');
      throw ValidationException(message: errorMessage);
    }

    // Convert to Task model for service layer
    final taskId = UuidUtils.generate();
    final task = TaskDto(
      id: taskId,
      title: createRequest.title,
      description: createRequest.description,
      status: TaskStatus.todo, // Default status for new tasks
      priority: createRequest.priority,
      dueDate: createRequest.dueDate,
      projectId: createRequest.projectId,
      assigneeId: createRequest.assigneeId,
      creatorId: userId,
    );

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
    
    // Use shared DTO for request validation
    final updateRequest = TaskUpdateRequestDto.fromJson(taskData);
    
    // Validate the request
    if (!updateRequest.isValid) {
      final errors = updateRequest.validate();
      final errorMessage = errors.values.join(', ');
      throw ValidationException(message: errorMessage);
    }

    // Get existing task first
    final existingTask = await _service.getTaskById(id, userId);
    if (existingTask == null) {
      throw NotFoundException(message: 'Task with ID $id not found or user not authorized.');
    }

    // Create updated task by merging existing data with updates
    final task = TaskDto(
      id: id,
      title: updateRequest.title ?? existingTask.title,
      description: updateRequest.description ?? existingTask.description,
      status: updateRequest.status ?? existingTask.status,
      priority: updateRequest.priority ?? existingTask.priority,
      dueDate: updateRequest.dueDate ?? existingTask.dueDate,
      projectId: existingTask.projectId, // Project cannot be changed via update
      assigneeId: updateRequest.assigneeId ?? existingTask.assigneeId,
      creatorId: userId,
    );

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
