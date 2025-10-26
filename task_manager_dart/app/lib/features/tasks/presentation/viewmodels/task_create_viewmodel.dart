import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/task_repository.dart';
import '../../../../core/utils/command.dart';
import '../../../../core/utils/result.dart';

/// ViewModel for creating tasks
/// 
/// Follows the Command pattern from auth ViewModels and matches
/// KMM's TaskCreateViewModel structure with validation and state management
class TaskCreateViewModel {
  TaskCreateViewModel({
    required TaskRepository repository,
    String? projectId,
  })  : _repository = repository,
        _projectId = projectId {
    createTask = Command0<TaskDto>(_createTask);
  }

  final TaskRepository _repository;
  final _log = Logger('TaskCreateViewModel');

  String? _projectId;

  late Command0<TaskDto> createTask;

  // Form field controllers state (using ValueNotifiers like auth)
  final ValueNotifier<String> title = ValueNotifier<String>('');
  final ValueNotifier<String> description = ValueNotifier<String>('');
  final ValueNotifier<Priority> priority = ValueNotifier<Priority>(Priority.medium);
  final ValueNotifier<TaskStatus> status = ValueNotifier<TaskStatus>(TaskStatus.todo);
  final ValueNotifier<DateTime?> dueDate = ValueNotifier<DateTime?>(null);

  // Validation state
  final ValueNotifier<String?> titleError = ValueNotifier<String?>(null);
  final ValueNotifier<String?> descriptionError = ValueNotifier<String?>(null);
  final ValueNotifier<bool> isFormValid = ValueNotifier<bool>(false);

  // Title validation
  bool _isTitleValid(String value) {
    return value.trim().isNotEmpty && value.trim().length <= 100;
  }

  // Description validation
  bool _isDescriptionValid(String value) {
    return value.trim().isEmpty || value.trim().length <= 500;
  }

  /// Validate form and update validation state
  /// 
  /// Matches KMM's validateInput() method
  void validateForm() {
    final titleValue = title.value.trim();
    final descriptionValue = description.value.trim();

    final titleValid = _isTitleValid(titleValue);
    final descriptionValid = _isDescriptionValid(descriptionValue);

    // Update title error
    String? newTitleError;
    if (titleValue.isEmpty) {
      newTitleError = 'Title is required';
    } else if (titleValue.length > 100) {
      newTitleError = 'Title must be less than 100 characters';
    }

    // Update description error
    String? newDescriptionError;
    if (descriptionValue.length > 500) {
      newDescriptionError = 'Description must be less than 500 characters';
    }

    // Only update if values changed to prevent unnecessary rebuilds
    if (titleError.value != newTitleError) {
      titleError.value = newTitleError;
    }
    if (descriptionError.value != newDescriptionError) {
      descriptionError.value = newDescriptionError;
    }

    final newIsFormValid = titleValid && descriptionValid;
    if (isFormValid.value != newIsFormValid) {
      isFormValid.value = newIsFormValid;
    }
  }

  /// Clear all validation errors
  /// 
  /// Matches KMM's clearErrors() action
  void clearValidation() {
    titleError.value = null;
    descriptionError.value = null;
    isFormValid.value = false;
  }

  /// Clear all form fields
  void clearForm() {
    title.value = '';
    description.value = '';
    priority.value = Priority.medium;
    status.value = TaskStatus.todo;
    dueDate.value = null;
    clearValidation();
  }

  /// Update project ID
  /// 
  /// Matches KMM's updateProjectId action
  void setProjectId(String? projectId) {
    _projectId = projectId;
  }

  /// Create task command action
  /// 
  /// Matches KMM's createTask() method with validation
  Future<Result<TaskDto>> _createTask() async {
    // Validate before attempting creation
    validateForm();

    if (!isFormValid.value) {
      return Result.error(Exception('Please fix validation errors'));
    }

    final titleValue = title.value.trim();
    final descriptionValue = description.value.trim();

    try {
      final request = TaskCreateRequestDto(
        title: titleValue,
        description: descriptionValue,
        priority: priority.value,
        dueDate: dueDate.value,
        projectId: _projectId,
      );

      final result = await _repository.createTask(request);

      if (result is Ok<TaskDto>) {
        _log.info('Task created successfully: ${result.value.id}');
        // Clear form on success
        clearForm();
        return Result.ok(result.value);
      } else {
        final error = (result as Error<TaskDto>).error;
        _log.warning('Failed to create task: $error');
        return Result.error(error);
      }
    } catch (e) {
      _log.severe('Create task failed', e);
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }

  /// Dispose of all ValueNotifiers
  void dispose() {
    title.dispose();
    description.dispose();
    priority.dispose();
    status.dispose();
    dueDate.dispose();
    titleError.dispose();
    descriptionError.dispose();
    isFormValid.dispose();
  }
}

