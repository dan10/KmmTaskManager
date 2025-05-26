import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/project_repository.dart';

enum ProjectListState {
  initial,
  loading,
  loaded,
  loadingMore,
  refreshing,
  error,
}

class ProjectListViewModel extends ChangeNotifier {
  final ProjectRepository _projectRepository;

  ProjectListState _state = ProjectListState.initial;
  List<Project> _projects = [];
  String? _errorMessage;
  
  // Pagination
  int _currentPage = 0;
  final int _pageSize = 10;
  int _totalElements = 0;
  int _totalPages = 0;
  bool _hasMorePages = true;
  
  // Search and filtering
  String _searchQuery = '';
  
  // Statistics
  int _totalProjects = 0;
  int _completedProjects = 0;
  double _averageCompletion = 0.0;

  // Getters
  ProjectListState get state => _state;
  List<Project> get projects => List.unmodifiable(_projects);
  String? get errorMessage => _errorMessage;
  
  // Pagination getters
  int get currentPage => _currentPage;
  int get pageSize => _pageSize;
  int get totalElements => _totalElements;
  int get totalPages => _totalPages;
  bool get hasMorePages => _hasMorePages;
  
  // Search getters
  String get searchQuery => _searchQuery;
  
  // Statistics getters
  int get totalProjects => _totalProjects;
  int get completedProjects => _completedProjects;
  double get averageCompletion => _averageCompletion;
  
  // State checks
  bool get isLoading => _state == ProjectListState.loading;
  bool get isLoadingMore => _state == ProjectListState.loadingMore;
  bool get isRefreshing => _state == ProjectListState.refreshing;
  bool get hasError => _state == ProjectListState.error;
  bool get isEmpty => _projects.isEmpty && _state == ProjectListState.loaded;
  bool get isNotEmpty => _projects.isNotEmpty;

  ProjectListViewModel(this._projectRepository);

  // Initialize and load first page
  Future<void> initialize() async {
    await loadProjects(refresh: true);
  }

  // Load projects with pagination
  Future<void> loadProjects({bool refresh = false}) async {
    if (refresh) {
      _currentPage = 0;
      _projects.clear();
      _hasMorePages = true;
      // Only set loading state if we're not already in refreshing state
      if (_state != ProjectListState.refreshing) {
        _setState(ProjectListState.loading);
      }
    } else if (!_hasMorePages || isLoadingMore) {
      return; // Don't load if no more pages or already loading
    } else {
      _setState(ProjectListState.loadingMore);
    }

    try {
      final response = await _projectRepository.getProjects(
        page: _currentPage,
        size: _pageSize,
        query: _searchQuery.isNotEmpty ? _searchQuery : null,
      );

      if (refresh) {
        _projects = response.items;
      } else {
        _projects.addAll(response.items);
      }

      _totalElements = response.total;
      _totalPages = response.totalPages;
      _hasMorePages = _currentPage < _totalPages - 1;
      _currentPage++;

      _updateStatistics();
      _setState(ProjectListState.loaded);
    } catch (e) {
      _setError('Failed to load projects: ${e.toString()}');
    }
  }

  // Load more projects (pagination)
  Future<void> loadMore() async {
    if (_hasMorePages && !isLoadingMore) {
      await loadProjects();
    }
  }

  // Refresh projects list
  Future<void> refresh() async {
    _setState(ProjectListState.refreshing);
    await loadProjects(refresh: true);
  }

  // Search projects
  Future<void> search(String query) async {
    if (_searchQuery != query) {
      _searchQuery = query;
      await loadProjects(refresh: true);
    }
  }

  // Clear search
  Future<void> clearSearch() async {
    if (_searchQuery.isNotEmpty) {
      _searchQuery = '';
      await loadProjects(refresh: true);
    }
  }

  // Create new project
  Future<bool> createProject({
    required String name,
    String? description,
  }) async {
    try {
      final request = CreateProjectRequestDto(
        name: name.trim(),
        description: description?.trim().isEmpty == true ? null : description?.trim(),
      );

      final newProject = await _projectRepository.createProject(request);
      
      // Add to the beginning of the list for immediate feedback
      _projects.insert(0, newProject);
      _totalElements++;
      _updateStatistics();
      
      notifyListeners();
      return true;
    } catch (e) {
      _setError('Failed to create project: ${e.toString()}');
      return false;
    }
  }

  // Update existing project
  Future<bool> updateProject({
    required String projectId,
    String? name,
    String? description,
    List<String>? memberIds,
  }) async {
    try {
      final request = ProjectUpdateRequestDto(
        name: name?.trim(),
        description: description?.trim(),
        memberIds: memberIds,
      );

      final updatedProject = await _projectRepository.updateProject(projectId, request);
      
      // Update in the list
      final index = _projects.indexWhere((p) => p.id == projectId);
      if (index != -1) {
        _projects[index] = updatedProject;
        _updateStatistics();
        notifyListeners();
      }
      
      return true;
    } catch (e) {
      _setError('Failed to update project: ${e.toString()}');
      return false;
    }
  }

  // Delete project
  Future<bool> deleteProject(String projectId) async {
    try {
      await _projectRepository.deleteProject(projectId);
      
      // Remove from the list
      _projects.removeWhere((p) => p.id == projectId);
      _totalElements--;
      _updateStatistics();
      
      notifyListeners();
      return true;
    } catch (e) {
      _setError('Failed to delete project: ${e.toString()}');
      return false;
    }
  }

  // Add member to project
  Future<bool> addMember(String projectId, String userId) async {
    try {
      final updatedProject = await _projectRepository.addMember(projectId, userId);
      
      // Update in the list
      final index = _projects.indexWhere((p) => p.id == projectId);
      if (index != -1) {
        _projects[index] = updatedProject;
        notifyListeners();
      }
      
      return true;
    } catch (e) {
      _setError('Failed to add member: ${e.toString()}');
      return false;
    }
  }

  // Remove member from project
  Future<bool> removeMember(String projectId, String userId) async {
    try {
      final updatedProject = await _projectRepository.removeMember(projectId, userId);
      
      // Update in the list
      final index = _projects.indexWhere((p) => p.id == projectId);
      if (index != -1) {
        _projects[index] = updatedProject;
        notifyListeners();
      }
      
      return true;
    } catch (e) {
      _setError('Failed to remove member: ${e.toString()}');
      return false;
    }
  }

  // Get project by ID
  Project? getProject(String projectId) {
    try {
      return _projects.firstWhere((p) => p.id == projectId);
    } catch (e) {
      return null;
    }
  }

  // Clear error
  void clearError() {
    _errorMessage = null;
    if (_state == ProjectListState.error) {
      _setState(_projects.isEmpty ? ProjectListState.initial : ProjectListState.loaded);
    }
  }

  // Reset to initial state
  void reset() {
    _setState(ProjectListState.initial);
    _projects.clear();
    _currentPage = 0;
    _totalElements = 0;
    _totalPages = 0;
    _hasMorePages = true;
    _searchQuery = '';
    _errorMessage = null;
    _resetStatistics();
    notifyListeners();
  }

  // Private methods
  void _setState(ProjectListState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _setState(ProjectListState.error);
  }

  void _updateStatistics() {
    _totalProjects = _projects.length;
    _completedProjects = _projects.where((p) => p.total > 0 && p.completed == p.total).length;
    
    if (_projects.isNotEmpty) {
      final totalCompletion = _projects
          .where((p) => p.total > 0)
          .map((p) => p.completed / p.total)
          .fold(0.0, (sum, completion) => sum + completion);
      
      final projectsWithTasks = _projects.where((p) => p.total > 0).length;
      _averageCompletion = projectsWithTasks > 0 ? totalCompletion / projectsWithTasks : 0.0;
    } else {
      _averageCompletion = 0.0;
    }
  }

  void _resetStatistics() {
    _totalProjects = 0;
    _completedProjects = 0;
    _averageCompletion = 0.0;
  }
} 