import 'package:provider/provider.dart';
import 'config/app_config.dart'; // Import AppConfig
import 'data/database.dart';
import 'repositories/auth_repository.dart';
import 'repositories/task_repository.dart';
import 'repositories/project_repository.dart';
import 'services/auth_service.dart';
import 'services/task_service.dart';
import 'services/project_service.dart';
import 'services/jwt_service.dart';

class Providers {
  static List<Provider> get providers => [
        Provider<AppConfig>( // Add AppConfig provider
          create: (_) => AppConfig(),
        ),
        Provider<Database>(
          create: (context) => Database(context.read<AppConfig>()), // Pass AppConfig
        ),
        Provider<JwtService>(
          create: (context) => JwtService(context.read<AppConfig>()), // Pass AppConfig
        ),
        Provider<AuthRepository>(
          create: (context) => AuthRepository(context.read<Database>()),
        ),
        Provider<TaskRepository>(
          create: (context) => TaskRepository(context.read<Database>()),
        ),
        Provider<ProjectRepository>(
          create: (context) => ProjectRepository(context.read<Database>()),
        ),
        Provider<AuthService>(
          create: (context) => AuthService(
            context.read<AuthRepository>(),
            context.read<JwtService>(),
          ),
        ),
        Provider<TaskService>(
          create: (context) => TaskService(
            context.read<TaskRepository>(),
          ),
        ),
        Provider<ProjectService>(
          create: (context) => ProjectService(
            context.read<ProjectRepository>(),
          ),
        ),
      ];
}
