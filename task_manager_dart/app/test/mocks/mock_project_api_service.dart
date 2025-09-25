import 'package:task_manager_shared/models.dart';

import '../../lib/data/services/project_api_service.dart';

class MockProjectApiService implements ProjectApiService {
  bool _shouldThrowError = false;
  bool _delayResponse = false;
  
  // Response configurations
  PaginatedResponse<ProjectResponseDto>? _getProjectsResponse;
  ProjectResponseDto? _getProjectResponse;
  ProjectResponseDto? _createProjectResponse;
  ProjectResponseDto? _updateProjectResponse;
  bool _deleteProjectSuccess = true;
  ProjectResponseDto? _addMemberResponse;
  ProjectResponseDto? _removeMemberResponse;
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

  void setDelayResponse(bool delay) {
    _delayResponse = delay;
  }

  void setGetProjectsResponse(PaginatedResponse<ProjectResponseDto> response) {
    _getProjectsResponse = response;
  }

  void setGetProjectResponse(ProjectResponseDto response) {
    _getProjectResponse = response;
  }

  void setCreateProjectResponse(ProjectResponseDto response) {
    _createProjectResponse = response;
  }

  void setUpdateProjectResponse(ProjectResponseDto response) {
    _updateProjectResponse = response;
  }

  void setDeleteProjectSuccess(bool success) {
    _deleteProjectSuccess = success;
  }

  void setAddMemberResponse(ProjectResponseDto response) {
    _addMemberResponse = response;
  }

  void setRemoveMemberResponse(ProjectResponseDto response) {
    _removeMemberResponse = response;
  }

  void setProjectStatsResponse(Map<String, dynamic> response) {
    _projectStatsResponse = response;
  }

  // Reset method
  void reset() {
    _shouldThrowError = false;
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
  Future<PaginatedResponse<ProjectResponseDto>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    await _simulateDelay();
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to fetch projects');
    }

    return _getProjectsResponse ?? PaginatedResponse<ProjectResponseDto>(
      items: [],
      page: page,
      size: size,
      total: 0,
      totalPages: 0,
    );
  }

  @override
  Future<ProjectResponseDto> getProject(String projectId) async {
    await _simulateDelay();
    lastProjectId = projectId;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to fetch project');
    }

    return _getProjectResponse ?? ProjectResponseDto(
      id: projectId,
      name: 'Mock Project',
      description: 'Mock Description',
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [],
      members: [],
    );
  }

  @override
  Future<ProjectResponseDto> createProject(CreateProjectRequestDto request) async {
    await _simulateDelay();
    lastCreateRequest = request;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to create project');
    }

    return _createProjectResponse ?? ProjectResponseDto(
      id: 'mock-id',
      name: request.name,
      description: request.description,
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [],
      members: [],
    );
  }

  @override
  Future<ProjectResponseDto> updateProject(String projectId, ProjectUpdateRequestDto request) async {
    await _simulateDelay();
    lastProjectId = projectId;
    lastUpdateRequest = request;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to update project');
    }

    return _updateProjectResponse ?? ProjectResponseDto(
      id: projectId,
      name: request.name ?? 'Updated Project',
      description: request.description,
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: request.memberIds ?? [],
      members: [],
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
  Future<ProjectResponseDto> addMember(String projectId, String userId) async {
    await _simulateDelay();
    lastAddMemberProjectId = projectId;
    lastAddMemberUserId = userId;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to add member');
    }

    return _addMemberResponse ?? ProjectResponseDto(
      id: projectId,
      name: 'Mock Project',
      description: 'Mock Description',
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [userId],
      members: [],
    );
  }

  @override
  Future<ProjectResponseDto> removeMember(String projectId, String userId) async {
    await _simulateDelay();
    lastRemoveMemberProjectId = projectId;
    lastRemoveMemberUserId = userId;
    
    if (_shouldThrowError) {
      throw Exception('Mock error: Failed to remove member');
    }

    return _removeMemberResponse ?? ProjectResponseDto(
      id: projectId,
      name: 'Mock Project',
      description: 'Mock Description',
      completed: 0,
      inProgress: 0,
      total: 0,
      memberIds: [],
      members: [],
    );
  }

  @override
  Future<Map<String, dynamic>> getProjectStats(String projectId) async {
    await _simulateDelay();
    lastStatsProjectId = projectId;
    
    if (_shouldThrowError) {
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