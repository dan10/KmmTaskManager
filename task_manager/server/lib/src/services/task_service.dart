import 'package:shared/src/models/task.dart' as shared;
import '../repositories/task_repository.dart';

abstract class TaskService {
  Future<List<shared.Task>> getAllTasks(String userId);
  Future<shared.Task?> getTaskById(String id, String userId);
  Future<shared.Task> createTask(shared.Task task);
  Future<shared.Task> updateTask(String id, shared.Task task, String userId);
  Future<void> deleteTask(String id, String userId);
}

class TaskServiceImpl implements TaskService {
  final TaskRepository _repository;

  TaskServiceImpl(this._repository);

  @override
  Future<List<shared.Task>> getAllTasks(String userId) async {
    return _repository.findAllByUserId(userId);
  }

  @override
  Future<shared.Task?> getTaskById(String id, String userId) async {
    final task = await _repository.findById(id);
    if (task == null ||
        (task.creatorId != userId && task.assigneeId != userId)) {
      return null;
    }
    return task;
  }

  @override
  Future<shared.Task> createTask(shared.Task task) async {
    return _repository.create(task);
  }

  @override
  Future<shared.Task> updateTask(
      String id, shared.Task task, String userId) async {
    final existingTask = await _repository.findById(id);
    if (existingTask == null ||
        (existingTask.creatorId != userId &&
            existingTask.assigneeId != userId)) {
      throw Exception('Task not found or unauthorized');
    }
    return _repository.update(task);
  }

  @override
  Future<void> deleteTask(String id, String userId) async {
    final task = await _repository.findById(id);
    if (task == null ||
        (task.creatorId != userId && task.assigneeId != userId)) {
      throw Exception('Task not found or unauthorized');
    }
    await _repository.delete(id);
  }
}
