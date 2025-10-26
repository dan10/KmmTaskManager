import 'package:task_manager_shared/models.dart';
import '../../../../core/utils/result.dart';
import '../../domain/repositories/task_repository.dart';
import '../services/task_api_service.dart';

/// Implementation of TaskRepository
/// Matches KMM's TaskRepositoryImpl
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
    final result = await _apiService.getTasks(
      page: page,
      size: size,
      query: query,
      projectId: projectId,
    );
    
    switch (result) {
      case Ok<PaginatedResponse<TaskDto>>():
        return result.value;
      case Error<PaginatedResponse<TaskDto>>():
        throw Exception('Failed to load tasks: ${result.error}');
    }
  }

  @override
  Future<TaskDto> getTask(String id) async {
    final result = await _apiService.getTask(id);
    
    switch (result) {
      case Ok<TaskDto>():
        return result.value;
      case Error<TaskDto>():
        throw Exception('Failed to load task: ${result.error}');
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

    final result = await _apiService.createTask(request);
    
    switch (result) {
      case Ok<TaskDto>():
        return result.value;
      case Error<TaskDto>():
        throw Exception('Failed to create task: ${result.error}');
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

    final result = await _apiService.updateTask(id, request);
    
    switch (result) {
      case Ok<TaskDto>():
        return result.value;
      case Error<TaskDto>():
        throw Exception('Failed to update task: ${result.error}');
    }
  }

  @override
  Future<void> deleteTask(String id) async {
    final result = await _apiService.deleteTask(id);
    
    switch (result) {
      case Ok<void>():
        return;
      case Error<void>():
        throw Exception('Failed to delete task: ${result.error}');
    }
  }

  @override
  Future<TaskDto> changeTaskStatus(String id, TaskStatus status) async {
    final request = TaskStatusChangeRequestDto(status: status);
    final result = await _apiService.changeTaskStatus(id, request);
    
    switch (result) {
      case Ok<TaskDto>():
        return result.value;
      case Error<TaskDto>():
        throw Exception('Failed to change task status: ${result.error}');
    }
  }

  @override
  Future<TaskDto> assignTask(String id, String assigneeId) async {
    final request = TaskAssignRequestDto(assigneeId: assigneeId);
    final result = await _apiService.assignTask(id, request);
    
    switch (result) {
      case Ok<TaskDto>():
        return result.value;
      case Error<TaskDto>():
        throw Exception('Failed to assign task: ${result.error}');
    }
  }
}

