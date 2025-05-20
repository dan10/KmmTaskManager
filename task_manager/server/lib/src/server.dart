import 'dart:io';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as shelf_io;
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:dotenv/dotenv.dart';

import 'routes/auth_routes.dart';
import 'routes/task_routes.dart';
import 'routes/project_routes.dart';
import 'middleware/auth_middleware.dart';

class Server {
  final int port;
  final String host;
  final Router _router;
  final _authMiddleware = AuthMiddleware();

  Server({required this.port, required this.host}) : _router = Router() {
    _setupRoutes();
  }

  void _setupRoutes() {
    // Auth routes
    _router.post('/auth/login', _authMiddleware.handle(_handleLogin));
    _router.post('/auth/register', _authMiddleware.handle(_handleRegister));
    _router.post('/auth/logout', _authMiddleware.handle(_handleLogout));

    // Task routes
    _router.get('/tasks', _authMiddleware.handle(_handleGetTasks));
    _router.post('/tasks', _authMiddleware.handle(_handleCreateTask));
    _router.put('/tasks/<id>', _authMiddleware.handle(_handleUpdateTask));
    _router.delete('/tasks/<id>', _authMiddleware.handle(_handleDeleteTask));

    // Project routes
    _router.get('/projects', _authMiddleware.handle(_handleGetProjects));
    _router.post('/projects', _authMiddleware.handle(_handleCreateProject));
    _router.put('/projects/<id>', _authMiddleware.handle(_handleUpdateProject));
    _router.delete(
      '/projects/<id>',
      _authMiddleware.handle(_handleDeleteProject),
    );
  }

  Future<Response> _handleLogin(Request request) async {
    // TODO: Implement login logic
    return Response.ok('Login endpoint');
  }

  Future<Response> _handleRegister(Request request) async {
    // TODO: Implement register logic
    return Response.ok('Register endpoint');
  }

  Future<Response> _handleLogout(Request request) async {
    // TODO: Implement logout logic
    return Response.ok('Logout endpoint');
  }

  Future<Response> _handleGetTasks(Request request) async {
    // TODO: Implement get tasks logic
    return Response.ok('Get tasks endpoint');
  }

  Future<Response> _handleCreateTask(Request request) async {
    // TODO: Implement create task logic
    return Response.ok('Create task endpoint');
  }

  Future<Response> _handleUpdateTask(Request request) async {
    // TODO: Implement update task logic
    return Response.ok('Update task endpoint');
  }

  Future<Response> _handleDeleteTask(Request request) async {
    // TODO: Implement delete task logic
    return Response.ok('Delete task endpoint');
  }

  Future<Response> _handleGetProjects(Request request) async {
    // TODO: Implement get projects logic
    return Response.ok('Get projects endpoint');
  }

  Future<Response> _handleCreateProject(Request request) async {
    // TODO: Implement create project logic
    return Response.ok('Create project endpoint');
  }

  Future<Response> _handleUpdateProject(Request request) async {
    // TODO: Implement update project logic
    return Response.ok('Update project endpoint');
  }

  Future<Response> _handleDeleteProject(Request request) async {
    // TODO: Implement delete project logic
    return Response.ok('Delete project endpoint');
  }

  Future<void> start() async {
    final handler = Pipeline()
        .addMiddleware(logRequests())
        .addMiddleware(corsHeaders())
        .addHandler(_router);

    final server = await shelf_io.serve(handler, host, port);

    print('Server running on ${server.address.host}:${server.port}');
  }
}
