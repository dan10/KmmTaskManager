import 'dart:io';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as shelf_io;
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:dotenv/dotenv.dart';
import 'package:provider/provider.dart';
import '../lib/src/providers.dart';
import '../lib/src/routes/auth_routes.dart';
import '../lib/src/routes/task_routes.dart';
import '../lib/src/routes/project_routes.dart';
import '../lib/src/data/database.dart';

void main() async {
  // Load environment variables
  load();

  // Initialize database
  await Database.connect();

  // Create router
  final router = Router()
    ..mount('/api/auth', AuthRoutes().router)
    ..mount('/api/tasks', TaskRoutes().router)
    ..mount('/api/projects', ProjectRoutes().router);

  // Create pipeline
  final handler = Pipeline()
      .addMiddleware(corsHeaders())
      .addMiddleware(logRequests())
      .addHandler(router);

  // Start server
  final port = int.parse(env['PORT'] ?? '8080');
  final host = env['HOST'] ?? '0.0.0.0';

  final server = await shelf_io.serve(
    handler,
    host,
    port,
  );

  print('Server running on ${server.address.host}:${server.port}');

  // Handle shutdown
  ProcessSignal.sigint.watch().listen((_) async {
    print('Shutting down...');
    await Database.disconnect();
    exit(0);
  });
}
