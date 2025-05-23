import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:uuid/uuid.dart';
import '../middleware/auth_middleware.dart';
import '../services/project_service.dart';
import 'package:shared/models.dart' as shared_models;
import '../util/shelf_helpers.dart';
import '../exceptions/custom_exceptions.dart';

class ProjectRoutes {
  final ProjectService _projectService;
  final AuthMiddleware _authMiddleware;
  late final Router router;

  ProjectRoutes(this._projectService, this._authMiddleware) {
    final baseRouter = Router()
      ..get('/projects', _getProjects)
      ..get('/projects/<id>', _getProjectById)
      ..post('/projects', _createProject)
      ..put('/projects/<id>', _updateProject)
      ..delete('/projects/<id>', _deleteProject)
      ..get('/projects/member/<userId>', _getProjectsByMember);
    
    // Wrap the router with auth middleware using Pipeline
    final handler = Pipeline()
        .addMiddleware(_authMiddleware.middleware())
        .addHandler(baseRouter.call);
    
    // Create a new router that delegates to the pipeline
    router = Router()
      ..mount('/', handler);
  }

  Future<Response> _getProjects(Request request) async {
    try {
      final queryParams = request.url.queryParameters;
      final creatorId = queryParams['creatorId'];
      final query = queryParams['query'];
      final page = int.tryParse(queryParams['page'] ?? '0') ?? 0;
      final size = int.tryParse(queryParams['size'] ?? '10') ?? 10;

      final projects = await _projectService.getProjects(
        creatorId: creatorId,
        query: query,
        page: page,
        size: size,
      );

      return okJsonResponse(projects.map((p) => p.toJson()).toList());
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _getProjectById(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projectId = request.params['id'];
      if (projectId == null) {
        throw ValidationException(message: 'Project ID is required');
      }
      final project = await _projectService.getProjectById(projectId, userId);

      if (project == null) {
        return Response.notFound(
          jsonEncode({'error': 'Project not found'}),
          headers: {'content-type': 'application/json'},
        );
      }

      return okJsonResponse(project.toJson());
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _createProject(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projectData = await request.readJsonBody();
      final projectId = projectData['id'] as String? ?? const Uuid().v4();
      final project = shared_models.Project(
        id: projectId,
        name: projectData['name'] as String,
        description: projectData['description'] as String?,
        creatorId: userId,
        memberIds: [userId],
      );

      final createdProject = await _projectService.createProject(project);

      return okJsonResponse(createdProject.toJson());
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _updateProject(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projectId = request.params['id'];
      if (projectId == null) {
        throw ValidationException(message: 'Project ID is required');
      }
      final projectData = await request.readJsonBody();
      final project = shared_models.Project(
        id: projectId,
        name: projectData['name'] as String,
        description: projectData['description'] as String?,
        creatorId: projectData['creatorId'] as String,
        memberIds: (projectData['memberIds'] as List<dynamic>)
            .map((e) => e as String)
            .toList(),
      );

      final updatedProject =
          await _projectService.updateProject(projectId, userId, project);

      return okJsonResponse(updatedProject.toJson());
    } catch (e) {
      if (e is ForbiddenException) {
        return Response.forbidden(
          jsonEncode({'error': e.message}),
          headers: {'content-type': 'application/json'},
        );
      }
      return Response.internalServerError(
        body: jsonEncode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _deleteProject(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projectId = request.params['id'];
      if (projectId == null) {
        throw ValidationException(message: 'Project ID is required');
      }
      final success = await _projectService.deleteProject(projectId, userId);

      return okJsonResponse({'success': success});
    } catch (e) {
      if (e is ForbiddenException) {
        return Response.forbidden(
          jsonEncode({'error': e.message}),
          headers: {'content-type': 'application/json'},
        );
      }
      return Response.internalServerError(
        body: jsonEncode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _getProjectsByMember(Request request) async {
    try {
      final userId = request.params['userId'];
      if (userId == null) {
        throw ValidationException(message: 'User ID is required');
      }
      final projects = await _projectService.getProjectsByMember(userId);

      return okJsonResponse(projects.map((p) => p.toJson()).toList());
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }
}
