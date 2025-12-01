import 'package:equatable/equatable.dart';
import 'package:task_manager_shared/models.dart';

/// State for the Tasks screen matching KMM's TasksState
class TasksState extends Equatable {
  final bool isLoading;
  final bool isRefreshing;
  final int completedTasks;
  final int totalTasks;
  final String searchQuery;
  final List<TaskDto> tasks;
  final String? errorMessage;
  final int currentPage;
  final int totalPages;
  final bool hasMorePages;

  const TasksState({
    this.isLoading = false,
    this.isRefreshing = false,
    this.completedTasks = 0,
    this.totalTasks = 0,
    this.searchQuery = '',
    this.tasks = const [],
    this.errorMessage,
    this.currentPage = 0,
    this.totalPages = 0,
    this.hasMorePages = false,
  });

  TasksState copyWith({
    bool? isLoading,
    bool? isRefreshing,
    int? completedTasks,
    int? totalTasks,
    String? searchQuery,
    List<TaskDto>? tasks,
    String? errorMessage,
    int? currentPage,
    int? totalPages,
    bool? hasMorePages,
  }) {
    return TasksState(
      isLoading: isLoading ?? this.isLoading,
      isRefreshing: isRefreshing ?? this.isRefreshing,
      completedTasks: completedTasks ?? this.completedTasks,
      totalTasks: totalTasks ?? this.totalTasks,
      searchQuery: searchQuery ?? this.searchQuery,
      tasks: tasks ?? this.tasks,
      errorMessage: errorMessage,
      currentPage: currentPage ?? this.currentPage,
      totalPages: totalPages ?? this.totalPages,
      hasMorePages: hasMorePages ?? this.hasMorePages,
    );
  }

  @override
  List<Object?> get props => [
        isLoading,
        isRefreshing,
        completedTasks,
        totalTasks,
        searchQuery,
        tasks,
        errorMessage,
        currentPage,
        totalPages,
        hasMorePages,
      ];
}



