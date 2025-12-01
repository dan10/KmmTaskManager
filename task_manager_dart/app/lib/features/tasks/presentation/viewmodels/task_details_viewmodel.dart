import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/command.dart';
import '../../../../core/utils/result.dart';
import '../../domain/usecases/delete_task_usecase.dart';
import '../../domain/usecases/get_task_usecase.dart';
import '../state/task_details_state.dart';

/// ViewModel for the Task Details screen
/// Follows Flutter's best practices with Command pattern
class TaskDetailsViewModel extends ChangeNotifier {
  final GetTaskUseCase _getTaskUseCase;
  final DeleteTaskUseCase _deleteTaskUseCase;
  final _log = Logger('TaskDetailsViewModel');

  // Callbacks for navigation
  VoidCallback? onBack;
  Function(String taskId)? onEditTask;

  TaskDetailsViewModel({
    required GetTaskUseCase getTaskUseCase,
    required DeleteTaskUseCase deleteTaskUseCase,
    required String taskId,
    TaskDto? initialTask,
  })  : _getTaskUseCase = getTaskUseCase,
        _deleteTaskUseCase = deleteTaskUseCase,
        _taskId = taskId {
    // Initialize commands
    deleteTask = Command0<void>(_deleteTask);
    
    // Set initial task if provided (for Hero animation)
    if (initialTask != null) {
      _state = _state.copyWith(task: initialTask, isLoading: false);
    }
    
    // Load task details (will update if initial task was provided)
    _loadTaskDetails();
  }

  final String _taskId;

  // Commands
  late Command0<void> deleteTask;

  // State
  TaskDetailsState _state = const TaskDetailsState();
  TaskDetailsState get state => _state;

  /// Load task details
  Future<void> _loadTaskDetails() async {
    // Only show loading if we don't have task data yet
    if (_state.task == null) {
      _state = _state.copyWith(isLoading: true, errorMessage: null);
      notifyListeners();
    }

    _log.info('Loading task details for taskId: $_taskId');
    final result = await _getTaskUseCase(_taskId);

    if (result is Ok<TaskDto>) {
      final task = result.value;
      _log.info('Task details loaded: ${task.title}');
      _state = _state.copyWith(
        isLoading: false,
        task: task,
      );
    } else {
      final error = (result as Error).error;
      _log.severe('Failed to load task details: $error');
      _state = _state.copyWith(
        isLoading: false,
        errorMessage: 'Failed to load task details',
      );
    }
    notifyListeners();
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
      // Navigate back after successful deletion
      onBack?.call();
      return Result.ok(null);
    } else {
      _log.severe('Failed to delete task');
      return Result.error((result as Error).error);
    }
  }

  /// Handle edit task action
  void handleEditTask() {
    if (_state.task != null) {
      onEditTask?.call(_state.task!.id);
    }
  }

  /// Handle navigate back action
  void handleNavigateBack() {
    onBack?.call();
  }

  /// Refresh task details
  Future<void> refresh() async {
    await _loadTaskDetails();
  }

  @override
  void dispose() {
    deleteTask.dispose();
    super.dispose();
  }
}

