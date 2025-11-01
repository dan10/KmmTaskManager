import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../middleware/auth_middleware.dart';
import '../services/task_service.dart';
import '../utils/uuid_utils.dart';
import 'package:task_manager_shared/models.dart';
import '../util/shelf_helpers.dart';
import '../exceptions/custom_exceptions.dart';

/// Routes for project-task relationships following the pattern:
/// GET /<projectId>/tasks - Get all tasks in a project
/// POST /<projectId>/tasks - Create task in a project
class ProjectTaskRoutes {
  final TaskService _taskService;
  final AuthMiddleware _authMiddleware;
  late final Router router;

  ProjectTaskRoutes(this._taskService, this._authMiddleware) {
    final baseRouter = Router()
      ..get('/<projectId>/tasks', _getTasksByProject)
      ..post('/<projectId>/tasks', _createTaskInProject);
    
    // Wrap the router with auth middleware using Pipeline
    final handler = Pipeline()
        .addMiddleware(_authMiddleware.middleware())
        .addHandler(baseRouter.call);
    
    // Create a new router that delegates to the pipeline
    router = Router()
      ..mount('/', handler);
  }

  Future<Response> _getTasksByProject(Request request) async {
    try {
      final projectId = request.params['projectId'];
      if (projectId == null || projectId.isEmpty) {
        throw ValidationException(message: 'Project ID is required');
      }

      final params = request.url.queryParameters;
      final pageStr = params['page'] ?? '0';
      int page;
      try {
        page = int.parse(pageStr);
        if (page < 0) {
          throw ValidationException(message: 'Query parameter "page" must be non-negative.');
        }
      } catch (e) {
        throw ValidationException(message: 'Query parameter "page" must be a valid integer.');
      }

      final sizeStr = params['size'] ?? '10';
      int size;
      try {
        size = int.parse(sizeStr);
        if (size <= 0) {
          throw ValidationException(message: 'Query parameter "size" must be positive.');
        }
        if (size > 100) size = 100; // Cap size
      } catch (e) {
        throw ValidationException(message: 'Query parameter "size" must be a valid integer.');
      }

      final query = params['query'];
      final assigneeId = params['assigneeId'];

      final tasks = await _taskService.getTasks(
        projectId: projectId,
        assigneeId: assigneeId,
        query: query,
        page: page,
        size: size,
      );

      return okJsonResponse(tasks.map((t) => t.toJson()).toList());
    } catch (e) {
      rethrow; // Let error handling middleware handle all exceptions
    }
  }

  Future<Response> _createTaskInProject(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projectId = request.params['projectId'];
      
      if (projectId == null || projectId.isEmpty) {
        throw ValidationException(message: 'Project ID is required');
      }

      final taskData = await request.readJsonBody();
      
      // Use shared DTO for request validation
      late final TaskCreateRequestDto createRequest;
      try {
        createRequest = TaskCreateRequestDto.fromJson(taskData);
      } on TypeError catch (e) {
        // Handle JSON deserialization errors (missing required fields, wrong types)
        throw ValidationException(message: 'Invalid request data: ${e.toString()}');
      }
      
      // Validate the request
      if (!createRequest.isValid) {
        final errors = createRequest.validate();
        final errorMessage = errors.values.join(', ');
        throw ValidationException(message: errorMessage);
      }

      // Override projectId from URL path parameter
      final taskId = UuidUtils.generate();
      final task = TaskDto(
        id: taskId,
        title: createRequest.title,
        description: createRequest.description,
        status: TaskStatus.todo, // Default status for new tasks
        priority: createRequest.priority,
        dueDate: createRequest.dueDate,
        projectId: projectId, // Use projectId from URL
        assigneeId: createRequest.assigneeId,
        creatorId: userId,
      );

      final createdTask = await _taskService.createTask(task);
      
      return Response(
        201,
        body: jsonEncode(createdTask.toJson()),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      rethrow; // Let error handling middleware handle all exceptions
    }
  }
}

