import 'package:task_manager_shared/models.dart';
import '../repositories/task_repository.dart';

/// Use case for updating task status
/// Matches KMM's UpdateTaskStatusUseCase
class UpdateTaskStatusUseCase {
  final TaskRepository _repository;

  UpdateTaskStatusUseCase(this._repository);

  Future<TaskDto> call(String taskId, TaskStatus status) async {
    return await _repository.changeTaskStatus(taskId, status);
  }
}

