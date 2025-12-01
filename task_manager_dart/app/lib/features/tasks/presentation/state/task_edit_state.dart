import 'package:equatable/equatable.dart';
import 'package:task_manager_shared/models.dart';

/// State for the Task Edit screen matching KMM's EditTaskState
class TaskEditState extends Equatable {
  final bool isLoading;
  final bool isDeleting;
  final String title;
  final String description;
  final Priority priority;
  final TaskStatus status;
  final DateTime? dueDate;
  final String? projectId;
  final String? errorMessage;
  final TaskDto? originalTask;

  const TaskEditState({
    this.isLoading = false,
    this.isDeleting = false,
    this.title = '',
    this.description = '',
    this.priority = Priority.medium,
    this.status = TaskStatus.todo,
    this.dueDate,
    this.projectId,
    this.errorMessage,
    this.originalTask,
  });

  /// Check if the form has been modified
  bool get isModified {
    if (originalTask == null) return false;
    return title != originalTask!.title ||
        description != originalTask!.description ||
        priority != originalTask!.priority ||
        status != originalTask!.status ||
        dueDate != originalTask!.dueDate;
  }

  /// Check if the form is valid and can be submitted
  bool get isValid => title.trim().isNotEmpty;

  /// Check if the update button should be enabled
  bool get isButtonEnabled => isValid && isModified && !isLoading;

  TaskEditState copyWith({
    bool? isLoading,
    bool? isDeleting,
    String? title,
    String? description,
    Priority? priority,
    TaskStatus? status,
    DateTime? dueDate,
    String? projectId,
    String? errorMessage,
    TaskDto? originalTask,
    bool clearDueDate = false,
    bool clearError = false,
  }) {
    return TaskEditState(
      isLoading: isLoading ?? this.isLoading,
      isDeleting: isDeleting ?? this.isDeleting,
      title: title ?? this.title,
      description: description ?? this.description,
      priority: priority ?? this.priority,
      status: status ?? this.status,
      dueDate: clearDueDate ? null : (dueDate ?? this.dueDate),
      projectId: projectId ?? this.projectId,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
      originalTask: originalTask ?? this.originalTask,
    );
  }

  @override
  List<Object?> get props => [
        isLoading,
        isDeleting,
        title,
        description,
        priority,
        status,
        dueDate,
        projectId,
        errorMessage,
        originalTask,
      ];
}

