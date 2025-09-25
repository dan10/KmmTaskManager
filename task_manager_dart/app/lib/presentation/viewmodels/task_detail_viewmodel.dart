import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/task_repository.dart';

enum TaskDetailState {
  initial,
  loading,
  loaded,
  updating,
  deleting,
  deleted,
  error,
}

class TaskDetailViewModel extends ChangeNotifier {
  final TaskRepository _taskRepository;

  TaskDetailState _state = TaskDetailState.initial;
  TaskDto? _task;
  String? _errorMessage;

  TaskDetailState get state => _state;
  TaskDto? get task => _task;
  String? get errorMessage => _errorMessage;
  
  bool get isLoading => _state == TaskDetailState.loading;
  bool get isUpdating => _state == TaskDetailState.updating;
  bool get isDeleting => _state == TaskDetailState.deleting;
  bool get hasTask => _task != null;

  TaskDetailViewModel(this._taskRepository);

  Future<void> loadTask(String id) async {
    _setState(TaskDetailState.loading);
    
    try {
      _task = await _taskRepository.getTask(id);
      _setState(TaskDetailState.loaded);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> updateTask(String id, TaskUpdateRequestDto request) async {
    _setState(TaskDetailState.updating);
    
    try {
      _task = await _taskRepository.updateTask(id, request);
      _setState(TaskDetailState.loaded);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<bool> deleteTask(String id) async {
    _setState(TaskDetailState.deleting);
    
    try {
      await _taskRepository.deleteTask(id);
      _task = null;
      _setState(TaskDetailState.deleted);
      return true;
    } catch (e) {
      _setError(e.toString());
      return false;
    }
  }

  Future<void> changeTaskStatus(String id, TaskStatus status) async {
    if (_task == null) return;
    
    _setState(TaskDetailState.updating);
    
    try {
      _task = await _taskRepository.changeTaskStatus(id, status);
      _setState(TaskDetailState.loaded);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> assignTask(String id, String assigneeId) async {
    if (_task == null) return;
    
    _setState(TaskDetailState.updating);
    
    try {
      _task = await _taskRepository.assignTask(id, assigneeId);
      _setState(TaskDetailState.loaded);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> refreshTask() async {
    if (_task != null) {
      await loadTask(_task!.id);
    }
  }

  // Utility methods
  bool get isOverdue {
    if (_task?.dueDate == null) return false;
    return _task!.dueDate!.isBefore(DateTime.now()) && 
           _task!.status != TaskStatus.done;
  }

  bool get isCompleted => _task?.status == TaskStatus.done;

  String get statusDisplayName {
    switch (_task?.status) {
      case TaskStatus.todo:
        return 'To Do';
      case TaskStatus.inProgress:
        return 'In Progress';
      case TaskStatus.done:
        return 'Done';
      default:
        return 'Unknown';
    }
  }

  String get priorityDisplayName {
    switch (_task?.priority) {
      case Priority.low:
        return 'Low';
      case Priority.medium:
        return 'Medium';
      case Priority.high:
        return 'High';
      default:
        return 'Unknown';
    }
  }

  void _setState(TaskDetailState state) {
    _state = state;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _setState(TaskDetailState.error);
  }

  void clearError() {
    _errorMessage = null;
    if (_state == TaskDetailState.error) {
      _setState(_task != null ? TaskDetailState.loaded : TaskDetailState.initial);
    } else {
      notifyListeners();
    }
  }

  void reset() {
    _state = TaskDetailState.initial;
    _task = null;
    _errorMessage = null;
    notifyListeners();
  }

  // Convenience getters for task properties
  String get taskTitle => _task?.title ?? '';
  String get taskDescription => _task?.description ?? '';
  TaskStatus get taskStatus => _task?.status ?? TaskStatus.todo;
  Priority get taskPriority => _task?.priority ?? Priority.medium;
  DateTime? get taskDueDate => _task?.dueDate;
  String? get taskProjectId => _task?.projectId;
  String? get taskAssigneeId => _task?.assigneeId;
  String get taskCreatorId => _task?.creatorId ?? '';

  // Task status checks
  bool get isTaskTodo => _task?.status == TaskStatus.todo;
  bool get isTaskInProgress => _task?.status == TaskStatus.inProgress;
  bool get isTaskDone => _task?.status == TaskStatus.done;
  
  // Task priority checks
  bool get isHighPriority => _task?.priority == Priority.high;
  bool get isMediumPriority => _task?.priority == Priority.medium;
  bool get isLowPriority => _task?.priority == Priority.low;
  
  bool get isAssigned => _task?.assigneeId != null;
} 