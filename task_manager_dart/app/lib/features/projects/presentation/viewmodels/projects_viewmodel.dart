import 'package:flutter/foundation.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/project_repository.dart';
import '../state/projects_state.dart';
import '../../../../core/utils/result.dart';
import '../../../../core/utils/command.dart';

/// ViewModel for the Projects screen
/// Follows Flutter's best practices with Command pattern and pagination
class ProjectsViewModel extends ChangeNotifier {
  final ProjectRepository _repository;
  final _log = Logger('ProjectsViewModel');

  ProjectsViewModel({
    required ProjectRepository repository,
  }) : _repository = repository {
    // Initialize commands
    deleteProject = Command1<void, String>(_deleteProject);
    
    // Initialize pagination controller
    _pagingController = PagingController<int, Project>(
      getNextPageKey: (state) {
        if (state.lastPageIsEmpty) return null;
        return state.nextIntPageKey;
      },
      fetchPage: _fetchPage,
    );
  }

  // Commands
  late Command1<void, String> deleteProject;

  // Pagination controller
  late final PagingController<int, Project> _pagingController;
  PagingController<int, Project> get pagingController => _pagingController;

  // State
  ProjectsState _state = const ProjectsState();
  ProjectsState get state => _state;

  static const int _pageSize = 20;

  // Fetch page for pagination
  Future<List<Project>> _fetchPage(int pageKey) async {
    try {
      final result = await _repository.getProjects(
        page: pageKey - 1,
        size: _pageSize,
        query: _state.searchQuery.isEmpty ? null : _state.searchQuery,
      );

      if (result is Ok<PaginatedResponse<Project>>) {
        final response = result.value;
        final isLastPage = pageKey >= response.totalPages - 1;

        _state = _state.copyWith(
          currentPage: pageKey,
          totalPages: response.totalPages,
          hasMorePages: !isLastPage,
        );

        return response.items;
      } else {
        final error = (result as Error).error;
        throw error;
      }
    } catch (error) {
      rethrow;
    }
  }

  // Search functionality
  void setSearchQuery(String query) {
    if (_state.searchQuery == query) return;
    
    _state = _state.copyWith(searchQuery: query);
    notifyListeners();
    
    // Refresh pagination with new query
    _pagingController.refresh();
  }

  void clearSearch() {
    setSearchQuery('');
  }

  // Refresh projects
  Future<void> _refreshProjects() async {
    _state = _state.copyWith(isRefreshing: true);
    notifyListeners();

    _pagingController.refresh();

    _state = _state.copyWith(isRefreshing: false);
    notifyListeners();
  }

  // Command: Delete project
  Future<Result<void>> _deleteProject(String projectId) async {
    final result = await _repository.deleteProject(projectId);
    
    // Refresh regardless of result
    await _refreshProjects();
    
    return result is Ok
        ? Result.ok(null)
        : Result.error((result as Error).error);
  }

  // Public refresh method
  void refresh() => _refreshProjects();

  // Check and refresh if needed (called on screen creation)
  void checkAndRefresh() {
    // Only refresh if we don't have any items yet
    if (_pagingController.value.items == null || 
        _pagingController.value.items!.isEmpty) {
      _pagingController.refresh();
    }
  }

  @override
  void dispose() {
    deleteProject.dispose();
    _pagingController.dispose();
    super.dispose();
  }
}

