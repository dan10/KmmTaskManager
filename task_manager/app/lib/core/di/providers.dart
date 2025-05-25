import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import '../../data/repositories/auth_repository.dart';
import '../../data/services/auth_service.dart';
import '../../data/sources/local/secure_storage.dart';
import '../../presentation/viewmodels/auth_viewmodel.dart';
import '../../presentation/viewmodels/login_viewmodel.dart';
import '../../presentation/viewmodels/register_viewmodel.dart';
import '../constants/api_constants.dart';

class DependencyProviders {
  static List<SingleChildWidget> get providers =>
      [
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

        // Repositories
        ProxyProvider2<AuthApiService, SecureStorage, AuthRepository>(
          update: (_, apiService, secureStorage, __) =>
              AuthRepositoryImpl(
                apiService: apiService,
                secureStorage: secureStorage,
              ),
        ),

        // ViewModels
        ChangeNotifierProxyProvider<AuthRepository, AuthViewModel>(
          create: (_) => AuthViewModel(null),
          update: (_, authRepository, previous) {
            if (previous != null) {
              previous.updateRepository(authRepository);
              return previous;
            }
            return AuthViewModel(authRepository);
          },
        ),

        ChangeNotifierProxyProvider<AuthRepository, LoginViewModel>(
          create: (_) => LoginViewModel(null),
          update: (_, authRepository, previous) {
            if (previous != null) {
              previous.updateRepository(authRepository);
              return previous;
            }
            return LoginViewModel(authRepository);
          },
        ),

        ChangeNotifierProxyProvider<AuthRepository, RegisterViewModel>(
          create: (_) => RegisterViewModel(null),
          update: (_, authRepository, previous) {
            if (previous != null) {
              previous.updateRepository(authRepository);
              return previous;
            }
            return RegisterViewModel(authRepository);
          },
        ),
      ];
} 