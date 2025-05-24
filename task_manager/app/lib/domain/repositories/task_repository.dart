import '../entities/task.dart';

abstract class TaskRepository {
  Future<List<Task>> getTasks({String? projectId});
  Future<Task> getTask(String id);
  Future<Task> createTask({
    required String title,
    String? description,
    required TaskPriority priority,
    required String projectId,
    String? assigneeId,
    DateTime? dueDate,
  });
  Future<Task> updateTask(String id, {
    String? title,
    String? description,
    TaskStatus? status,
    TaskPriority? priority,
    String? assigneeId,
    DateTime? dueDate,
  });
  Future<void> deleteTask(String id);
} 