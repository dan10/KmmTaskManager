import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/task_repository.dart';

enum TaskCreateEditState {
  initial,
  loading,
  loaded,
  saving,
  saved,
  deleting,
  deleted,
  error,
}

class TaskCreateEditViewModel extends ChangeNotifier {
  final TaskRepository _taskRepository;

  TaskCreateEditState _state = TaskCreateEditState.initial;
  TaskDto? _task;
  String? _errorMessage;
  
  // Form fields
  String _title = '';
  String _description = '';
  Priority _priority = Priority.medium;
  DateTime? _dueDate;
  String? _projectId;
  String? _assigneeId;

  // Validation
  String? _titleError;
  String? _descriptionError;

  // Getters
  TaskCreateEditState get state => _state;
  TaskDto? get task => _task;
  String? get errorMessage => _errorMessage;
  
  // Form field getters
  String get title => _title;
  String get description => _description;
  Priority get priority => _priority;
  DateTime? get dueDate => _dueDate;
  String? get projectId => _projectId;
  String? get assigneeId => _assigneeId;
  
  // Validation getters
  String? get titleError => _titleError;
  String? get descriptionError => _descriptionError;
  
  // State checks
  bool get isLoading => _state == TaskCreateEditState.loading;
  bool get isSaving => _state == TaskCreateEditState.saving;
  bool get isDeleting => _state == TaskCreateEditState.deleting;
  bool get hasTask => _task != null;
  bool get isEditing => _task != null;
  bool get isCreating => _task == null;
  
  // Validation
  bool get isValid => _title.trim().isNotEmpty && _projectId != null;
  bool get hasChanges => _hasFormChanges();

  TaskCreateEditViewModel(this._taskRepository);

  // Initialize for creating a new task
  void initializeForCreate({String? projectId}) {
    _setState(TaskCreateEditState.initial);
    _task = null;
    _clearForm();
    _projectId = projectId;
    notifyListeners();
  }

  // Initialize for editing an existing task
  Future<void> initializeForEdit(String taskId) async {
    _setState(TaskCreateEditState.loading);
    
    try {
      _task = await _taskRepository.getTask(taskId);
      _initializeFormFromTask();
      _setState(TaskCreateEditState.loaded);
    } catch (e) {
      _setError('Failed to load task: ${e.toString()}');
    }
  }

  // Update form fields
  void updateTitle(String title) {
    _title = title;
    _validateTitle();
    notifyListeners();
  }

  void updateDescription(String description) {
    _description = description;
    _validateDescription();
    notifyListeners();
  }

  void updatePriority(Priority priority) {
    _priority = priority;
    notifyListeners();
  }

  void updateDueDate(DateTime? dueDate) {
    _dueDate = dueDate;
    notifyListeners();
  }

  void updateProjectId(String? projectId) {
    _projectId = projectId;
    notifyListeners();
  }

  void updateAssigneeId(String? assigneeId) {
    _assigneeId = assigneeId;
    notifyListeners();
  }

  // Save task (create or update)
  Future<bool> saveTask() async {
    if (!_validateForm()) {
      _setError('Please fill in all required fields');
      return false;
    }

    _setState(TaskCreateEditState.saving);

    try {
      if (isCreating) {
        await _createTask();
      } else {
        await _updateTask();
      }
      
      _setState(TaskCreateEditState.saved);
      return true;
    } catch (e) {
      _setError(isCreating 
          ? 'Failed to create task: ${e.toString()}'
          : 'Failed to update task: ${e.toString()}');
      return false;
    }
  }

  // Delete task
  Future<bool> deleteTask() async {
    if (_task == null) return false;

    _setState(TaskCreateEditState.deleting);

    try {
      await _taskRepository.deleteTask(_task!.id);
      _task = null;
      _setState(TaskCreateEditState.deleted);
      return true;
    } catch (e) {
      _setError('Failed to delete task: ${e.toString()}');
      return false;
    }
  }

  // Clear error
  void clearError() {
    _errorMessage = null;
    if (_state == TaskCreateEditState.error) {
      _setState(_task != null ? TaskCreateEditState.loaded : TaskCreateEditState.initial);
    }
  }

  // Reset form
  void reset() {
    _setState(TaskCreateEditState.initial);
    _task = null;
    _clearForm();
    notifyListeners();
  }

  // Private methods
  void _setState(TaskCreateEditState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _setState(TaskCreateEditState.error);
  }

  void _clearForm() {
    _title = '';
    _description = '';
    _priority = Priority.medium;
    _dueDate = null;
    _projectId = null;
    _assigneeId = null;
    _errorMessage = null;
    _titleError = null;
    _descriptionError = null;
  }

  void _initializeFormFromTask() {
    if (_task != null) {
      _title = _task!.title;
      _description = _task!.description;
      _priority = _task!.priority;
      _dueDate = _task!.dueDate;
      _projectId = _task!.projectId;
      _assigneeId = _task!.assigneeId;
    }
  }

  Future<void> _createTask() async {
    if (_projectId == null) {
      throw Exception('Project ID is required for creating a task');
    }

    final description = _description.trim().isEmpty ? '' : _description.trim();

    final request = TaskCreateRequestDto(
      title: _title.trim(),
      description: description,
      priority: _priority,
      dueDate: _dueDate,
      projectId: _projectId,
      assigneeId: _assigneeId,
    );

    _task = await _taskRepository.createTask(request);
  }

  Future<void> _updateTask() async {
    if (_task == null) return;

    final request = TaskUpdateRequestDto(
      title: _title.trim() != _task!.title ? _title.trim() : null,
      description: _description.trim() != (_task!.description ?? '') 
          ? (_description.trim().isEmpty ? null : _description.trim()) 
          : null,
      priority: _priority != _task!.priority ? _priority : null,
      dueDate: _dueDate != _task!.dueDate ? _dueDate : null,
      projectId: _projectId != _task!.projectId ? _projectId : null,
      assigneeId: _assigneeId != _task!.assigneeId ? _assigneeId : null,
    );

    // Only update if there are actual changes
    if (request.hasUpdates) {
      _task = await _taskRepository.updateTask(_task!.id, request);
    }
  }

  bool _hasFormChanges() {
    if (_task == null) {
      return _title.trim().isNotEmpty || 
             _description.trim().isNotEmpty || 
             _priority != Priority.medium ||
             _dueDate != null ||
             _projectId != null ||
             _assigneeId != null;
    }

    return _title.trim() != _task!.title ||
           _description.trim() != (_task!.description ?? '') ||
           _priority != _task!.priority ||
           _dueDate != _task!.dueDate ||
           _projectId != _task!.projectId ||
           _assigneeId != _task!.assigneeId;
  }

  // Validation methods
  void _validateTitle() {
    if (_title.trim().isEmpty) {
      _titleError = 'Title cannot be empty';
    } else if (_title.trim().length < 3) {
      _titleError = 'Title must be at least 3 characters long';
    } else if (_title.trim().length > 100) {
      _titleError = 'Title cannot exceed 100 characters';
    } else {
      _titleError = null;
    }
  }

  void _validateDescription() {
    if (_description.trim().isNotEmpty) {
      if (_description.trim().length < 10) {
        _descriptionError = 'Description must be at least 10 characters long';
      } else if (_description.trim().length > 500) {
        _descriptionError = 'Description cannot exceed 500 characters';
      } else {
        _descriptionError = null;
      }
    } else {
      _descriptionError = null; // Description is optional
    }
  }

  bool _validateForm() {
    _validateTitle();
    _validateDescription();
    return _titleError == null && _descriptionError == null && _projectId != null;
  }
} 