import 'package:task_manager_shared/models.dart';

import '../../../../data/services/project_api_service.dart';
import '../../../../utils/result.dart';

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
    try {
      final response = await _apiService.getProjects(page: page, size: size, query: query);
      final mapped = PaginatedResponse<Project>(
        items: response.items.map((dto) => dto.toProject()).toList(),
        page: response.page,
        size: response.size,
        total: response.total,
        totalPages: response.totalPages,
      );
      return Ok<PaginatedResponse<Project>>(mapped);
    } catch (e, st) {
      return Error<PaginatedResponse<Project>>(e, st);
    }
  }

  @override
  Future<Result<Project>> getProject(String id) async {
    try {
      final dto = await _apiService.getProject(id);
      return Ok<Project>(dto.toProject());
    } catch (e, st) {
      return Error<Project>(e, st);
    }
  }

  @override
  Future<Result<Project>> createProject({required String name, String? description}) async {
    try {
      final req = CreateProjectRequestDto(name: name, description: description);
      final dto = await _apiService.createProject(req);
      return Ok<Project>(dto.toProject());
    } catch (e, st) {
      return Error<Project>(e, st);
    }
  }

  @override
  Future<Result<Project>> updateProject(String id, {String? name, String? description}) async {
    try {
      final req = ProjectUpdateRequestDto(name: name, description: description);
      final dto = await _apiService.updateProject(id, req);
      return Ok<Project>(dto.toProject());
    } catch (e, st) {
      return Error<Project>(e, st);
    }
  }

  @override
  Future<Result<void>> deleteProject(String id) async {
    try {
      await _apiService.deleteProject(id);
      return Ok<void>(null);
    } catch (e, st) {
      return Error<void>(e, st);
    }
  }
}


