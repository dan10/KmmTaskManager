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
    try {
      final res = await _apiService.getTasks(page: page, size: size, query: query, projectId: projectId);
      return Result.ok(res);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<TaskProgress>> getTaskProgress() async {
    try {
      final res = await _apiService.getTaskProgress();
      return Result.ok(res);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<TaskDto>> getTask(String id) async {
    try {
      final res = await _apiService.getTask(id);
      return Result.ok(res);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<TaskDto>> createTask(TaskCreateRequestDto request) async {
    try {
      final res = await _apiService.createTask(request);
      return Result.ok(res);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<TaskDto>> updateTask(String id, TaskUpdateRequestDto request) async {
    try {
      final res = await _apiService.updateTask(id, request);
      return Result.ok(res);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<void>> deleteTask(String id) async {
    try {
      await _apiService.deleteTask(id);
      return Result.ok(null);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<TaskDto>> changeTaskStatus(String id, TaskStatus status) async {
    try {
      final req = TaskStatusChangeRequestDto(status: status);
      final res = await _apiService.changeTaskStatus(id, req);
      return Result.ok(res);
    } on Exception catch (e) {
      return Result.error(e);
    }
  }

  @override
  Future<Result<TaskDto>> assignTask(String id, String assigneeId) async {
    try {
      final req = TaskAssignRequestDto(assigneeId: assigneeId);
      final res = await _apiService.assignTask(id, req);
      return Result.ok(res);
    } on Exception  catch (e) {
      return Result.error(e);
    }
  }
}


