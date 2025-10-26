import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import '../../data/sources/local/secure_storage.dart';
import '../constants/api_constants.dart';
import '../network/api_client.dart';

// Feature providers
import '../../features/auth/di/providers.dart' as auth_di;
import '../../features/projects/di/providers.dart' as projects_di;
import '../../features/tasks/di/providers.dart' as tasks_di;

class DependencyProviders {
  static List<SingleChildWidget> get providers => [
        // Core services only
        Provider<SecureStorage>(
          create: (_) => SecureStorage(),
        ),

        // Singleton ApiClient - shared by all authenticated services
        ProxyProvider<SecureStorage, ApiClient>(
          update: (context, secureStorage, __) {
            // Parse the base URL to extract host, port, and scheme
            final uri = Uri.parse(ApiConstants.baseUrl);
            
            // Create singleton ApiClient with proper configuration and 401 handling
            return ApiClient(
              host: uri.host,
              port: uri.port,
              scheme: uri.scheme,
              authTokenProvider: () => secureStorage.getToken(),
              unauthorizedHandler: () async {
                // Clear token on 401 - this will trigger auth state change
                // and the router will automatically redirect to login
                await secureStorage.deleteToken();
              },
              enableLogging: true,
            );
          },
        ),

        // Feature-specific providers (services, repositories, etc.)
        ...auth_di.providers,
        ...tasks_di.providers,
        ...projects_di.providers,
      ];
} 