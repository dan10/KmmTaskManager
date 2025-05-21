import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../middleware/auth_middleware.dart';
import '../services/project_service.dart';
import 'package:shared/models.dart';
import '../util/shelf_helpers.dart';
import '../dto/project/project_assign_request_dto.dart';
import '../dto/user_public_response_dto.dart';
import '../exceptions/custom_exceptions.dart'; // Import new exceptions
import '../dto/error_response_dto.dart';      // Import ErrorResponseDto

class ProjectRoutes {
  final ProjectService _service;
  final AuthMiddleware _authMiddleware;

  ProjectRoutes(this._service, this._authMiddleware);

  Router get router {
    final router = Router();

    router.get('/projects', _authMiddleware.middleware(_getAllProjects));
    router.get('/projects/all', _authMiddleware.middleware(_getAllSystemProjects));
    router.get('/projects/<id>', _authMiddleware.middleware(_getProjectById));
    router.post('/projects', _authMiddleware.middleware(_createProject));
    router.put('/projects/<id>', _authMiddleware.middleware(_updateProject));
    router.delete('/projects/<id>', _authMiddleware.middleware(_deleteProject));

    // New routes for assigning/removing users
    router.post('/projects/<id>/assign', _authMiddleware.middleware(_assignUserToProject));
    router.delete('/projects/<id>/assign/<userId>', _authMiddleware.middleware(_removeUserFromProject));

    // Routes for getting users by project and projects by user
    router.get('/projects/<id>/users', _authMiddleware.middleware(_getUsersByProject));
    router.get('/projects/user/<userId>', _authMiddleware.middleware(_getProjectsByUser));

    return router;
  }

  Future<Response> _getAllProjects(Request request) async {
    // No try-catch needed here if exceptions are AppExceptions or FormatException for page/size
    // The middleware will handle them.
    // FormatException for page/size parsing needs to be handled specifically or route needs to ensure it.
    // Let's assume page/size parsing is robust or throws FormatException.
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

    final projects = await _service.getAllProjects(userId, page, size, query);
    return okJsonResponse(projects.map((p) => p.toJson()).toList());
  }

  Future<Response> _getUsersByProject(Request request) async {
    final projectId = request.params['id'];
    if (projectId == null || projectId.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for project cannot be null or empty.');
    }
    final users = await _service.getUsersByProject(projectId);
    return okJsonResponse(users.map((u) => u.toJson()).toList());
  }

  Future<Response> _getProjectsByUser(Request request) async {
    final userId = request.params['userId'];
     if (userId == null || userId.isEmpty) {
      throw ValidationException(message: 'Path parameter <userId> cannot be null or empty.');
    }
    final projects = await _service.getProjectsByUser(userId);
    return okJsonResponse(projects.map((p) => p.toJson()).toList());
  }

  Future<Response> _assignUserToProject(Request request) async {
    final projectId = request.params['id'];
    if (projectId == null || projectId.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for project cannot be null or empty.');
    }
    final requestBody = await request.readJsonBody();
    final assignDto = ProjectAssignRequestDto.fromJson(requestBody);
    assignDto.validateOrThrow(); // Call validation method from DTO

    final result = await _service.assignUserToProject(projectId, assignDto.userId);
    return okJsonResponse(result);
  }

  Future<Response> _removeUserFromProject(Request request) async {
    final projectId = request.params['id'];
    if (projectId == null || projectId.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for project cannot be null or empty.');
    }
    final userIdToRemove = request.params['userId'];
    if (userIdToRemove == null || userIdToRemove.isEmpty) {
      throw ValidationException(message: 'Path parameter <userId> for user to remove cannot be null or empty.');
    }

    final success = await _service.removeUserFromProject(projectId, userIdToRemove);
    if (success) {
      return Response(204); // No Content
    } else {
      throw NotFoundException(message: 'Failed to remove user: User not found on project or project itself not found.');
    }
  }


  Future<Response> _getAllSystemProjects(Request request) async {
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
    final projects = await _service.getAllSystemProjects(page, size, query);
    return okJsonResponse(projects.map((p) => p.toJson()).toList());
  }

  Future<Response> _getProjectById(Request request) async {
    final id = request.params['id'];
    if (id == null || id.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for project cannot be null or empty.');
    }
    final userId = request.context['userId'] as String;
    final project = await _service.getProjectById(id, userId);
    if (project == null) {
      throw NotFoundException(message: 'Project with ID $id not found or user not authorized.');
    }
    return okJsonResponse(project.toJson());
  }

  Future<Response> _createProject(Request request) async {
    final projectData = await request.readJsonBody();
    final userId = request.context['userId'] as String;
    var project = Project.fromJson(projectData);
    if (project.name.isEmpty) throw ValidationException(message: 'Project name is required.');
    project = project.copyWith(creatorId: userId);

    final createdProject = await _service.createProject(project);
    return okJsonResponse(createdProject.toJson());
  }

  Future<Response> _updateProject(Request request) async {
    final id = request.params['id'];
     if (id == null || id.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for project cannot be null or empty.');
    }
    final userId = request.context['userId'] as String;
    final projectData = await request.readJsonBody();
    var project = Project.fromJson(projectData);
    if (project.name.isEmpty) throw ValidationException(message: 'Project name is required.');
    project = project.copyWith(id: id, creatorId: userId);

    final updatedProject = await _service.updateProject(id, project, userId);
    return okJsonResponse(updatedProject.toJson());
  }

  Future<Response> _deleteProject(Request request) async {
    final id = request.params['id'];
    if (id == null || id.isEmpty) {
      throw ValidationException(message: 'Path parameter <id> for project cannot be null or empty.');
    }
    final userId = request.context['userId'] as String;
    await _service.deleteProject(id, userId);
    return okJsonResponse({'message': 'Project deleted successfully'});
    }
  }
}
