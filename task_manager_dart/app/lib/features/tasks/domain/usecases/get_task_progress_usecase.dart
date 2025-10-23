import 'package:task_manager_shared/models.dart';
import '../repositories/task_repository.dart';

/// Model for task progress
class TaskProgress {
  final int completedTasks;
  final int totalTasks;

  TaskProgress({
    required this.completedTasks,
    required this.totalTasks,
  });
}

/// Use case for getting task progress (completed/total)
/// Matches KMM's GetTaskProgressUseCase
class GetTaskProgressUseCase {
  final TaskRepository _repository;

  GetTaskProgressUseCase(this._repository);

  Future<TaskProgress> call() async {
    try {
      // Get all tasks to calculate progress
      // In a real app, you might have a dedicated API endpoint for this
      final response = await _repository.getTasks(
        page: 0,
        size: 1000, // Get all tasks for progress calculation
      );

      final completedTasks = response.items
          .where((task) => task.status == TaskStatus.done)
          .length;

      return TaskProgress(
        completedTasks: completedTasks,
        totalTasks: response.total,
      );
    } catch (e) {
      rethrow;
    }
  }
}

