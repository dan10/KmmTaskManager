import 'package:equatable/equatable.dart';
import 'package:task_manager_shared/models.dart';

/// State for the Task Details screen matching KMM's TasksDetailsState
class TaskDetailsState extends Equatable {
  final bool isLoading;
  final bool isDeleting;
  final TaskDto? task;
  final String? errorMessage;

  const TaskDetailsState({
    this.isLoading = false,
    this.isDeleting = false,
    this.task,
    this.errorMessage,
  });

  TaskDetailsState copyWith({
    bool? isLoading,
    bool? isDeleting,
    TaskDto? task,
    String? errorMessage,
  }) {
    return TaskDetailsState(
      isLoading: isLoading ?? this.isLoading,
      isDeleting: isDeleting ?? this.isDeleting,
      task: task ?? this.task,
      errorMessage: errorMessage,
    );
  }

  @override
  List<Object?> get props => [
        isLoading,
        isDeleting,
        task,
        errorMessage,
      ];
}

