import 'dart:io';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as shelf_io;
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:shelf_router/shelf_router.dart';

void main() async {
  print('🚀 Testing Basic Dart Server Components...');

  try {
    // Create Simple Router to test basic functionality
    final router = Router();

    // Health check endpoint
    router.get('/health', (Request request) {
      return Response.ok('{"status": "healthy", "service": "dart-task-manager"}', 
          headers: {'Content-Type': 'application/json'});
    });

    // API endpoints matching Kotlin structure
    router.get('/api/info', (Request request) {
      return Response.ok('{"message": "Dart Task Manager API", "version": "1.0.0", "status": "running"}', 
          headers: {'Content-Type': 'application/json'});
    });

    // Mock auth endpoints (without actual implementation)
    router.post('/api/auth/register', (Request request) {
      return Response.ok('{"message": "Register endpoint available"}', 
          headers: {'Content-Type': 'application/json'});
    });

    router.post('/api/auth/login', (Request request) {
      return Response.ok('{"message": "Login endpoint available"}', 
          headers: {'Content-Type': 'application/json'});
    });

    router.post('/api/auth/google', (Request request) {
      return Response.ok('{"message": "Google login endpoint available"}', 
          headers: {'Content-Type': 'application/json'});
    });

    // Mock project endpoints
    router.get('/api/projects', (Request request) {
      return Response.ok('{"message": "Projects endpoint available", "projects": []}', 
          headers: {'Content-Type': 'application/json'});
    });

    router.post('/api/projects', (Request request) {
      return Response.ok('{"message": "Create project endpoint available"}', 
          headers: {'Content-Type': 'application/json'});
    });

    // Mock task endpoints
    router.get('/api/tasks/user', (Request request) {
      return Response.ok('{"message": "User tasks endpoint available", "tasks": []}', 
          headers: {'Content-Type': 'application/json'});
    });

    router.post('/api/tasks', (Request request) {
      return Response.ok('{"message": "Create task endpoint available"}', 
          headers: {'Content-Type': 'application/json'});
    });

    // Create Pipeline
    final handler = Pipeline()
        .addMiddleware(corsHeaders())
        .addMiddleware(logRequests())
        .addHandler(router);

    // Start Server
    final server = await shelf_io.serve(
      handler,
      '0.0.0.0',
      8080,
    );

    print('🎉 Basic server running on ${server.address.host}:${server.port}');
    print('📍 Health check: http://${server.address.host}:${server.port}/health');
    print('📍 API info: http://${server.address.host}:${server.port}/api/info');
    print('📍 Auth endpoints: http://${server.address.host}:${server.port}/api/auth/*');
    print('📍 Projects: http://${server.address.host}:${server.port}/api/projects');
    print('📍 Tasks: http://${server.address.host}:${server.port}/api/tasks/user');
    print('');
    print('🧪 All core components working! Ready for full implementation.');

    // Handle Shutdown
    ProcessSignal.sigint.watch().listen((_) async {
      print('🛑 Shutting down test server...');
      exit(0);
    });

  } catch (e, stackTrace) {
    print('❌ Failed to start test server: $e');
    print('Stack trace: $stackTrace');
    exit(1);
  }
} 