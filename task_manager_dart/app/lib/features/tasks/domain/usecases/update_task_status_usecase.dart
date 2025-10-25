import 'package:task_manager_shared/models.dart';
import '../../../../core/utils/result.dart';
import '../../data/repositories/task_repository.dart';

/// Use case for updating task status
/// Matches KMM's UpdateTaskStatusUseCase
class UpdateTaskStatusUseCase {
  final TaskRepository _repository;

  UpdateTaskStatusUseCase(this._repository);

  Future<Result<TaskDto>> call(String taskId, TaskStatus status) async {
    return await _repository.changeTaskStatus(taskId, status);
  }
}

