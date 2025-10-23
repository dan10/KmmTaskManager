import 'package:task_manager_shared/models.dart';

/// Domain repository interface for tasks
/// Matches KMM's TaskRepository interface
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

