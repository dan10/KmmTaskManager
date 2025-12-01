import 'package:equatable/equatable.dart';
import 'package:task_manager_shared/models.dart';

/// State for the Project Details screen
class ProjectDetailState extends Equatable {
  final bool isLoading;
  final bool isRefreshing;
  final Project? project;
  final String? errorMessage;

  const ProjectDetailState({
    this.isLoading = false,
    this.isRefreshing = false,
    this.project,
    this.errorMessage,
  });

  ProjectDetailState copyWith({
    bool? isLoading,
    bool? isRefreshing,
    Project? project,
    String? errorMessage,
  }) {
    return ProjectDetailState(
      isLoading: isLoading ?? this.isLoading,
      isRefreshing: isRefreshing ?? this.isRefreshing,
      project: project ?? this.project,
      errorMessage: errorMessage,
    );
  }

  @override
  List<Object?> get props => [
        isLoading,
        isRefreshing,
        project,
        errorMessage,
      ];
}

