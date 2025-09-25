import 'package:task_manager_shared/models.dart';

import '../services/task_api_service.dart';

abstract class TaskRepository {
  Future<PaginatedResponse<TaskDto>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  });
  
  Future<TaskDto> getTask(String id);
  Future<TaskDto> createTask(TaskCreateRequestDto request);
  Future<TaskDto> updateTask(String id, TaskUpdateRequestDto request);
  Future<void> deleteTask(String id);
  Future<TaskDto> changeTaskStatus(String id, TaskStatus status);
  Future<TaskDto> assignTask(String id, String assigneeId);
}

class TaskRepositoryImpl implements TaskRepository {
  final TaskApiService _apiService;

  TaskRepositoryImpl(this._apiService);

  @override
  Future<PaginatedResponse<TaskDto>> getTasks({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  }) async {
    try {
      final response = await _apiService.getTasks(
        page: page,
        size: size,
        query: query,
        projectId: projectId,
      );
      
      return response;
    } catch (e) {
      throw Exception('Failed to load tasks: ${e.toString()}');
    }
  }

  @override
  Future<TaskDto> getTask(String id) async {
    try {
      final response = await _apiService.getTask(id);
      return response;
    } catch (e) {
      throw Exception('Failed to load task: ${e.toString()}');
    }
  }

  @override
  Future<TaskDto> createTask(TaskCreateRequestDto request) async {
    // Validate the request
    if (!request.isValid) {
      final errors = request.validate();
      final errorMessage = errors.values.join(', ');
      throw Exception('Validation failed: $errorMessage');
    }

    try {
      final response = await _apiService.createTask(request);
      return response;
    } catch (e) {
      throw Exception('Failed to create task: ${e.toString()}');
    }
  }

  @override
  Future<TaskDto> updateTask(String id, TaskUpdateRequestDto request) async {
    // Validate the request
    if (!request.isValid) {
      final errors = request.validate();
      final errorMessage = errors.values.join(', ');
      throw Exception('Validation failed: $errorMessage');
    }

    // Check if there are any updates
    if (!request.hasUpdates) {
      throw Exception('No updates provided');
    }

    try {
      final response = await _apiService.updateTask(id, request);
      return response;
    } catch (e) {
      throw Exception('Failed to update task: ${e.toString()}');
    }
  }

  @override
  Future<void> deleteTask(String id) async {
    try {
      await _apiService.deleteTask(id);
    } catch (e) {
      throw Exception('Failed to delete task: ${e.toString()}');
    }
  }

  @override
  Future<TaskDto> changeTaskStatus(String id, TaskStatus status) async {
    try {
      final request = TaskStatusChangeRequestDto(status: status);
      final response = await _apiService.changeTaskStatus(id, request);
      return response;
    } catch (e) {
      throw Exception('Failed to change task status: ${e.toString()}');
    }
  }

  @override
  Future<TaskDto> assignTask(String id, String assigneeId) async {
    try {
      final request = TaskAssignRequestDto(assigneeId: assigneeId);
      final response = await _apiService.assignTask(id, request);
      return response;
    } catch (e) {
      throw Exception('Failed to assign task: ${e.toString()}');
    }
  }
} 