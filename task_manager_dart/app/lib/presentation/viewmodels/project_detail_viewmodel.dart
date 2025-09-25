import 'package:flutter/foundation.dart';
import 'package:task_manager_shared/models.dart';

import '../../data/repositories/project_repository.dart';

enum ProjectDetailState {
  initial,
  loading,
  loaded,
  updating,
  deleting,
  error,
}

class ProjectDetailViewModel extends ChangeNotifier {
  final ProjectRepository _projectRepository;

  ProjectDetailState _state = ProjectDetailState.initial;
  Project? _project;
  String? _errorMessage;
  Map<String, dynamic>? _statistics;

  // Getters
  ProjectDetailState get state => _state;
  Project? get project => _project;
  String? get errorMessage => _errorMessage;
  Map<String, dynamic>? get statistics => _statistics;

  // State checks
  bool get isLoading => _state == ProjectDetailState.loading;
  bool get isUpdating => _state == ProjectDetailState.updating;
  bool get isDeleting => _state == ProjectDetailState.deleting;
  bool get hasError => _state == ProjectDetailState.error;
  bool get hasProject => _project != null;

  // Project properties (safe getters)
  String get projectId => _project?.id ?? '';
  String get projectName => _project?.name ?? '';
  String get projectDescription => _project?.description ?? '';
  List<String> get memberIds => _project?.memberIds ?? [];
  int get completedTasks => _project?.completed ?? 0;
  int get inProgressTasks => _project?.inProgress ?? 0;
  int get totalTasks => _project?.total ?? 0;
  
  // Calculated properties
  double get completionPercentage {
    if (_project == null || _project!.total == 0) return 0.0;
    return (_project!.completed / _project!.total) * 100;
  }

  bool get isCompleted => _project != null && _project!.total > 0 && _project!.completed == _project!.total;
  bool get hasMembers => _project != null && _project!.memberIds.isNotEmpty;
  int get memberCount => _project?.memberIds.length ?? 0;

  ProjectDetailViewModel(this._projectRepository);

  // Load project details
  Future<void> loadProject(String projectId) async {
    _setState(ProjectDetailState.loading);

    try {
      _project = await _projectRepository.getProject(projectId);
      await _loadStatistics(projectId);
      _setState(ProjectDetailState.loaded);
    } catch (e) {
      _setError('Failed to load project: ${e.toString()}');
    }
  }

  // Update project
  Future<bool> updateProject({
    String? name,
    String? description,
    List<String>? memberIds,
  }) async {
    if (_project == null) return false;

    _setState(ProjectDetailState.updating);

    try {
      final request = ProjectUpdateRequestDto(
        name: name?.trim(),
        description: description?.trim(),
        memberIds: memberIds,
      );

      _project = await _projectRepository.updateProject(_project!.id, request);
      _setState(ProjectDetailState.loaded);
      return true;
    } catch (e) {
      _setError('Failed to update project: ${e.toString()}');
      return false;
    }
  }

  // Delete project
  Future<bool> deleteProject() async {
    if (_project == null) return false;

    _setState(ProjectDetailState.deleting);

    try {
      await _projectRepository.deleteProject(_project!.id);
      _project = null;
      _statistics = null;
      _setState(ProjectDetailState.initial);
      return true;
    } catch (e) {
      _setError('Failed to delete project: ${e.toString()}');
      return false;
    }
  }

  // Add member to project
  Future<bool> addMember(String userId) async {
    if (_project == null) return false;

    try {
      _project = await _projectRepository.addMember(_project!.id, userId);
      notifyListeners();
      return true;
    } catch (e) {
      _setError('Failed to add member: ${e.toString()}');
      return false;
    }
  }

  // Remove member from project
  Future<bool> removeMember(String userId) async {
    if (_project == null) return false;

    try {
      _project = await _projectRepository.removeMember(_project!.id, userId);
      notifyListeners();
      return true;
    } catch (e) {
      _setError('Failed to remove member: ${e.toString()}');
      return false;
    }
  }

  // Refresh project data
  Future<void> refresh() async {
    if (_project != null) {
      await loadProject(_project!.id);
    }
  }

  // Load project statistics
  Future<void> _loadStatistics(String projectId) async {
    try {
      _statistics = await _projectRepository.getProjectStats(projectId);
    } catch (e) {
      // Statistics are optional, don't fail the whole operation
      _statistics = null;
    }
  }

  // Check if user is member of the project
  bool isMember(String userId) {
    return _project?.memberIds.contains(userId) ?? false;
  }

  // Get statistics value safely
  T? getStatistic<T>(String key) {
    if (_statistics == null) return null;
    return _statistics![key] as T?;
  }

  // Clear error
  void clearError() {
    _errorMessage = null;
    if (_state == ProjectDetailState.error) {
      _setState(_project != null ? ProjectDetailState.loaded : ProjectDetailState.initial);
    }
  }

  // Reset to initial state
  void reset() {
    _setState(ProjectDetailState.initial);
    _project = null;
    _statistics = null;
    _errorMessage = null;
    notifyListeners();
  }

  // Private methods
  void _setState(ProjectDetailState newState) {
    _state = newState;
    notifyListeners();
  }

  void _setError(String error) {
    _errorMessage = error;
    _setState(ProjectDetailState.error);
  }
} 