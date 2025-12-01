import 'package:task_manager_shared/models.dart';
import '../../../../core/constants/api_routes.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/utils/result.dart';

/// Low-level API service for project HTTP calls
abstract class ProjectApiService {
  Future<Result<PaginatedResponse<ProjectResponseDto>>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  });

  Future<Result<ProjectResponseDto>> getProject(String projectId);

  Future<Result<ProjectResponseDto>> createProject(CreateProjectRequestDto request);

  Future<Result<ProjectResponseDto>> updateProject(
    String projectId,
    ProjectUpdateRequestDto request,
  );

  Future<Result<void>> deleteProject(String projectId);

  Future<Result<ProjectResponseDto>> addMember(String projectId, String userId);

  Future<Result<ProjectResponseDto>> removeMember(String projectId, String userId);

  Future<Result<Map<String, dynamic>>> getProjectStats(String projectId);
  
  Future<Result<PaginatedResponse<TaskDto>>> getProjectTasks({
    required String projectId,
    int page = 0,
    int size = 10,
    String? query,
  });
}

class ProjectApiServiceImpl implements ProjectApiService {
  final ApiClient _client;

  ProjectApiServiceImpl(this._client);

  @override
  Future<Result<PaginatedResponse<ProjectResponseDto>>> getProjects({
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    final queryParams = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
      if (query != null && query.isNotEmpty) 'query': query,
    };

    return _client.get<PaginatedResponse<ProjectResponseDto>>(
      ApiRoutes.projects,
      queryParameters: queryParams,
      fromJson: (json) {
        // Parse items list
        final items = (json['items'] as List?)
            ?.map((item) => ProjectResponseDto.fromJson(item as Map<String, dynamic>))
            .toList() ?? [];
        
        // Server sends: currentPage, pageSize, total, totalPages
        final page = json['currentPage'] is int 
            ? json['currentPage'] as int 
            : int.tryParse(json['currentPage']?.toString() ?? '0') ?? 0;
        
        final size = json['pageSize'] is int 
            ? json['pageSize'] as int 
            : int.tryParse(json['pageSize']?.toString() ?? '0') ?? 0;
        
        final total = json['total'] is int 
            ? json['total'] as int 
            : int.tryParse(json['total']?.toString() ?? '0') ?? 0;
        
        final totalPages = json['totalPages'] is int 
            ? json['totalPages'] as int 
            : int.tryParse(json['totalPages']?.toString() ?? '0') ?? 0;
        
        return PaginatedResponse<ProjectResponseDto>(
          items: items,
          page: page,
          size: size,
          total: total,
          totalPages: totalPages,
        );
      },
    );
  }

  @override
  Future<Result<ProjectResponseDto>> getProject(String projectId) async {
    return _client.get<ProjectResponseDto>(
      ApiRoutes.projectById(projectId),
      fromJson: (json) => ProjectResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<ProjectResponseDto>> createProject(
    CreateProjectRequestDto request,
  ) async {
    return _client.post<ProjectResponseDto>(
      ApiRoutes.projects,
      body: request.toJson(),
      fromJson: (json) => ProjectResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<ProjectResponseDto>> updateProject(
    String projectId,
    ProjectUpdateRequestDto request,
  ) async {
    return _client.put<ProjectResponseDto>(
      ApiRoutes.projectById(projectId),
      body: request.toJson(),
      fromJson: (json) => ProjectResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<void>> deleteProject(String projectId) async {
    return _client.delete(ApiRoutes.projectById(projectId));
  }

  @override
  Future<Result<ProjectResponseDto>> addMember(
    String projectId,
    String userId,
  ) async {
    return _client.post<ProjectResponseDto>(
      ApiRoutes.projectMembers(projectId),
      body: {'userId': userId},
      fromJson: (json) => ProjectResponseDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<ProjectResponseDto>> removeMember(
    String projectId,
    String userId,
  ) async {
    return _client.delete(ApiRoutes.projectMemberById(projectId, userId)).then((result) {
      // Delete returns void, but we need to fetch the updated project
      if (result is Ok) {
        return getProject(projectId);
      }
      return Result.error((result as Error).error);
    });
  }

  @override
  Future<Result<Map<String, dynamic>>> getProjectStats(String projectId) async {
    return _client.get<Map<String, dynamic>>(
      ApiRoutes.projectStats(projectId),
      fromJson: (json) => json as Map<String, dynamic>,
    );
  }

  @override
  Future<Result<PaginatedResponse<TaskDto>>> getProjectTasks({
    required String projectId,
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    final queryParams = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
      if (query != null && query.isNotEmpty) 'searchText': query,
    };

    return _client.get<PaginatedResponse<TaskDto>>(
      ApiRoutes.projectTasks(projectId),
      queryParameters: queryParams,
      fromJson: (json) {
        // Parse items list
        final items = (json['items'] as List?)
            ?.map((item) => TaskDto.fromJson(item as Map<String, dynamic>))
            .toList() ?? [];
        
        // Server sends: currentPage, pageSize, total, totalPages
        final page = json['currentPage'] is int 
            ? json['currentPage'] as int 
            : int.tryParse(json['currentPage']?.toString() ?? '0') ?? 0;
        
        final size = json['pageSize'] is int 
            ? json['pageSize'] as int 
            : int.tryParse(json['pageSize']?.toString() ?? '0') ?? 0;
        
        final total = json['total'] is int 
            ? json['total'] as int 
            : int.tryParse(json['total']?.toString() ?? '0') ?? 0;
        
        final totalPages = json['totalPages'] is int 
            ? json['totalPages'] as int 
            : int.tryParse(json['totalPages']?.toString() ?? '0') ?? 0;
        
        return PaginatedResponse<TaskDto>(
          items: items,
          page: page,
          size: size,
          total: total,
          totalPages: totalPages,
        );
      },
    );
  }
}
