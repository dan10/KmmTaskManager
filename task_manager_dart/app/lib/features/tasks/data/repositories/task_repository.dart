import 'package:task_manager_shared/models.dart';

import '../../../../data/services/task_api_service.dart';
import '../../../../utils/result.dart';

abstract class TaskRepository {
  Future<Result<PaginatedResponse<TaskDto>>> getTasks({int page = 0, int size = 20, String? query, String? projectId});
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
    try {
      final res = await _apiService.getTasks(page: page, size: size, query: query, projectId: projectId);
      return Ok<PaginatedResponse<TaskDto>>(res);
    } catch (e, st) {
      return Error<PaginatedResponse<TaskDto>>(e, st);
    }
  }

  @override
  Future<Result<TaskDto>> getTask(String id) async {
    try {
      final res = await _apiService.getTask(id);
      return Ok<TaskDto>(res);
    } catch (e, st) {
      return Error<TaskDto>(e, st);
    }
  }

  @override
  Future<Result<TaskDto>> createTask(TaskCreateRequestDto request) async {
    try {
      final res = await _apiService.createTask(request);
      return Ok<TaskDto>(res);
    } catch (e, st) {
      return Error<TaskDto>(e, st);
    }
  }

  @override
  Future<Result<TaskDto>> updateTask(String id, TaskUpdateRequestDto request) async {
    try {
      final res = await _apiService.updateTask(id, request);
      return Ok<TaskDto>(res);
    } catch (e, st) {
      return Error<TaskDto>(e, st);
    }
  }

  @override
  Future<Result<void>> deleteTask(String id) async {
    try {
      await _apiService.deleteTask(id);
      return Ok<void>(null);
    } catch (e, st) {
      return Error<void>(e, st);
    }
  }

  @override
  Future<Result<TaskDto>> changeTaskStatus(String id, TaskStatus status) async {
    try {
      final req = TaskStatusChangeRequestDto(status: status);
      final res = await _apiService.changeTaskStatus(id, req);
      return Ok<TaskDto>(res);
    } catch (e, st) {
      return Error<TaskDto>(e, st);
    }
  }

  @override
  Future<Result<TaskDto>> assignTask(String id, String assigneeId) async {
    try {
      final req = TaskAssignRequestDto(assigneeId: assigneeId);
      final res = await _apiService.assignTask(id, req);
      return Ok<TaskDto>(res);
    } catch (e, st) {
      return Error<TaskDto>(e, st);
    }
  }
}


