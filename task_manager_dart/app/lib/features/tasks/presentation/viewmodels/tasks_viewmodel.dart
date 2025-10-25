import 'package:flutter/foundation.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:task_manager_shared/models.dart';

import '../../domain/usecases/delete_task_usecase.dart';
import '../../domain/usecases/get_task_progress_usecase.dart';
import '../../domain/usecases/get_tasks_usecase.dart';
import '../../domain/usecases/update_task_status_usecase.dart';
import '../actions/tasks_action.dart';
import '../state/tasks_state.dart';
import '../../../../core/utils/result.dart';

/// ViewModel for the Tasks screen
/// Follows Flutter's best practices - no side effects system
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
        if (state.lastPageIsEmpty) return null;
        return state.nextIntPageKey;
      },
      fetchPage: _fetchPage,
    );
    // Load progress independently
    loadTaskProgress();
  }

  // Pagination controller
  late final PagingController<int, TaskDto> _pagingController;
  PagingController<int, TaskDto> get pagingController => _pagingController;

  // State
  TasksState _state = const TasksState();
  TasksState get state => _state;

  static const int _pageSize = 20;

  // Fetch page for pagination
  Future<List<TaskDto>> _fetchPage(int pageKey) async {
    try {
      final result = await _getTasksUseCase(
        page: pageKey - 1,
        size: _pageSize,
        query: _state.searchQuery.isEmpty ? null : _state.searchQuery,
      );

      if (result is Ok<PaginatedResponse<TaskDto>>) {
        final response = result.value;
        final isLastPage = pageKey >= response.totalPages - 1;

        _state = _state.copyWith(
          currentPage: pageKey,
          totalPages: response.totalPages,
          hasMorePages: !isLastPage,
        );

        return response.items;
      } else {
        final error = (result as Error).error;
        throw error;
      }
    } catch (error) {
      rethrow;
    }
  }

  // Load task progress independently
  Future<void> loadTaskProgress() async {
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
    await loadTaskProgress();

    _state = _state.copyWith(isRefreshing: false);
    notifyListeners();
  }

  // Public method: Update task status - returns Result for UI to handle
  Future<Result<void>> updateTaskStatus(String taskId, TaskStatus status) async {
    final result = await _updateTaskStatusUseCase(taskId, status);
    
    // Refresh regardless of result
    await _refreshTasks();
    
    return result is Ok
        ? Result.ok(null)
        : Result.error((result as Error).error);
  }

  // Public method: Delete task - returns Result for UI to handle
  Future<Result<void>> deleteTask(String taskId) async {
    final result = await _deleteTaskUseCase(taskId);
    
    // Refresh regardless of result
    await _refreshTasks();
    
    return result is Ok
        ? Result.ok(null)
        : Result.error((result as Error).error);
  }

  // Search tasks
  Future<void> searchTasks(String query) async {
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
        searchTasks(action.query);
        break;
      case OpenCreateTask():
      case OpenTaskDetails():
      case ConfirmTaskCompletion():
      case ConfirmTaskDeletion():
        // Navigation/dialogs handled in UI
        break;
      case UpdateTaskStatus():
        updateTaskStatus(action.taskId, action.status);
        break;
      case DeleteTask():
        deleteTask(action.taskId);
        break;
    }
  }

  // Public refresh method
  void refresh() => _refreshTasks();

  @override
  void dispose() {
    _pagingController.dispose();
    super.dispose();
  }
}
