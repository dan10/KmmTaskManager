import 'package:flutter/foundation.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/command.dart';
import '../../../../core/utils/result.dart';
import '../../../tasks/domain/usecases/delete_task_usecase.dart';
import '../../../tasks/domain/usecases/update_task_status_usecase.dart';
import '../../domain/usecases/get_tasks_for_date_usecase.dart';
import '../state/calendar_state.dart';

/// ViewModel for the Calendar screen
/// Follows the same pattern as TasksViewModel
class CalendarViewModel extends ChangeNotifier {
  final GetTasksForDateUseCase _getTasksForDateUseCase;
  final UpdateTaskStatusUseCase _updateTaskStatusUseCase;
  final DeleteTaskUseCase _deleteTaskUseCase;

  CalendarViewModel({
    required GetTasksForDateUseCase getTasksForDateUseCase,
    required UpdateTaskStatusUseCase updateTaskStatusUseCase,
    required DeleteTaskUseCase deleteTaskUseCase,
  })  : _getTasksForDateUseCase = getTasksForDateUseCase,
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
  }

  // Commands
  late Command1<void, (String, TaskStatus)> updateTaskStatus;
  late Command1<void, String> deleteTask;

  // Pagination controller
  late final PagingController<int, TaskDto> _pagingController;
  PagingController<int, TaskDto> get pagingController => _pagingController;

  // State
  CalendarState _state = CalendarState(selectedDate: DateTime.now());
  CalendarState get state => _state;

  static const int _pageSize = 20;

  // Fetch page for pagination
  Future<List<TaskDto>> _fetchPage(int pageKey) async {
    try {
      final result = await _getTasksForDateUseCase(
        date: _state.selectedDate,
        page: pageKey - 1,
        size: _pageSize,
      );

      if (result is Ok<PaginatedResponse<TaskDto>>) {
        final response = result.value;
        final isLastPage = pageKey >= response.totalPages - 1;

        _state = _state.copyWith(
          currentPage: pageKey,
          totalPages: response.totalPages,
          hasMorePages: !isLastPage,
          totalTasks: response.total,
        );
        notifyListeners();

        return response.items;
      } else {
        final error = (result as Error).error;
        throw error;
      }
    } catch (error) {
      rethrow;
    }
  }

  // Change selected date
  void selectDate(DateTime date) {
    if (_isSameDay(_state.selectedDate, date)) return;

    _state = _state.copyWith(selectedDate: date);
    notifyListeners();

    // Refresh pagination with new date
    _pagingController.refresh();
  }

  bool _isSameDay(DateTime a, DateTime b) {
    return a.year == b.year && a.month == b.month && a.day == b.day;
  }

  // Refresh tasks
  Future<void> _refreshTasks() async {
    _state = _state.copyWith(isRefreshing: true);
    notifyListeners();

    _pagingController.refresh();

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

