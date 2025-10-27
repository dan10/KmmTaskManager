import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/result.dart';
import '../services/project_api_service.dart';

abstract class ProjectRepository {
  Future<Result<PaginatedResponse<Project>>> getProjects({int page = 0, int size = 10, String? query});
  Future<Result<Project>> getProject(String id);
  Future<Result<Project>> createProject({required String name, String? description});
  Future<Result<Project>> updateProject(String id, {String? name, String? description});
  Future<Result<void>> deleteProject(String id);
}

class ProjectRepositoryImpl implements ProjectRepository {
  ProjectRepositoryImpl(this._apiService);
  final ProjectApiService _apiService;

  @override
  Future<Result<PaginatedResponse<Project>>> getProjects({int page = 0, int size = 10, String? query}) async {
    final result = await _apiService.getProjects(page: page, size: size, query: query);
    
    if (result is Ok<PaginatedResponse<ProjectResponseDto>>) {
      final response = result.value;
      final mapped = PaginatedResponse<Project>(
        items: response.items.map((dto) => dto.toProject()).toList(),
        page: response.page,
        size: response.size,
        total: response.total,
        totalPages: response.totalPages,
      );
      return Result.ok(mapped);
    } else {
      return Result.error((result as Error).error);
    }
  }

  @override
  Future<Result<Project>> getProject(String id) async {
    final result = await _apiService.getProject(id);
    
    if (result is Ok<ProjectResponseDto>) {
      return Result.ok(result.value.toProject());
    } else {
      return Result.error((result as Error).error);
    }
  }

  @override
  Future<Result<Project>> createProject({required String name, String? description}) async {
    final req = CreateProjectRequestDto(name: name, description: description);
    final result = await _apiService.createProject(req);
    
    if (result is Ok<ProjectResponseDto>) {
      return Result.ok(result.value.toProject());
    } else {
      return Result.error((result as Error).error);
    }
  }

  @override
  Future<Result<Project>> updateProject(String id, {String? name, String? description}) async {
    final req = ProjectUpdateRequestDto(name: name, description: description);
    final result = await _apiService.updateProject(id, req);
    
    if (result is Ok<ProjectResponseDto>) {
      return Result.ok(result.value.toProject());
    } else {
      return Result.error((result as Error).error);
    }
  }

  @override
  Future<Result<void>> deleteProject(String id) async {
    return await _apiService.deleteProject(id);
  }
}


