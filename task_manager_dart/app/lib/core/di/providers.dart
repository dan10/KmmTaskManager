import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import '../../data/services/auth_service.dart';
import '../../data/services/task_api_service.dart';
import '../../data/services/project_api_service.dart';
import '../../data/sources/local/secure_storage.dart';
import '../constants/api_constants.dart';

// Feature providers
import '../../features/auth/di/providers.dart' as auth_di;
import '../../features/projects/di/providers.dart' as projects_di;
import '../../features/tasks/di/providers.dart' as tasks_di;

class DependencyProviders {
  static List<SingleChildWidget> get providers => [
        // Core services
        Provider<SecureStorage>(
          create: (_) => SecureStorage(),
        ),

        // API Services
        Provider<AuthApiService>(
          create: (_) =>
              AuthApiServiceImpl(
                baseUrl: ApiConstants.baseUrl,
              ),
        ),

        ProxyProvider<SecureStorage, TaskApiService>(
          update: (_, secureStorage, __) => TaskApiServiceImpl(secureStorage),
        ),

        ProxyProvider<SecureStorage, ProjectApiService>(
          update: (_, secureStorage, __) => ProjectApiService(secureStorage),
        ),

        // Feature providers
        ...auth_di.providers,
        ...projects_di.providers,
        ...tasks_di.providers,
      ];
} 