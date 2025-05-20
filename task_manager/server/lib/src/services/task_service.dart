import 'package:shared/src/models/task.dart' as shared;
import '../repositories/task_repository.dart';

abstract class TaskService {
  Future<shared.Task> createTask(shared.Task task);
  Future<shared.Task?> getTaskById(String id);
  Future<List<shared.Task>> getAllTasks();
  Future<shared.Task> updateTask(shared.Task task);
  Future<void> deleteTask(String id);
}

class TaskServiceImpl implements TaskService {
  final TaskRepository _repository;

  TaskServiceImpl(this._repository);

  @override
  Future<shared.Task> createTask(shared.Task task) {
    return _repository.create(task);
  }

  @override
  Future<shared.Task?> getTaskById(String id) {
    return _repository.findById(id);
  }

  @override
  Future<List<shared.Task>> getAllTasks() {
    return _repository.findAll();
  }

  @override
  Future<shared.Task> updateTask(shared.Task task) {
    return _repository.update(task);
  }

  @override
  Future<void> deleteTask(String id) {
    return _repository.delete(id);
  }
}
