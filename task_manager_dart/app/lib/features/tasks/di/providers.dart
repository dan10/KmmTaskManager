import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';
import 'package:task_manager_shared/models.dart';

import '../../../core/network/api_client.dart';
import '../data/services/task_api_service.dart';
import '../data/repositories/task_repository.dart';
import '../domain/usecases/delete_task_usecase.dart';
import '../domain/usecases/get_task_progress_usecase.dart';
import '../domain/usecases/get_task_usecase.dart';
import '../domain/usecases/get_tasks_usecase.dart';
import '../domain/usecases/update_task_status_usecase.dart';
import '../domain/usecases/update_task_usecase.dart';
import '../presentation/viewmodels/task_details_viewmodel.dart';
import '../presentation/viewmodels/task_edit_viewmodel.dart';
import '../presentation/viewmodels/tasks_viewmodel.dart';


List<SingleChildWidget> get providers => [
  // Task API Service - uses shared ApiClient from core
  ProxyProvider<ApiClient, TaskApiService>(
    update: (_, apiClient, __) => TaskApiServiceImpl(apiClient),
  ),

  // Task Repository
  ProxyProvider<TaskApiService, TaskRepository>(
    update: (_, api, __) => TaskRepositoryImpl(api),
  ),
  
  // Use Cases
  ProxyProvider<TaskApiService, GetTaskUseCase>(
    update: (_, api, __) => GetTaskUseCase(api),
  ),
  ProxyProvider<TaskApiService, UpdateTaskUseCase>(
    update: (_, api, __) => UpdateTaskUseCase(api),
  ),
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
];

/// Factory function to create TaskDetailsViewModel
/// Note: TaskDetailsViewModel requires a taskId parameter, so it cannot be
/// provided globally. Instead, use this factory function with ChangeNotifierProvider.value
/// or create it directly when navigating to the details screen.
TaskDetailsViewModel createTaskDetailsViewModel({
  required BuildContext context,
  required String taskId,
  TaskDto? initialTask,
}) {
  return TaskDetailsViewModel(
    getTaskUseCase: Provider.of<GetTaskUseCase>(context, listen: false),
    deleteTaskUseCase: Provider.of<DeleteTaskUseCase>(context, listen: false),
    taskId: taskId,
    initialTask: initialTask,
  );
}

/// Factory function to create TaskEditViewModel
/// Note: TaskEditViewModel requires a taskId parameter, so it cannot be
/// provided globally. Instead, use this factory function with ChangeNotifierProvider.value
/// or create it directly when navigating to the edit screen.
TaskEditViewModel createTaskEditViewModel({
  required BuildContext context,
  required String taskId,
}) {
  return TaskEditViewModel(
    getTaskUseCase: Provider.of<GetTaskUseCase>(context, listen: false),
    updateTaskUseCase: Provider.of<UpdateTaskUseCase>(context, listen: false),
    deleteTaskUseCase: Provider.of<DeleteTaskUseCase>(context, listen: false),
    taskId: taskId,
  );
}


