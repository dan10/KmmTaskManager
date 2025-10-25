import 'package:flutter/foundation.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../../domain/usecases/delete_task_usecase.dart';
import '../../domain/usecases/get_task_progress_usecase.dart';
import '../../domain/usecases/get_tasks_usecase.dart';
import '../../domain/usecases/update_task_status_usecase.dart';
import '../state/tasks_state.dart';
import '../../../../core/utils/result.dart';
import '../../../../core/utils/command.dart';

/// ViewModel for the Tasks screen
/// Follows Flutter's best practices with Command pattern
class TasksViewModel extends ChangeNotifier {
  final GetTasksUseCase _getTasksUseCase;
  final GetTaskProgressUseCase _getTaskProgressUseCase;
  final UpdateTaskStatusUseCase _updateTaskStatusUseCase;
  final DeleteTaskUseCase _deleteTaskUseCase;
  final _log = Logger('TasksViewModel');

  TasksViewModel({
    required GetTasksUseCase getTasksUseCase,
    required GetTaskProgressUseCase getTaskProgressUseCase,
    required UpdateTaskStatusUseCase updateTaskStatusUseCase,
    required DeleteTaskUseCase deleteTaskUseCase,
  })  : _getTasksUseCase = getTasksUseCase,
        _getTaskProgressUseCase = getTaskProgressUseCase,
        _updateTaskStatusUseCase = updateTaskStatusUseCase,
        _deleteTaskUseCase = deleteTaskUseCase {
    // Initialize commands
    updateTaskStatus = Command1<void, (String, TaskStatus)>(_updateTaskStatus);
    deleteTask = Command1<void, String>(_deleteTask);
    
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

  // Commands
  late Command1<void, (String, TaskStatus)> updateTaskStatus;
  late Command1<void, String> deleteTask;

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
  Future<void> loadTaskProgress({bool isRefresh = false}) async {
    // Only set isLoading on initial load, not on refresh
    if (!isRefresh) {
      _state = _state.copyWith(isLoading: true);
      notifyListeners();
    }

    _log.info('Loading task progress...');
    final result = await _getTaskProgressUseCase();

    if (result is Ok<TaskProgress>) {
      final progress = result.value;
      _log.info('Task progress loaded: ${progress.completedTasks}/${progress.totalTasks}');
      _state = _state.copyWith(
        isLoading: false,
        completedTasks: progress.completedTasks,
        totalTasks: progress.totalTasks,
      );
    } else {
      final error = (result as Error).error;
      _log.severe('Failed to load task progress: $error');
      _state = _state.copyWith(isLoading: false);
    }
    notifyListeners();
  }

  // Refresh tasks
  Future<void> _refreshTasks() async {
    _state = _state.copyWith(isRefreshing: true);
    notifyListeners();

    _pagingController.refresh();
    await loadTaskProgress(isRefresh: true);

    _state = _state.copyWith(isRefreshing: false);
    notifyListeners();
  }

  // Command: Update task status
  Future<Result<void>> _updateTaskStatus((String, TaskStatus) params) async {
    final (taskId, status) = params;
    final result = await _updateTaskStatusUseCase(taskId, status);
    
    // Refresh regardless of result
    await _refreshTasks();
    
    return result is Ok
        ? Result.ok(null)
        : Result.error((result as Error).error);
  }

  // Command: Delete task
  Future<Result<void>> _deleteTask(String taskId) async {
    final result = await _deleteTaskUseCase(taskId);
    
    // Refresh regardless of result
    await _refreshTasks();
    
    return result is Ok
        ? Result.ok(null)
        : Result.error((result as Error).error);
  }

  // Search tasks
  void searchTasks(String query) {
    _state = _state.copyWith(searchQuery: query);
    notifyListeners();
    _pagingController.refresh();
  }

  // Public refresh method
  void refresh() => _refreshTasks();

  @override
  void dispose() {
    updateTaskStatus.dispose();
    deleteTask.dispose();
    _pagingController.dispose();
    super.dispose();
  }
}
