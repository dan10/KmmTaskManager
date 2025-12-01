import 'package:task_manager_shared/models.dart';

import '../services/task_api_service.dart';
import '../../../../core/utils/result.dart';

abstract class TaskRepository {
  Future<Result<PaginatedResponse<TaskDto>>> getTasks({int page = 0, int size = 20, String? query, String? projectId});
  Future<Result<TaskProgress>> getTaskProgress();
  Future<Result<TaskDto>> getTask(String id);
  Future<Result<TaskDto>> createTask(TaskCreateRequestDto request);
  Future<Result<TaskDto>> updateTask(String id, TaskUpdateRequestDto request);
  Future<Result<void>> deleteTask(String id);
  Future<Result<TaskDto>> changeTaskStatus(String id, TaskStatus status);
  Future<Result<TaskDto>> assignTask(String id, String assigneeId);
}

class TaskRepositoryImpl implements TaskRepository {
  TaskRepositoryImpl(this._apiService);
  final TaskApiService _apiService;

  @override
  Future<Result<PaginatedResponse<TaskDto>>> getTasks({int page = 0, int size = 20, String? query, String? projectId}) async {
    return _apiService.getTasks(page: page, size: size, query: query, projectId: projectId);
  }

  @override
  Future<Result<TaskProgress>> getTaskProgress() async {
    return _apiService.getTaskProgress();
  }

  @override
  Future<Result<TaskDto>> getTask(String id) async {
    return _apiService.getTask(id);
  }

  @override
  Future<Result<TaskDto>> createTask(TaskCreateRequestDto request) async {
    return _apiService.createTask(request);
  }

  @override
  Future<Result<TaskDto>> updateTask(String id, TaskUpdateRequestDto request) async {
    return _apiService.updateTask(id, request);
  }

  @override
  Future<Result<void>> deleteTask(String id) async {
    return _apiService.deleteTask(id);
  }

  @override
  Future<Result<TaskDto>> changeTaskStatus(String id, TaskStatus status) async {
    final req = TaskStatusChangeRequestDto(status: status);
    return _apiService.changeTaskStatus(id, req);
  }

  @override
  Future<Result<TaskDto>> assignTask(String id, String assigneeId) async {
    final req = TaskAssignRequestDto(assigneeId: assigneeId);
    return _apiService.assignTask(id, req);
  }
}


