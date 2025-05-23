import 'package:shared/models.dart' as shared_models;
import '../repositories/task_repository.dart';
import '../exceptions/custom_exceptions.dart'; // Import new exceptions

// Old custom exceptions removed from here.

abstract class TaskService {
  Future<List<shared_models.Task>> getTasks({
    String? assigneeId,
    String? creatorId,
    String? projectId,
    String? query,
    int page = 0,
    int size = 10,
  });
  Future<shared_models.Task?> getTaskById(String id, String userId);
  Future<shared_models.Task> createTask(shared_models.Task task);
  Future<shared_models.Task> updateTask(
      String id, shared_models.Task task, String userId);
  Future<void> deleteTask(String id, String userId);
  Future<shared_models.Task> assignTask(
      String taskId, String assigneeId); // New
  Future<shared_models.Task> changeTaskStatus(
      String taskId, shared_models.TaskStatus newStatus); // New
}

class TaskServiceImpl implements TaskService {
  final TaskRepository _repository;

  TaskServiceImpl(this._repository);

  @override
  Future<List<shared_models.Task>> getTasks({
    String? assigneeId,
    String? creatorId,
    String? projectId,
    String? query,
    int page = 0,
    int size = 10,
  }) async {
    return _repository.getTasks(
      assigneeId: assigneeId,
      creatorId: creatorId,
      projectId: projectId,
      query: query,
      page: page,
      size: size,
    );
  }

  @override
  Future<shared_models.Task?> getTaskById(String id, String userId) async {
    final task = await _repository.findById(id);
    // Basic authorization: user must be creator or assignee to fetch by ID directly.
    // More granular access control could be added (e.g. project member).
    if (task == null ||
        (task.creatorId != userId && task.assigneeId != userId)) {
      return null;
    }
    return task;
  }

  @override
  Future<shared_models.Task> createTask(shared_models.Task task) async {
    // TODO: Add validation, e.g., check if project_id exists if provided.
    return _repository.create(task);
  }

  @override
  Future<shared_models.Task> updateTask(
      String id, shared_models.Task task, String userId) async {
    final existingTask = await _repository.findById(id);
    if (existingTask == null) {
      throw TaskNotFoundException(id: id);
    }
    // Authorization: user must be creator to update.
    if (existingTask.creatorId != userId) {
      throw ForbiddenException(
          message: 'User not authorized to update task $id.');
    }
    return _repository.update(task);
  }

  @override
  Future<void> deleteTask(String id, String userId) async {
    final task = await _repository.findById(id);
    if (task == null) {
      throw TaskNotFoundException(id: id);
    }
    // Authorization: user must be creator to delete.
    if (task.creatorId != userId) {
      throw ForbiddenException(
          message: 'User not authorized to delete task $id.');
    }
    await _repository.delete(id);
  }

  @override
  Future<shared_models.Task> assignTask(
      String taskId, String assigneeId) async {
    // Repository now throws TaskNotFoundException, UserNotFoundException
    return _repository.assignTask(taskId, assigneeId);
    // No need to check for null if repo guarantees to throw or return valid object.
  }

  @override
  Future<shared_models.Task> changeTaskStatus(
      String taskId, shared_models.TaskStatus newStatus) async {
    // Repository now throws TaskNotFoundException
    return _repository.changeTaskStatus(taskId, newStatus);
    // No need to check for null if repo guarantees to throw or return valid object.
  }
}
