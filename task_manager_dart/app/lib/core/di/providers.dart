import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import '../data/local/secure_storage.dart';
import '../constants/api_constants.dart';
import '../network/api_client.dart';
import '../auth/auth_state.dart';

// Feature providers
import '../../features/auth/di/providers.dart' as auth_di;
import '../../features/calendar/di/providers.dart' as calendar_di;
import '../../features/projects/di/providers.dart' as projects_di;
import '../../features/tasks/di/providers.dart' as tasks_di;

class DependencyProviders {
  static List<SingleChildWidget> get providers => [
        // Core services only
        Provider<SecureStorage>(
          create: (_) => SecureStorage(),
        ),

        // Auth state notifier used to refresh router on auth changes
        ChangeNotifierProxyProvider<SecureStorage, AuthState>(
          create: (context) => AuthState(Provider.of<SecureStorage>(context, listen: false)),
          update: (context, secureStorage, previous) => previous ?? AuthState(secureStorage),
        ),

        // Singleton ApiClient - shared by all authenticated services
        ProxyProvider2<SecureStorage, AuthState, ApiClient>(
          update: (context, secureStorage, authState, _) {
            // Parse the base URL to extract host, port, and scheme
            final uri = Uri.parse(ApiConstants.baseUrl);
            
            // Create singleton ApiClient with proper configuration and 401 handling
            return ApiClient(
              host: uri.host,
              port: uri.port,
              scheme: uri.scheme,
              authTokenProvider: () => secureStorage.getToken(),
              unauthorizedHandler: () async {
                // Clear token and notify auth state on 401 - router will redirect
                await authState.logout();
              },
              enableLogging: true,
            );
          },
        ),

        // Feature-specific providers (services, repositories, etc.)
        ...auth_di.providers,
        ...tasks_di.providers,
        ...projects_di.providers,
        ...calendar_di.providers,
      ];
} 