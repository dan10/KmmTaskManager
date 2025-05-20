import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:shared/src/models/project.dart' as shared;
import '../services/project_service.dart';
import '../middleware/auth_middleware.dart';

class ProjectRoutes {
  final ProjectService _service;

  ProjectRoutes(this._service);

  Router get router {
    final router = Router();

    router.get('/projects', _getAllProjects);
    router.get('/projects/<id>', _getProjectById);
    router.post('/projects', _createProject);
    router.put('/projects/<id>', _updateProject);
    router.delete('/projects/<id>', _deleteProject);

    return router;
  }

  Future<Response> _getAllProjects(Request request) async {
    final projects = await _service.getAllProjects();
    return Response.ok(
      jsonEncode(projects.map((p) => p.toJson()).toList()),
      headers: {'content-type': 'application/json'},
    );
  }

  Future<Response> _getProjectById(Request request) async {
    final id = request.params['id'];
    if (id == null) {
      return Response.badRequest(body: 'Project ID is required');
    }

    final project = await _service.getProjectById(id);
    if (project == null) {
      return Response.notFound('Project not found');
    }

    return Response.ok(
      jsonEncode(project.toJson()),
      headers: {'content-type': 'application/json'},
    );
  }

  Future<Response> _createProject(Request request) async {
    final body = await request.readAsString();
    final project = shared.Project.fromJson(jsonDecode(body));
    final createdProject = await _service.createProject(project);
    return Response.ok(
      jsonEncode(createdProject.toJson()),
      headers: {'content-type': 'application/json'},
    );
  }

  Future<Response> _updateProject(Request request) async {
    final id = request.params['id'];
    if (id == null) {
      return Response.badRequest(body: 'Project ID is required');
    }

    final body = await request.readAsString();
    final project = shared.Project.fromJson(jsonDecode(body));
    if (project.id != id) {
      return Response.badRequest(body: 'Project ID mismatch');
    }

    final updatedProject = await _service.updateProject(project);
    return Response.ok(
      jsonEncode(updatedProject.toJson()),
      headers: {'content-type': 'application/json'},
    );
  }

  Future<Response> _deleteProject(Request request) async {
    final id = request.params['id'];
    if (id == null) {
      return Response.badRequest(body: 'Project ID is required');
    }

    await _service.deleteProject(id);
    return Response.ok('Project deleted');
  }
}
