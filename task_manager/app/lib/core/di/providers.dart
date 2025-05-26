import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import '../../data/repositories/auth_repository.dart';
import '../../data/repositories/task_repository.dart';
import '../../data/repositories/project_repository.dart';
import '../../data/services/auth_service.dart';
import '../../data/services/task_api_service.dart';
import '../../data/services/project_api_service.dart';
import '../../data/sources/local/secure_storage.dart';
import '../../presentation/viewmodels/auth_viewmodel.dart';
import '../../presentation/viewmodels/login_viewmodel.dart';
import '../../presentation/viewmodels/register_viewmodel.dart';
import '../../presentation/viewmodels/task_list_viewmodel.dart';
import '../../presentation/viewmodels/task_detail_viewmodel.dart';
import '../../presentation/viewmodels/task_create_edit_viewmodel.dart';
import '../../presentation/viewmodels/project_list_viewmodel.dart';
import '../../presentation/viewmodels/project_detail_viewmodel.dart';
import '../../presentation/viewmodels/project_create_edit_viewmodel.dart';
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

        ProxyProvider<SecureStorage, TaskApiService>(
          update: (_, secureStorage, __) => TaskApiServiceImpl(secureStorage),
        ),

        ProxyProvider<SecureStorage, ProjectApiService>(
          update: (_, secureStorage, __) => ProjectApiService(secureStorage),
        ),

        // Repositories
        ProxyProvider2<AuthApiService, SecureStorage, AuthRepository>(
          update: (_, apiService, secureStorage, __) =>
              AuthRepositoryImpl(
                apiService: apiService,
                secureStorage: secureStorage,
              ),
        ),

        ProxyProvider<TaskApiService, TaskRepository>(
          update: (_, apiService, __) => TaskRepositoryImpl(apiService),
        ),

        ProxyProvider<ProjectApiService, ProjectRepository>(
          update: (_, apiService, __) => ProjectRepositoryImpl(apiService),
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

        ChangeNotifierProxyProvider<TaskRepository, TaskListViewModel>(
          create: (context) {
            final taskRepository = Provider.of<TaskRepository>(context, listen: false);
            return TaskListViewModel(taskRepository);
          },
          update: (_, taskRepository, previous) {
            return TaskListViewModel(taskRepository);
          },
        ),

        ChangeNotifierProxyProvider<TaskRepository, TaskDetailViewModel>(
          create: (context) {
            final taskRepository = Provider.of<TaskRepository>(context, listen: false);
            return TaskDetailViewModel(taskRepository);
          },
          update: (_, taskRepository, previous) {
            return TaskDetailViewModel(taskRepository);
          },
        ),

        ChangeNotifierProxyProvider<TaskRepository, TaskCreateEditViewModel>(
          create: (context) {
            final taskRepository = Provider.of<TaskRepository>(context, listen: false);
            return TaskCreateEditViewModel(taskRepository);
          },
          update: (_, taskRepository, previous) {
            return TaskCreateEditViewModel(taskRepository);
          },
        ),

        ChangeNotifierProxyProvider<ProjectRepository, ProjectListViewModel>(
          create: (context) {
            final projectRepository = Provider.of<ProjectRepository>(context, listen: false);
            return ProjectListViewModel(projectRepository);
          },
          update: (_, projectRepository, previous) {
            return ProjectListViewModel(projectRepository);
          },
        ),

        ChangeNotifierProxyProvider<ProjectRepository, ProjectDetailViewModel>(
          create: (context) {
            final projectRepository = Provider.of<ProjectRepository>(context, listen: false);
            return ProjectDetailViewModel(projectRepository);
          },
          update: (_, projectRepository, previous) {
            return ProjectDetailViewModel(projectRepository);
          },
        ),

        ChangeNotifierProxyProvider<ProjectRepository, ProjectCreateEditViewModel>(
          create: (context) {
            final projectRepository = Provider.of<ProjectRepository>(context, listen: false);
            return ProjectCreateEditViewModel(projectRepository);
          },
          update: (_, projectRepository, previous) {
            return ProjectCreateEditViewModel(projectRepository);
          },
        ),
      ];
} 