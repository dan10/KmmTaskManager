import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

import '../../../data/services/task_api_service.dart';
import '../data/repositories/task_repository.dart';
import '../domain/usecases/delete_task_usecase.dart';
import '../domain/usecases/get_task_progress_usecase.dart';
import '../domain/usecases/get_tasks_usecase.dart';
import '../domain/usecases/update_task_status_usecase.dart';
import '../presentation/viewmodels/tasks_viewmodel.dart';
import '../view_models/task_list_viewmodel.dart';
import '../view_models/task_detail_viewmodel.dart';
import '../view_models/task_create_edit_viewmodel.dart';

List<SingleChildWidget> get providers => [
  // Repository
  ProxyProvider<TaskApiService, TaskRepository>(
    update: (_, api, __) => TaskRepositoryImpl(api),
  ),
  
  // Use Cases
  ProxyProvider<TaskRepository, GetTasksUseCase>(
    update: (_, repo, __) => GetTasksUseCase(repo),
  ),
  ProxyProvider<TaskRepository, GetTaskProgressUseCase>(
    update: (_, repo, __) => GetTaskProgressUseCase(repo),
  ),
  ProxyProvider<TaskRepository, UpdateTaskStatusUseCase>(
    update: (_, repo, __) => UpdateTaskStatusUseCase(repo),
  ),
  ProxyProvider<TaskRepository, DeleteTaskUseCase>(
    update: (_, repo, __) => DeleteTaskUseCase(repo),
  ),

  // New Tasks ViewModel (with pagination)
  ChangeNotifierProxyProvider4<GetTasksUseCase, GetTaskProgressUseCase,
      UpdateTaskStatusUseCase, DeleteTaskUseCase, TasksViewModel>(
    create: (context) => TasksViewModel(
      getTasksUseCase: Provider.of<GetTasksUseCase>(context, listen: false),
      getTaskProgressUseCase: Provider.of<GetTaskProgressUseCase>(context, listen: false),
      updateTaskStatusUseCase: Provider.of<UpdateTaskStatusUseCase>(context, listen: false),
      deleteTaskUseCase: Provider.of<DeleteTaskUseCase>(context, listen: false),
    ),
    update: (_, getTasks, getProgress, updateStatus, deleteTask, previous) =>
        previous ??
        TasksViewModel(
          getTasksUseCase: getTasks,
          getTaskProgressUseCase: getProgress,
          updateTaskStatusUseCase: updateStatus,
          deleteTaskUseCase: deleteTask,
        ),
  ),

  // Legacy ViewModels (kept for backward compatibility)
  ChangeNotifierProxyProvider<TaskRepository, TaskListViewModel>(
    create: (context) => TaskListViewModel(
      repository: Provider.of<TaskRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? TaskListViewModel(repository: repo),
  ),
  ChangeNotifierProxyProvider<TaskRepository, TaskDetailViewModel>(
    create: (context) => TaskDetailViewModel(
      repository: Provider.of<TaskRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? TaskDetailViewModel(repository: repo),
  ),
  ChangeNotifierProxyProvider<TaskRepository, TaskCreateEditViewModel>(
    create: (context) => TaskCreateEditViewModel(
      repository: Provider.of<TaskRepository>(context, listen: false),
    ),
    update: (_, repo, previous) => previous ?? TaskCreateEditViewModel(repository: repo),
  ),
];


