import 'dart:io';
import 'dart:io';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as shelf_io;
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:shelf_router/shelf_router.dart';

// Configuration
import '../lib/src/config/app_config.dart';

// Data
import '../lib/src/data/database.dart';

// Repositories
import '../lib/src/repositories/auth_repository.dart';
import '../lib/src/repositories/task_repository.dart';
import '../lib/src/repositories/project_repository.dart';

// Services
import '../lib/src/services/jwt_service.dart';
import '../lib/src/services/auth_service.dart';
import '../lib/src/services/task_service.dart';
import '../lib/src/services/project_service.dart';

// Middleware
import '../lib/src/middleware/auth_middleware.dart';
import '../lib/src/middleware/error_handling_middleware.dart'; // Import error handler

// Routes
import '../lib/src/routes/auth_routes.dart';
import '../lib/src/routes/task_routes.dart';
import '../lib/src/routes/project_routes.dart';

void main() async {
  // 1. Initialize Configuration
  final appConfig = AppConfig();

  // 2. Initialize Database (Database constructor now takes AppConfig)
  final database = Database(appConfig);
  await database.connect(); // Instance method connect

  // 3. Initialize Services & Repositories
  // JWT Service
  final jwtService = JwtService(appConfig);

  // Auth
  final authRepository = AuthRepository(database);
  final authService = AuthService(authRepository, jwtService);

  // Task
  final taskRepository = TaskRepository(database);
  final taskService = TaskService(taskRepository);

  // Project
  final projectRepository = ProjectRepository(database);
  final projectService = ProjectService(projectRepository);

  // 4. Initialize Middleware
  final authMiddleware = AuthMiddleware(jwtService);

  // 5. Create Router and Mount Routes
  final router = Router()
    ..mount('/api/auth', AuthRoutes(authService, jwtService).router)
    ..mount('/api/tasks', TaskRoutes(taskService, authMiddleware).router)
    ..mount('/api/projects', ProjectRoutes(projectService, authMiddleware).router);

  // 6. Create Pipeline
  final handler = Pipeline()
      .addMiddleware(corsHeaders())      // Apply CORS headers first
      .addMiddleware(logRequests())      // Then log requests
      .addMiddleware(errorHandlingMiddleware()) // Add error handling middleware
      .addHandler(router);

  // 7. Start Server
  final server = await shelf_io.serve(
    handler,
    appConfig.host, // Use host from AppConfig
    appConfig.port, // Use port from AppConfig
  );

  print('Server running on ${server.address.host}:${server.port} with log level ${appConfig.logLevel}');

  // 8. Handle Shutdown
  ProcessSignal.sigint.watch().listen((_) async {
    print('Shutting down...');
    await database.disconnect(); // Instance method disconnect
    exit(0);
  });
}
