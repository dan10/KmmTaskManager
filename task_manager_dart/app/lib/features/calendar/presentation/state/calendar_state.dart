import 'package:equatable/equatable.dart';
import 'package:task_manager_shared/models.dart';

/// State for the Calendar screen
class CalendarState extends Equatable {
  final DateTime selectedDate;
  final bool isLoading;
  final bool isRefreshing;
  final List<TaskDto> tasks;
  final String? errorMessage;
  final int currentPage;
  final int totalPages;
  final bool hasMorePages;
  final int totalTasks;

  const CalendarState({
    required this.selectedDate,
    this.isLoading = false,
    this.isRefreshing = false,
    this.tasks = const [],
    this.errorMessage,
    this.currentPage = 0,
    this.totalPages = 0,
    this.hasMorePages = false,
    this.totalTasks = 0,
  });

  CalendarState copyWith({
    DateTime? selectedDate,
    bool? isLoading,
    bool? isRefreshing,
    List<TaskDto>? tasks,
    String? errorMessage,
    int? currentPage,
    int? totalPages,
    bool? hasMorePages,
    int? totalTasks,
  }) {
    return CalendarState(
      selectedDate: selectedDate ?? this.selectedDate,
      isLoading: isLoading ?? this.isLoading,
      isRefreshing: isRefreshing ?? this.isRefreshing,
      tasks: tasks ?? this.tasks,
      errorMessage: errorMessage,
      currentPage: currentPage ?? this.currentPage,
      totalPages: totalPages ?? this.totalPages,
      hasMorePages: hasMorePages ?? this.hasMorePages,
      totalTasks: totalTasks ?? this.totalTasks,
    );
  }

  @override
  List<Object?> get props => [
        selectedDate,
        isLoading,
        isRefreshing,
        tasks,
        errorMessage,
        currentPage,
        totalPages,
        hasMorePages,
        totalTasks,
      ];
}
