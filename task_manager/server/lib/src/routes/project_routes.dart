import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import '../services/project_service.dart';
import '../middleware/auth_middleware.dart';

class ProjectRoutes {
  final ProjectService _projectService;

  ProjectRoutes(this._projectService);

  Router get router {
    final router = Router();

    router.get('/', _getProjects);
    router.get('/<id>', _getProject);
    router.post('/', _createProject);
    router.put('/<id>', _updateProject);
    router.delete('/<id>', _deleteProject);
    router.post('/<id>/members', _addMember);
    router.delete('/<id>/members/<userId>', _removeMember);

    return router;
  }

  Future<Response> _getProjects(Request request) async {
    try {
      final userId = request.context['userId'] as String;
      final projects = await _projectService.getProjects(userId);

      return Response.ok(
        json.encode(projects.map((p) => p.toJson()).toList()),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _getProject(Request request) async {
    try {
      final projectId = request.params['id'];
      final userId = request.context['userId'] as String;

      final project = await _projectService.getProject(projectId, userId);
      if (project == null) {
        return Response.notFound('Project not found');
      }

      return Response.ok(
        json.encode(project.toJson()),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _createProject(Request request) async {
    try {
      final body = await request.readAsString();
      final data = json.decode(body) as Map<String, dynamic>;
      final userId = request.context['userId'] as String;

      final project = await _projectService.createProject(
        name: data['name'] as String,
        description: data['description'] as String,
        creatorId: userId,
      );

      return Response.ok(
        json.encode(project.toJson()),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _updateProject(Request request) async {
    try {
      final projectId = request.params['id'];
      final body = await request.readAsString();
      final data = json.decode(body) as Map<String, dynamic>;
      final userId = request.context['userId'] as String;

      final project = await _projectService.updateProject(
        projectId,
        userId,
        name: data['name'] as String?,
        description: data['description'] as String?,
      );

      if (project == null) {
        return Response.notFound('Project not found');
      }

      return Response.ok(
        json.encode(project.toJson()),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _deleteProject(Request request) async {
    try {
      final projectId = request.params['id'];
      final userId = request.context['userId'] as String;

      final success = await _projectService.deleteProject(projectId, userId);
      if (!success) {
        return Response.notFound('Project not found');
      }

      return Response.ok('Project deleted successfully');
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _addMember(Request request) async {
    try {
      final projectId = request.params['id'];
      final body = await request.readAsString();
      final data = json.decode(body) as Map<String, dynamic>;
      final userId = request.context['userId'] as String;

      final success = await _projectService.addMember(
        projectId,
        userId,
        data['memberId'] as String,
      );

      if (!success) {
        return Response.notFound('Project not found');
      }

      return Response.ok('Member added successfully');
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }

  Future<Response> _removeMember(Request request) async {
    try {
      final projectId = request.params['id'];
      final memberId = request.params['userId'];
      final userId = request.context['userId'] as String;

      final success = await _projectService.removeMember(
        projectId,
        userId,
        memberId,
      );

      if (!success) {
        return Response.notFound('Project not found');
      }

      return Response.ok('Member removed successfully');
    } catch (e) {
      return Response.badRequest(
        body: json.encode({'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  }
}
