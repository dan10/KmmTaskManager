import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';
import '../state/tasks_state.dart';
import '../actions/tasks_action.dart';
import '../effects/tasks_effect.dart';
import '../../domain/usecases/get_tasks_usecase.dart';
import '../../domain/usecases/get_task_progress_usecase.dart';
import '../../domain/usecases/update_task_status_usecase.dart';
import '../../domain/usecases/delete_task_usecase.dart';

/// Tasks ViewModel matching KMM's TasksViewModel with State-Action-Effect pattern
class TasksViewModel extends ChangeNotifier {
  final GetTasksUseCase _getTasksUseCase;
  final GetTaskProgressUseCase _getTaskProgressUseCase;
  final UpdateTaskStatusUseCase _updateTaskStatusUseCase;
  final DeleteTaskUseCase _deleteTaskUseCase;

  TasksState _state = const TasksState();
  final _effectsController = StreamController<TasksEffect>.broadcast();

  TasksState get state => _state;
  Stream<TasksEffect> get effects => _effectsController.stream;

  TasksViewModel({
    required GetTasksUseCase getTasksUseCase,
    required GetTaskProgressUseCase getTaskProgressUseCase,
    required UpdateTaskStatusUseCase updateTaskStatusUseCase,
    required DeleteTaskUseCase deleteTaskUseCase,
  })  : _getTasksUseCase = getTasksUseCase,
        _getTaskProgressUseCase = getTaskProgressUseCase,
        _updateTaskStatusUseCase = updateTaskStatusUseCase,
        _deleteTaskUseCase = deleteTaskUseCase {
    // Load initial data
    handleAction(const LoadTasks());
  }

  @override
  void dispose() {
    _effectsController.close();
    super.dispose();
  }

  /// Central action handler matching KMM's handleActions method
  void handleAction(TasksAction action) {
    switch (action) {
      case LoadTasks():
        _loadTasks();
        break;
      case RefreshTasks():
        _refreshTasks();
        break;
      case LoadMoreTasks():
        _loadMoreTasks();
        break;
      case SearchTasks():
        _searchTasks(action.query);
        break;
      case OpenCreateTask():
        _showCreateTaskBottomSheet();
        break;
      case OpenTaskDetails():
        _navigateToTaskDetail(action.taskId);
        break;
      case UpdateTaskStatus():
        _updateTaskStatus(action.taskId, action.status);
        break;
      case DeleteTask():
        _deleteTask(action.taskId);
        break;
      case ConfirmTaskCompletion():
        _confirmTaskCompletion(action.taskId, action.status);
        break;
      case ConfirmTaskDeletion():
        _confirmTaskDeletion(action.taskId);
        break;
    }
  }

  Future<void> _loadTasks() async {
    _updateState(_state.copyWith(isLoading: true));
    
    try {
      // Load tasks and progress in parallel
      final results = await Future.wait([
        _getTasksUseCase(page: 0, size: 20, query: _state.searchQuery.isEmpty ? null : _state.searchQuery),
        _getTaskProgressUseCase(),
      ]);

      final tasksResponse = results[0] as PaginatedResponse<TaskDto>;
      final progress = results[1] as TaskProgress;

      _updateState(_state.copyWith(
        isLoading: false,
        tasks: tasksResponse.items,
        currentPage: tasksResponse.page,
        totalPages: tasksResponse.totalPages,
        hasMorePages: tasksResponse.page < tasksResponse.totalPages - 1,
        completedTasks: progress.completedTasks,
        totalTasks: progress.totalTasks,
      ));
    } catch (e) {
      _updateState(_state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
      _effectsController.add(ShowErrorSnackbar(
        message: 'Failed to load tasks',
        actionLabel: 'Retry',
        onAction: () => handleAction(const LoadTasks()),
      ));
    }
  }

  Future<void> _refreshTasks() async {
    _updateState(_state.copyWith(isRefreshing: true));
    
    try {
      // Load tasks and progress in parallel
      final results = await Future.wait([
        _getTasksUseCase(page: 0, size: 20, query: _state.searchQuery.isEmpty ? null : _state.searchQuery),
        _getTaskProgressUseCase(),
      ]);

      final tasksResponse = results[0] as PaginatedResponse<TaskDto>;
      final progress = results[1] as TaskProgress;

      _updateState(_state.copyWith(
        isRefreshing: false,
        tasks: tasksResponse.items,
        currentPage: tasksResponse.page,
        totalPages: tasksResponse.totalPages,
        hasMorePages: tasksResponse.page < tasksResponse.totalPages - 1,
        completedTasks: progress.completedTasks,
        totalTasks: progress.totalTasks,
      ));
    } catch (e) {
      _updateState(_state.copyWith(
        isRefreshing: false,
        errorMessage: e.toString(),
      ));
    }
  }

  Future<void> _loadMoreTasks() async {
    if (!_state.hasMorePages || _state.isLoading) return;

    final nextPage = _state.currentPage + 1;
    
    try {
      final tasksResponse = await _getTasksUseCase(
        page: nextPage,
        size: 20,
        query: _state.searchQuery.isEmpty ? null : _state.searchQuery,
      );

      _updateState(_state.copyWith(
        tasks: [..._state.tasks, ...tasksResponse.items],
        currentPage: tasksResponse.page,
        totalPages: tasksResponse.totalPages,
        hasMorePages: tasksResponse.page < tasksResponse.totalPages - 1,
      ));
    } catch (e) {
      _effectsController.add(ShowErrorSnackbar(
        message: 'Failed to load more tasks',
      ));
    }
  }

  Future<void> _searchTasks(String query) async {
    _updateState(_state.copyWith(searchQuery: query));
    
    // Debounce search
    await Future.delayed(const Duration(milliseconds: 300));
    if (_state.searchQuery == query) {
      _loadTasks();
    }
  }

  Future<void> _updateTaskStatus(String taskId, TaskStatus status) async {
    try {
      await _updateTaskStatusUseCase(taskId, status);
      _refreshTasks();
      
      final message = status == TaskStatus.done
          ? 'Task completed!'
          : 'Task marked as incomplete';
      _effectsController.add(ShowSuccessSnackbar(message));
    } catch (e) {
      _effectsController.add(ShowErrorSnackbar(
        message: 'Failed to update task',
        actionLabel: 'Retry',
        onAction: () => _updateTaskStatus(taskId, status),
      ));
    }
  }

  Future<void> _deleteTask(String taskId) async {
    try {
      await _deleteTaskUseCase(taskId);
      _refreshTasks();
      _effectsController.add(const ShowSuccessSnackbar('Task deleted successfully'));
    } catch (e) {
      _effectsController.add(ShowErrorSnackbar(
        message: 'Failed to delete task',
        actionLabel: 'Retry',
        onAction: () => _deleteTask(taskId),
      ));
    }
  }

  void _confirmTaskCompletion(String taskId, TaskStatus status) {
    final actionText = status == TaskStatus.done ? 'complete' : 'mark as incomplete';
    _effectsController.add(ShowConfirmationSnackbar(
      message: 'Are you sure you want to $actionText this task?',
      actionLabel: 'Confirm',
      onAction: () => _updateTaskStatus(taskId, status),
    ));
  }

  void _confirmTaskDeletion(String taskId) {
    _effectsController.add(ShowConfirmationSnackbar(
      message: 'Are you sure you want to delete this task?',
      actionLabel: 'Delete',
      onAction: () => _deleteTask(taskId),
    ));
  }

  void _navigateToTaskDetail(String taskId) {
    _effectsController.add(NavigateToTaskDetail(taskId));
  }

  void _showCreateTaskBottomSheet() {
    _effectsController.add(const ShowCreateTaskBottomSheet());
  }

  void _updateState(TasksState newState) {
    _state = newState;
    notifyListeners();
  }

  // Public method for external refresh (e.g., when returning from detail screen)
  void refresh() {
    handleAction(const RefreshTasks());
  }
}

