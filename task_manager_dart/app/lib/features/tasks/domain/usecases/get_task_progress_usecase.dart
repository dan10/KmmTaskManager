import 'package:task_manager_shared/models.dart';
import '../../../../core/utils/result.dart';
import '../../data/repositories/task_repository.dart';

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

  Future<Result<TaskProgress>> call() async {
    try {
      // Get all tasks to calculate progress
      // In a real app, you might have a dedicated API endpoint for this
      final result = await _repository.getTasks(
        page: 0,
        size: 1000, // Get all tasks for progress calculation
      );

      if (result is Ok<PaginatedResponse<TaskDto>>) {
        final response = result.value;
        final completedTasks = response.items
            .where((task) => task.status == TaskStatus.done)
            .length;

        return Result.ok(TaskProgress(
          completedTasks: completedTasks,
          totalTasks: response.total,
        ));
      } else {
        return Result.error((result as Error).error);
      }
    } catch (e) {
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }
}

