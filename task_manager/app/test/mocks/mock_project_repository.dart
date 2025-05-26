import 'package:task_manager_shared/models.dart';

import '../../lib/data/repositories/project_repository.dart';

class MockProjectRepository implements ProjectRepository {
  bool _shouldThrowError = false;
  bool _shouldThrowStatsError = false;
  bool _delayResponse = false;
  
  // Response configurations
  PaginatedResponse<Project>? _getProjectsResponse;
  Project? _getProjectResponse;
  Project? _createProjectResponse;
  Project? _updateProjectResponse;
  bool _deleteProjectSuccess = true;
  Project? _addMemberResponse;
  Project? _removeMemberResponse;
  Map<String, dynamic>? _projectStatsResponse;

  // Track method calls
  String? lastProjectId;
  CreateProjectRequestDto? lastCreateRequest;
  ProjectUpdateRequestDto? lastUpdateRequest;
  String? lastDeleteProjectId;
  String? lastAddMemberProjectId;
  String? lastAddMemberUserId;
  String? lastRemoveMemberProjectId;
  String? lastRemoveMemberUserId;
  String? lastStatsProjectId;

  // Configuration methods
  void setShouldThrowError(bool shouldThrow) {
    _shouldThrowError = shouldThrow;
  }

  void setShouldThrowStatsError(bool shouldThrow) {
    _shouldThrowStatsError = shouldThrow;
  }

  void setDelayResponse(bool delay) {
    _delayResponse = delay;
  }

  void setGetProjectsResponse(PaginatedResponse<Project> response) {
    _getProjectsResponse = response;
  }

  void setGetProjectResponse(Project response) {
    _getProjectResponse = response;
  }

  void setCreateProjectResponse(Project response) {
    _createProjectResponse = response;
  }

  void setUpdateProjectResponse(Project response) {
    _updateProjectResponse = response;
  }

  void setDeleteProjectSuccess(bool success) {
    _deleteProjectSuccess = success;
  }

  void setAddMemberResponse(Project response) {
    _addMemberResponse = response;
  }

  void setRemoveMemberResponse(Project response) {
    _removeMemberResponse = response;
  }

  void setProjectStatsResponse(Map<String, dynamic> response) {
    _projectStatsResponse = response;
  }

  // Reset method
  void reset() {
    _shouldThrowError = false;
    _shouldThrowStatsError = false;
    _delayResponse = false;
    _getProjectsResponse = null;
    _getProjectResponse = null;
    _createProjectResponse = null;
    _updateProjectResponse = null;
    _deleteProjectSuccess = true;
    _addMemberResponse = null;
    _removeMemberResponse = null;
    _projectStatsResponse = null;
    
    lastProjectId = null;
    lastCreateRequest = null;
    lastUpdateRequest = null;
    lastDeleteProjectId = null;
    lastAddMemberProjectId = null;
    lastAddMemberUserId = null;
    lastRemoveMemberProjectId = null;
    lastRemoveMemberUserId = null;
    lastStatsProjectId = null;
  }

  Future<void> _simulateDelay() async {
    if (_delayResponse) {
      await Future.delayed(const Duration(milliseconds: 100));
    }
  }

  @override
  Future<PaginatedResponse<Project>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    await _simulateDelay();
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to fetch projects');
    }

    return _getProjectsResponse ?? PaginatedResponse<Project>(
      items: [],
      page: page,
      size: size,
      total: 0,
      totalPages: 0,
    );
  }

  @override
  Future<Project> getProject(String projectId) async {
    await _simulateDelay();
    lastProjectId = projectId;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to fetch project');
    }

    return _getProjectResponse ?? const Project(
      id: 'mock-id',
      name: 'Mock Project',
      description: 'Mock Description',
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [],
    );
  }

  @override
  Future<Project> createProject(CreateProjectRequestDto request) async {
    await _simulateDelay();
    lastCreateRequest = request;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to create project');
    }

    return _createProjectResponse ?? Project(
      id: 'mock-id',
      name: request.name,
      description: request.description,
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: const [],
    );
  }

  @override
  Future<Project> updateProject(String projectId, ProjectUpdateRequestDto request) async {
    await _simulateDelay();
    lastProjectId = projectId;
    lastUpdateRequest = request;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to update project');
    }

    return _updateProjectResponse ?? Project(
      id: projectId,
      name: request.name ?? 'Updated Project',
      description: request.description,
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: request.memberIds ?? const [],
    );
  }

  @override
  Future<void> deleteProject(String projectId) async {
    await _simulateDelay();
    lastDeleteProjectId = projectId;
    
    if (_shouldThrowError || !_deleteProjectSuccess) {
      throw Exception('Mock error: Failed to delete project');
    }
  }

  @override
  Future<Project> addMember(String projectId, String userId) async {
    await _simulateDelay();
    lastAddMemberProjectId = projectId;
    lastAddMemberUserId = userId;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to add member');
    }

    return _addMemberResponse ?? Project(
      id: projectId,
      name: 'Mock Project',
      description: 'Mock Description',
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [userId],
    );
  }

  @override
  Future<Project> removeMember(String projectId, String userId) async {
    await _simulateDelay();
    lastRemoveMemberProjectId = projectId;
    lastRemoveMemberUserId = userId;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to remove member');
    }

    return _removeMemberResponse ?? const Project(
      id: 'mock-id',
      name: 'Mock Project',
      description: 'Mock Description',
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [],
    );
  }

  @override
  Future<Map<String, dynamic>> getProjectStats(String projectId) async {
    await _simulateDelay();
    lastStatsProjectId = projectId;
    
    if (_shouldThrowError || _shouldThrowStatsError) {
      throw Exception('Mock error: Failed to fetch project statistics');
    }

    return _projectStatsResponse ?? {
      'totalTasks': 0,
      'completedTasks': 0,
      'inProgressTasks': 0,
      'overdueTasks': 0,
    };
  }
} 