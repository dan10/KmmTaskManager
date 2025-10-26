import 'package:task_manager_shared/models.dart';

import '../../../../core/constants/api_routes.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/utils/result.dart';

abstract class TaskApiService {
  Future<Result<PaginatedResponse<TaskDto>>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  });
  
  Future<Result<TaskProgress>> getTaskProgress();
  Future<Result<TaskDto>> getTask(String id);
  Future<Result<TaskDto>> createTask(TaskCreateRequestDto request);
  Future<Result<TaskDto>> updateTask(String id, TaskUpdateRequestDto request);
  Future<Result<void>> deleteTask(String id);
  Future<Result<TaskDto>> changeTaskStatus(String id, TaskStatusChangeRequestDto request);
  Future<Result<TaskDto>> assignTask(String id, TaskAssignRequestDto request);
}

class TaskApiServiceImpl implements TaskApiService {
  final ApiClient _client;

  TaskApiServiceImpl(this._client);

  @override
  Future<Result<PaginatedResponse<TaskDto>>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  }) async {
    final queryParams = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
      if (query != null && query.isNotEmpty) 'query': query,
    };

    return _client.get<PaginatedResponse<TaskDto>>(
      ApiRoutes.tasksAssigned,
      queryParameters: queryParams,
      fromJson: (json) => PaginatedResponse<TaskDto>.fromJson(
        json,
        (item) => TaskDto.fromJson(item as Map<String, dynamic>),
      ),
    );
  }

  @override
  Future<Result<TaskProgress>> getTaskProgress() async {
    return _client.get<TaskProgress>(
      ApiRoutes.tasksStats,
      fromJson: (json) => TaskProgress.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<TaskDto>> getTask(String id) async {
    return _client.get<TaskDto>(
      ApiRoutes.taskById(id),
      fromJson: (json) => TaskDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<TaskDto>> createTask(TaskCreateRequestDto request) async {
    return _client.post<TaskDto>(
      ApiRoutes.tasks,
      body: request.toJson(),
      fromJson: (json) => TaskDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<TaskDto>> updateTask(String id, TaskUpdateRequestDto request) async {
    return _client.put<TaskDto>(
      ApiRoutes.taskById(id),
      body: request.toJson(),
      fromJson: (json) => TaskDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<void>> deleteTask(String id) async {
    return _client.delete(ApiRoutes.taskById(id));
  }

  @override
  Future<Result<TaskDto>> changeTaskStatus(String id, TaskStatusChangeRequestDto request) async {
    return _client.post<TaskDto>(
      ApiRoutes.taskStatus(id),
      body: request.toJson(),
      fromJson: (json) => TaskDto.fromJson(json as Map<String, dynamic>),
    );
  }

  @override
  Future<Result<TaskDto>> assignTask(String id, TaskAssignRequestDto request) async {
    return _client.patch<TaskDto>(
      ApiRoutes.taskAssign(id),
      body: request.toJson(),
      fromJson: (json) => TaskDto.fromJson(json as Map<String, dynamic>),
    );
  }
} 