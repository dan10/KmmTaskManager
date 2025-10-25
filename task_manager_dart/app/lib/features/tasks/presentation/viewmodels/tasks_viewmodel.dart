import 'package:flutter/foundation.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:task_manager_shared/models.dart';

import '../../domain/usecases/delete_task_usecase.dart';
import '../../domain/usecases/get_task_progress_usecase.dart';
import '../../domain/usecases/get_tasks_usecase.dart';
import '../../domain/usecases/update_task_status_usecase.dart';
import '../actions/tasks_action.dart';
import '../effects/tasks_effect.dart';
import '../state/tasks_state.dart';
import '../../../../core/utils/result.dart';

/// ViewModel for the Tasks screen
/// Matches KMM's TasksViewModel with pagination support using infinite_scroll_pagination
class TasksViewModel extends ChangeNotifier {
  final GetTasksUseCase _getTasksUseCase;
  final GetTaskProgressUseCase _getTaskProgressUseCase;
  final UpdateTaskStatusUseCase _updateTaskStatusUseCase;
  final DeleteTaskUseCase _deleteTaskUseCase;

  TasksViewModel({
    required GetTasksUseCase getTasksUseCase,
    required GetTaskProgressUseCase getTaskProgressUseCase,
    required UpdateTaskStatusUseCase updateTaskStatusUseCase,
    required DeleteTaskUseCase deleteTaskUseCase,
  })  : _getTasksUseCase = getTasksUseCase,
        _getTaskProgressUseCase = getTaskProgressUseCase,
        _updateTaskStatusUseCase = updateTaskStatusUseCase,
        _deleteTaskUseCase = deleteTaskUseCase {
    // Initialize pagination controller
    _pagingController = PagingController<int, TaskDto>(
      getNextPageKey: (state) {
        // Check if we have more pages based on our custom state
        if (_state.hasMorePages) {
          return _state.currentPage + 1;
        }
        return null;
      },
      fetchPage: _fetchPage,
    );
    // Load initial progress
    _loadTaskProgress();
  }

  // Pagination controller
  late final PagingController<int, TaskDto> _pagingController;

  PagingController<int, TaskDto> get pagingController => _pagingController;

  // State
  TasksState _state = const TasksState();
  TasksState get state => _state;

  // Effects stream
  final List<TasksEffect> _effects = [];
  List<TasksEffect> get effects => List.unmodifiable(_effects);

  static const int _pageSize = 20;

  // Fetch page for pagination
  Future<List<TaskDto>> _fetchPage(int pageKey) async {
    try {
      final result = await _getTasksUseCase(
        page: pageKey,
        size: _pageSize,
        query: _state.searchQuery.isEmpty ? null : _state.searchQuery,
      );

      if (result is Ok<PaginatedResponse<TaskDto>>) {
        final response = result.value;
        final isLastPage = pageKey >= response.totalPages - 1;

        // Update state with pagination info
        _state = _state.copyWith(
          currentPage: pageKey,
          totalPages: response.totalPages,
          hasMorePages: !isLastPage,
        );
        notifyListeners();

        return response.items;
      } else {
        final error = (result as Error).error;
        _state = _state.copyWith(errorMessage: error.toString());
        notifyListeners();
        throw error;
      }
    } catch (error) {
      _state = _state.copyWith(errorMessage: error.toString());
      notifyListeners();
      rethrow;
    }
  }

  // Load task progress (completed/total tasks)
  Future<void> _loadTaskProgress() async {
    _state = _state.copyWith(isLoading: true);
    notifyListeners();

    final result = await _getTaskProgressUseCase();

    if (result is Ok<TaskProgress>) {
      final progress = result.value;
      _state = _state.copyWith(
        isLoading: false,
        completedTasks: progress.completedTasks,
        totalTasks: progress.totalTasks,
      );
    } else {
      _state = _state.copyWith(isLoading: false);
    }
    notifyListeners();
  }

  // Refresh tasks
  Future<void> _refreshTasks() async {
    _state = _state.copyWith(isRefreshing: true);
    notifyListeners();

    _pagingController.refresh();
    await _loadTaskProgress();

    _state = _state.copyWith(isRefreshing: false);
    notifyListeners();
  }

  // Update task status
  Future<void> _updateTaskStatus(String taskId, TaskStatus status) async {
    final result = await _updateTaskStatusUseCase(taskId, status);

    if (result is Ok<TaskDto>) {
      await _refreshTasks();
      final message =
          status == TaskStatus.done ? 'Task completed!' : 'Task marked as incomplete';
      _emitEffect(TasksEffect.showSuccessSnackbar(message));
    } else {
      await _refreshTasks();
      _emitEffect(TasksEffect.showErrorSnackbar(
        message: 'Failed to update task',
        actionLabel: 'Retry',
        onAction: () => _updateTaskStatus(taskId, status),
      ));
    }
  }

  // Delete task
  Future<void> _deleteTask(String taskId) async {
    final result = await _deleteTaskUseCase(taskId);

    // Check if result is Ok (success case)
    if (result is Ok) {
      await _refreshTasks();
      _emitEffect(TasksEffect.showSuccessSnackbar('Task deleted successfully'));
    } else {
      // result is Error
      await _refreshTasks();
      _emitEffect(TasksEffect.showErrorSnackbar(
        message: 'Failed to delete task',
        actionLabel: 'Retry',
        onAction: () {
          _deleteTask(taskId);
        },
      ));
    }
  }

  // Confirm task completion
  void _confirmTaskCompletion(String taskId, TaskStatus status) {
    final actionText = status == TaskStatus.done ? 'complete' : 'mark as incomplete';
    _emitEffect(TasksEffect.showConfirmationSnackbar(
      message: 'Are you sure you want to $actionText this task?',
      actionLabel: 'Confirm',
      onAction: () => _updateTaskStatus(taskId, status),
    ));
  }

  // Confirm task deletion
  void _confirmTaskDeletion(String taskId) {
    _emitEffect(TasksEffect.showConfirmationSnackbar(
      message: 'Are you sure you want to delete this task?',
      actionLabel: 'Delete',
      onAction: () => _deleteTask(taskId),
    ));
  }

  // Navigate to task detail
  void _navigateToTaskDetail(String taskId) {
    _emitEffect(TasksEffect.navigateToTaskDetail(taskId));
  }

  // Show create task bottom sheet
  void _showCreateTaskBottomSheet() {
    _emitEffect(TasksEffect.showCreateTaskBottomSheet());
  }

  // Search tasks
  Future<void> _searchTasks(String query) async {
    _state = _state.copyWith(searchQuery: query);
    notifyListeners();
    await _refreshTasks();
  }

  // Handle actions
  void handleAction(TasksAction action) {
    switch (action) {
      case LoadTasks():
        _pagingController.refresh();
        break;
      case RefreshTasks():
        _refreshTasks();
        break;
      case LoadMoreTasks():
        // Handled automatically by PagingController
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

  // Emit effect
  void _emitEffect(TasksEffect effect) {
    _effects.add(effect);
    notifyListeners();
  }

  // Clear effects
  void clearEffects() {
    _effects.clear();
    notifyListeners();
  }

  // Public method to refresh - can be called from outside
  void refresh() {
    _refreshTasks();
  }

  // Method to check if refresh is needed
  void checkAndRefresh() {
    refresh();
  }

  @override
  void dispose() {
    _pagingController.dispose();
    super.dispose();
  }
}

