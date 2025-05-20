import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../middleware/auth_middleware.dart';
import '../services/project_service.dart';
import 'package:shared/src/models/project.dart';

class ProjectRoutes {
  final ProjectService _service;
  final AuthMiddleware _authMiddleware;

  ProjectRoutes(this._service, this._authMiddleware);

  Router get router {
    final router = Router();

    router.get('/projects', _authMiddleware.middleware(_getAllProjects));
    router.get('/projects/<id>', _authMiddleware.middleware(_getProjectById));
    router.post('/projects', _authMiddleware.middleware(_createProject));
    router.put('/projects/<id>', _authMiddleware.middleware(_updateProject));
    router.delete('/projects/<id>', _authMiddleware.middleware(_deleteProject));

    return router;
  }

  Future<Response> _getAllProjects(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projects = await _service.getAllProjects(userId);
      return Response.ok(jsonEncode(projects.map((p) => p.toJson()).toList()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _getProjectById(Request request) async {
    try {
      final id = request.params['id']!;
      final userId = request.context['userId'] as String;
      final project = await _service.getProjectById(id, userId);
      if (project == null) {
        return Response.notFound('Project not found');
      }
      return Response.ok(jsonEncode(project.toJson()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _createProject(Request request) async {
    try {
      final body = await request.readAsString();
      final projectData = jsonDecode(body) as Map<String, dynamic>;
      final userId = request.context['userId'] as String;
      final project = Project(
        id: projectData['id'] as String,
        name: projectData['name'] as String,
        description: projectData['description'] as String,
        creatorId: userId,
        memberIds:
            (projectData['memberIds'] as List<dynamic>?)?.cast<String>() ?? [],
      );
      final createdProject = await _service.createProject(project);
      return Response.ok(jsonEncode(createdProject.toJson()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _updateProject(Request request) async {
    try {
      final id = request.params['id']!;
      final userId = request.context['userId'] as String;
      final body = await request.readAsString();
      final projectData = jsonDecode(body) as Map<String, dynamic>;
      final project = Project(
        id: id,
        name: projectData['name'] as String,
        description: projectData['description'] as String,
        creatorId: userId,
        memberIds:
            (projectData['memberIds'] as List<dynamic>?)?.cast<String>() ?? [],
      );
      final updatedProject = await _service.updateProject(id, project, userId);
      return Response.ok(jsonEncode(updatedProject.toJson()));
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }

  Future<Response> _deleteProject(Request request) async {
    try {
      final id = request.params['id']!;
      final userId = request.context['userId'] as String;
      await _service.deleteProject(id, userId);
      return Response.ok('Project deleted');
    } catch (e) {
      return Response.internalServerError(body: e.toString());
    }
  }
}
