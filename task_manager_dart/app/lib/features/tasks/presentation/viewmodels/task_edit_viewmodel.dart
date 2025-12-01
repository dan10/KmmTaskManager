import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/command.dart';
import '../../../../core/utils/result.dart';
import '../../domain/usecases/delete_task_usecase.dart';
import '../../domain/usecases/get_task_usecase.dart';
import '../../domain/usecases/update_task_usecase.dart';
import '../state/task_edit_state.dart';

/// ViewModel for the Task Edit screen
/// Follows Flutter's best practices with Command pattern
class TaskEditViewModel extends ChangeNotifier {
  final GetTaskUseCase _getTaskUseCase;
  final UpdateTaskUseCase _updateTaskUseCase;
  final DeleteTaskUseCase _deleteTaskUseCase;
  final _log = Logger('TaskEditViewModel');

  // Callbacks for navigation
  VoidCallback? onBack;
  VoidCallback? onTaskUpdated;
  VoidCallback? onTaskDeleted;

  TaskEditViewModel({
    required GetTaskUseCase getTaskUseCase,
    required UpdateTaskUseCase updateTaskUseCase,
    required DeleteTaskUseCase deleteTaskUseCase,
    required String taskId,
  })  : _getTaskUseCase = getTaskUseCase,
        _updateTaskUseCase = updateTaskUseCase,
        _deleteTaskUseCase = deleteTaskUseCase,
        _taskId = taskId {
    // Initialize commands
    updateTask = Command0<void>(_updateTask);
    deleteTask = Command0<void>(_deleteTask);

    // Load task details
    _loadTask();
  }

  final String _taskId;

  // Commands
  late Command0<void> updateTask;
  late Command0<void> deleteTask;

  // State
  TaskEditState _state = const TaskEditState();
  TaskEditState get state => _state;

  /// Load task details
  Future<void> _loadTask() async {
    _state = _state.copyWith(isLoading: true, clearError: true);
    notifyListeners();

    _log.info('Loading task for edit: $_taskId');
    final result = await _getTaskUseCase(_taskId);

    if (result is Ok<TaskDto>) {
      final task = result.value;
      _log.info('Task loaded: ${task.title}');
      _state = TaskEditState(
        isLoading: false,
        title: task.title,
        description: task.description,
        priority: task.priority,
        status: task.status,
        dueDate: task.dueDate,
        projectId: task.projectId,
        originalTask: task,
      );
    } else {
      final error = (result as Error).error;
      _log.severe('Failed to load task: $error');
      _state = _state.copyWith(
        isLoading: false,
        errorMessage: 'Failed to load task',
      );
    }
    notifyListeners();
  }

  /// Command: Update task
  Future<Result<void>> _updateTask() async {
    if (!_state.isValid) {
      return Result.error(Exception('Invalid form data'));
    }

    _state = _state.copyWith(isLoading: true, clearError: true);
    notifyListeners();

    final request = TaskUpdateRequestDto(
      title: _state.title,
      description: _state.description,
      priority: _state.priority,
      status: _state.status,
      dueDate: _state.dueDate,
    );

    final result = await _updateTaskUseCase(_taskId, request);

    if (result is Ok<TaskDto>) {
      _log.info('Task updated successfully');
      _state = _state.copyWith(isLoading: false);
      notifyListeners();
      onTaskUpdated?.call();
      return Result.ok(null);
    } else {
      _log.severe('Failed to update task');
      _state = _state.copyWith(
        isLoading: false,
        errorMessage: 'Failed to update task',
      );
      notifyListeners();
      return Result.error((result as Error).error);
    }
  }

  /// Command: Delete task
  Future<Result<void>> _deleteTask() async {
    _state = _state.copyWith(isDeleting: true);
    notifyListeners();

    final result = await _deleteTaskUseCase(_taskId);

    _state = _state.copyWith(isDeleting: false);
    notifyListeners();

    if (result is Ok<void>) {
      _log.info('Task deleted successfully');
      onTaskDeleted?.call();
      return Result.ok(null);
    } else {
      _log.severe('Failed to delete task');
      return Result.error((result as Error).error);
    }
  }

  /// Update title
  void setTitle(String title) {
    _state = _state.copyWith(title: title);
    notifyListeners();
  }

  /// Update description
  void setDescription(String description) {
    _state = _state.copyWith(description: description);
    notifyListeners();
  }

  /// Update priority
  void setPriority(Priority priority) {
    _state = _state.copyWith(priority: priority);
    notifyListeners();
  }

  /// Update status
  void setStatus(TaskStatus status) {
    _state = _state.copyWith(status: status);
    notifyListeners();
  }

  /// Update due date
  void setDueDate(DateTime? dueDate) {
    _state = _state.copyWith(dueDate: dueDate, clearDueDate: dueDate == null);
    notifyListeners();
  }

  /// Handle navigate back action
  void handleNavigateBack() {
    onBack?.call();
  }

  @override
  void dispose() {
    updateTask.dispose();
    deleteTask.dispose();
    super.dispose();
  }
}

