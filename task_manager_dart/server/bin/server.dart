import 'dart:io';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as shelf_io;
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:prometheus_client_shelf/shelf_handler.dart';

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
import '../lib/src/services/metrics_service.dart';

// Middleware
import '../lib/src/middleware/auth_middleware.dart';
// import '../lib/src/middleware/error_handling_middleware.dart'; // Import error handler - temporarily disabled

// Routes
import '../lib/src/routes/auth_routes.dart';
import '../lib/src/routes/task_routes.dart';
import '../lib/src/routes/project_routes.dart';

void main() async {
  // 1. Initialize Configuration
  final appConfig = AppConfig();

  // 2. Initialize Metrics Service
  final metricsService = MetricsService();
  metricsService.initialize();

  // 3. Initialize Database (Database constructor now takes AppConfig)
  final database = Database(appConfig);
  await database.connect(); // Instance method connect

  // 4. Initialize Services & Repositories
  // JWT Service
  final jwtService = JwtService(appConfig);

  // Auth
  final authRepository = AuthRepository(database.connection);
  final authService = AuthServiceImpl(authRepository, jwtService, appConfig);

  // Task
  final taskRepository = TaskRepository(database.connection);
  final taskService = TaskServiceImpl(taskRepository);

  // Project
  final projectRepository = ProjectRepositoryImpl(database.connection);
  final projectService = ProjectServiceImpl(projectRepository);

  // 5. Initialize Middleware
  final authMiddleware = AuthMiddleware(jwtService);

  // 6. Create Router and Mount Routes
  final router = Router()
    ..mount('/api/auth', AuthRoutes(authService, jwtService).router)
    ..mount('/api/tasks', TaskRoutes(taskService, authMiddleware).router)
    ..mount('/api/projects', ProjectRoutes(projectService, authMiddleware).router)
    ..get('/metrics', prometheusHandler(metricsService.registry))
    ..get('/health', (Request request) => Response.ok('OK'));

  // 7. Create Pipeline
  final handler = Pipeline()
      .addMiddleware(corsHeaders())      // Apply CORS headers first
      // .addMiddleware(logRequests())      // Then log requests - temporarily disabled
      // .addMiddleware(errorHandlingMiddleware()) // Add error handling middleware - temporarily disabled
      .addHandler(router);

  // 8. Start Server
  final server = await shelf_io.serve(
    handler,
    appConfig.host, // Use host from AppConfig
    appConfig.port, // Use port from AppConfig
  );

  print('Server running on ${server.address.host}:${server.port} with log level ${appConfig.logLevel}');
  print('Metrics endpoint: http://${server.address.host}:${server.port}/metrics');
  print('Health endpoint: http://${server.address.host}:${server.port}/health');

  // 9. Handle Shutdown
  ProcessSignal.sigint.watch().listen((_) async {
    print('Shutting down...');
    await database.disconnect(); // Instance method disconnect
    exit(0);
  });
}
