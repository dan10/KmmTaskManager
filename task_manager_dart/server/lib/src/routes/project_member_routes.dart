import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../middleware/auth_middleware.dart';
import '../services/project_service.dart';
import '../util/shelf_helpers.dart';
import '../exceptions/custom_exceptions.dart';

/// Routes for project member management following the pattern:
/// GET /<projectId>/users - Get all members in a project
/// POST /<projectId>/assign - Add member to project
/// DELETE /<projectId>/assign/<userId> - Remove member from project
class ProjectMemberRoutes {
  final ProjectServiceImpl _projectService;
  final AuthMiddleware _authMiddleware;
  late final Router router;

  ProjectMemberRoutes(this._projectService, this._authMiddleware) {
    final baseRouter = Router()
      ..get('/<projectId>/users', _getUsersByProject)
      ..post('/<projectId>/assign', _assignUserToProject)
      ..delete('/<projectId>/assign/<userId>', _removeUserFromProject);
    
    // Wrap the router with auth middleware using Pipeline
    final handler = Pipeline()
        .addMiddleware(_authMiddleware.middleware())
        .addHandler(baseRouter.call);
    
    // Create a new router that delegates to the pipeline
    router = Router()
      ..mount('/', handler);
  }

  Future<Response> _getUsersByProject(Request request) async {
    try {
      final projectId = request.params['projectId'];
      if (projectId == null || projectId.isEmpty) {
        throw ValidationException(message: 'Project ID is required');
      }

      final users = await _projectService.getUsersByProject(projectId);
      return okJsonResponse(users.map((u) => u.toJson()).toList());
    } catch (e) {
      rethrow; // Let error handling middleware handle all exceptions
    }
  }

  Future<Response> _assignUserToProject(Request request) async {
    try {
      final creatorId = request.context['userId'] as String;
      final projectId = request.params['projectId'];
      if (projectId == null || projectId.isEmpty) {
        throw ValidationException(message: 'Project ID is required');
      }

      final requestBody = await request.readJsonBody();
      final userId = requestBody['userId'] as String?;
      if (userId == null || userId.isEmpty) {
        throw ValidationException(message: 'User ID is required in request body');
      }

      final assignment = await _projectService.assignUserToProject(
        projectId,
        userId,
        creatorId,
      );

      return Response(
        201,
        body: jsonEncode(assignment),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      rethrow; // Let error handling middleware handle all exceptions
    }
  }

  Future<Response> _removeUserFromProject(Request request) async {
    try {
      final creatorId = request.context['userId'] as String;
      final projectId = request.params['projectId'];
      final userId = request.params['userId'];

      if (projectId == null || projectId.isEmpty) {
        throw ValidationException(message: 'Project ID is required');
      }
      if (userId == null || userId.isEmpty) {
        throw ValidationException(message: 'User ID is required');
      }

      final removed = await _projectService.removeUserFromProject(
        projectId,
        userId,
        creatorId,
      );

      if (removed) {
        return Response(204); // No Content
      } else {
        throw NotFoundException(message: 'Assignment not found');
      }
    } catch (e) {
      rethrow; // Let error handling middleware handle all exceptions
    }
  }
}

