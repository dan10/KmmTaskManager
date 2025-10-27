import 'package:equatable/equatable.dart';

/// State for the Projects screen
class ProjectsState extends Equatable {
  final String searchQuery;
  final bool isLoading;
  final bool isRefreshing;
  final int currentPage;
  final int totalPages;
  final bool hasMorePages;
  final String? errorMessage;

  const ProjectsState({
    this.searchQuery = '',
    this.isLoading = false,
    this.isRefreshing = false,
    this.currentPage = 0,
    this.totalPages = 0,
    this.hasMorePages = false,
    this.errorMessage,
  });

  ProjectsState copyWith({
    String? searchQuery,
    bool? isLoading,
    bool? isRefreshing,
    int? currentPage,
    int? totalPages,
    bool? hasMorePages,
    String? errorMessage,
  }) {
    return ProjectsState(
      searchQuery: searchQuery ?? this.searchQuery,
      isLoading: isLoading ?? this.isLoading,
      isRefreshing: isRefreshing ?? this.isRefreshing,
      currentPage: currentPage ?? this.currentPage,
      totalPages: totalPages ?? this.totalPages,
      hasMorePages: hasMorePages ?? this.hasMorePages,
      errorMessage: errorMessage,
    );
  }

  @override
  List<Object?> get props => [
        searchQuery,
        isLoading,
        isRefreshing,
        currentPage,
        totalPages,
        hasMorePages,
        errorMessage,
      ];
}
