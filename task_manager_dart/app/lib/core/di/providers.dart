import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import '../../data/repositories/auth_repository.dart';
import '../../data/repositories/task_repository.dart' as old_task;
import '../../data/repositories/project_repository.dart';
import '../../data/services/auth_service.dart';
import '../../data/services/task_api_service.dart';
import '../../data/services/project_api_service.dart';
import '../../data/sources/local/secure_storage.dart';
import '../../presentation/providers/auth_provider.dart';
import '../../presentation/viewmodels/auth_viewmodel.dart';
import '../../presentation/viewmodels/login_viewmodel.dart';
import '../../presentation/viewmodels/project_create_edit_viewmodel.dart';
import '../../presentation/viewmodels/project_detail_viewmodel.dart';
import '../../presentation/viewmodels/project_list_viewmodel.dart';
import '../../presentation/viewmodels/project_viewmodel.dart';
import '../../presentation/viewmodels/register_viewmodel.dart';
import '../constants/api_constants.dart';

// New feature-based imports
import '../../features/tasks/domain/repositories/task_repository.dart';
import '../../features/tasks/data/repositories/task_repository_impl.dart';
import '../../features/tasks/domain/usecases/get_tasks_usecase.dart';
import '../../features/tasks/domain/usecases/get_task_progress_usecase.dart';
import '../../features/tasks/domain/usecases/update_task_status_usecase.dart';
import '../../features/tasks/domain/usecases/delete_task_usecase.dart';
import '../../features/tasks/presentation/viewmodels/tasks_viewmodel.dart';

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

        // Repositories
        ProxyProvider2<AuthApiService, SecureStorage, AuthRepository>(
          update: (_, apiService, secureStorage, __) =>
              AuthRepositoryImpl(
                apiService: apiService,
                secureStorage: secureStorage,
              ),
        ),

        // Old task repository for backward compatibility
        ProxyProvider<TaskApiService, old_task.TaskRepository>(
          update: (_, apiService, __) => old_task.TaskRepositoryImpl(apiService),
        ),

        // New feature-based task repository
        ProxyProvider<TaskApiService, TaskRepository>(
          update: (_, apiService, __) => TaskRepositoryImpl(apiService),
        ),

        ProxyProvider<ProjectApiService, ProjectRepository>(
          update: (_, apiService, __) => ProjectRepositoryImpl(apiService),
        ),

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

        ChangeNotifierProxyProvider<AuthViewModel, AuthProvider>(
          create: (context) {
            final authViewModel = Provider.of<AuthViewModel>(context, listen: false);
            return AuthProvider(authViewModel);
          },
          update: (_, authViewModel, previous) {
            return previous ?? AuthProvider(authViewModel);
          },
        ),

        // ViewModels
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

        // New feature-based Tasks use cases
        ProxyProvider<TaskRepository, GetTasksUseCase>(
          update: (_, repository, __) => GetTasksUseCase(repository),
        ),

        ProxyProvider<TaskRepository, GetTaskProgressUseCase>(
          update: (_, repository, __) => GetTaskProgressUseCase(repository),
        ),

        ProxyProvider<TaskRepository, UpdateTaskStatusUseCase>(
          update: (_, repository, __) => UpdateTaskStatusUseCase(repository),
        ),

        ProxyProvider<TaskRepository, DeleteTaskUseCase>(
          update: (_, repository, __) => DeleteTaskUseCase(repository),
        ),

        // New feature-based Tasks ViewModel
        ChangeNotifierProxyProvider4<GetTasksUseCase, GetTaskProgressUseCase,
            UpdateTaskStatusUseCase, DeleteTaskUseCase, TasksViewModel>(
          create: (context) {
            return TasksViewModel(
              getTasksUseCase: Provider.of<GetTasksUseCase>(context, listen: false),
              getTaskProgressUseCase: Provider.of<GetTaskProgressUseCase>(context, listen: false),
              updateTaskStatusUseCase: Provider.of<UpdateTaskStatusUseCase>(context, listen: false),
              deleteTaskUseCase: Provider.of<DeleteTaskUseCase>(context, listen: false),
            );
          },
          update: (_, getTasksUseCase, getTaskProgressUseCase, updateTaskStatusUseCase,
              deleteTaskUseCase, previous) {
            return previous ?? TasksViewModel(
              getTasksUseCase: getTasksUseCase,
              getTaskProgressUseCase: getTaskProgressUseCase,
              updateTaskStatusUseCase: updateTaskStatusUseCase,
              deleteTaskUseCase: deleteTaskUseCase,
            );
          },
        ),

        ChangeNotifierProxyProvider<ProjectRepository, ProjectListViewModel>(
          create: (context) {
            final projectRepository =
                Provider.of<ProjectRepository>(context, listen: false);
            return ProjectListViewModel(projectRepository);
          },
          update: (_, projectRepository, previous) {
            if (previous != null) {
              previous.updateRepository(projectRepository);
              return previous;
            }
            return ProjectListViewModel(projectRepository);
          },
        ),

        ChangeNotifierProxyProvider<ProjectRepository, ProjectDetailViewModel>(
          create: (context) {
            final projectRepository =
                Provider.of<ProjectRepository>(context, listen: false);
            return ProjectDetailViewModel(projectRepository);
          },
          update: (_, projectRepository, previous) {
            if (previous != null) {
              previous.updateRepository(projectRepository);
              return previous;
            }
            return ProjectDetailViewModel(projectRepository);
          },
        ),

        ChangeNotifierProxyProvider<ProjectRepository, ProjectCreateEditViewModel>(
          create: (context) {
            final projectRepository =
                Provider.of<ProjectRepository>(context, listen: false);
            return ProjectCreateEditViewModel(projectRepository);
          },
          update: (_, projectRepository, previous) {
            if (previous != null) {
              previous.updateRepository(projectRepository);
              return previous;
            }
            return ProjectCreateEditViewModel(projectRepository);
          },
        ),

        // Legacy project view model used by some screens
        ChangeNotifierProvider(
          create: (_) => ProjectViewModel(),
        ),
      ];
} 