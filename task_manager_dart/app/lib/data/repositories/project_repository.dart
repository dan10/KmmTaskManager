import 'package:task_manager_shared/models.dart';

import '../services/project_api_service.dart';

abstract class ProjectRepository {
  Future<PaginatedResponse<Project>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  });
  
  Future<Project> getProject(String projectId);
  Future<Project> createProject(CreateProjectRequestDto request);
  Future<Project> updateProject(String projectId, ProjectUpdateRequestDto request);
  Future<void> deleteProject(String projectId);
  Future<Project> addMember(String projectId, String userId);
  Future<Project> removeMember(String projectId, String userId);
  Future<Map<String, dynamic>> getProjectStats(String projectId);
}

class ProjectRepositoryImpl implements ProjectRepository {
  final ProjectApiService _apiService;

  ProjectRepositoryImpl(this._apiService);

  @override
  Future<PaginatedResponse<Project>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    try {
      final response = await _apiService.getProjects(
        page: page,
        size: size,
        query: query,
      );

      return PaginatedResponse<Project>(
        items: response.items.map((dto) => dto.toProject()).toList(),
        page: response.page,
        size: response.size,
        total: response.total,
        totalPages: response.totalPages,
      );
    } catch (e) {
      throw Exception('Failed to fetch projects: ${e.toString()}');
    }
  }

  @override
  Future<Project> getProject(String projectId) async {
    try {
      if (projectId.trim().isEmpty) {
        throw Exception('Project ID cannot be empty');
      }

      final response = await _apiService.getProject(projectId);
      return response.toProject();
    } catch (e) {
      throw Exception('Failed to fetch project: ${e.toString()}');
    }
  }

  @override
  Future<Project> createProject(CreateProjectRequestDto request) async {
    try {
      // Validate request
      final validationErrors = request.validate();
      if (validationErrors.isNotEmpty) {
        final errorMessage = validationErrors.values.join(', ');
        throw Exception('Validation failed: $errorMessage');
      }

      final response = await _apiService.createProject(request);
      return response.toProject();
    } catch (e) {
      throw Exception('Failed to create project: ${e.toString()}');
    }
  }

  @override
  Future<Project> updateProject(String projectId, ProjectUpdateRequestDto request) async {
    try {
      if (projectId.trim().isEmpty) {
        throw Exception('Project ID cannot be empty');
      }

      // Validate request
      final validationErrors = request.validate();
      if (validationErrors.isNotEmpty) {
        final errorMessage = validationErrors.values.join(', ');
        throw Exception('Validation failed: $errorMessage');
      }

      // Check if there are actual updates
      if (!request.hasUpdates) {
        throw Exception('No updates provided');
      }

      final response = await _apiService.updateProject(projectId, request);
      return response.toProject();
    } catch (e) {
      throw Exception('Failed to update project: ${e.toString()}');
    }
  }

  @override
  Future<void> deleteProject(String projectId) async {
    try {
      if (projectId.trim().isEmpty) {
        throw Exception('Project ID cannot be empty');
      }

      await _apiService.deleteProject(projectId);
    } catch (e) {
      throw Exception('Failed to delete project: ${e.toString()}');
    }
  }

  @override
  Future<Project> addMember(String projectId, String userId) async {
    try {
      if (projectId.trim().isEmpty) {
        throw Exception('Project ID cannot be empty');
      }
      
      if (userId.trim().isEmpty) {
        throw Exception('User ID cannot be empty');
      }

      final response = await _apiService.addMember(projectId, userId);
      return response.toProject();
    } catch (e) {
      throw Exception('Failed to add member: ${e.toString()}');
    }
  }

  @override
  Future<Project> removeMember(String projectId, String userId) async {
    try {
      if (projectId.trim().isEmpty) {
        throw Exception('Project ID cannot be empty');
      }
      
      if (userId.trim().isEmpty) {
        throw Exception('User ID cannot be empty');
      }

      final response = await _apiService.removeMember(projectId, userId);
      return response.toProject();
    } catch (e) {
      throw Exception('Failed to remove member: ${e.toString()}');
    }
  }

  @override
  Future<Map<String, dynamic>> getProjectStats(String projectId) async {
    try {
      if (projectId.trim().isEmpty) {
        throw Exception('Project ID cannot be empty');
      }

      return await _apiService.getProjectStats(projectId);
    } catch (e) {
      throw Exception('Failed to fetch project statistics: ${e.toString()}');
    }
  }
} 