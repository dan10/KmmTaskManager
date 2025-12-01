import '../../../../core/utils/result.dart';
import '../../data/repositories/task_repository.dart';

/// Use case for deleting a task
/// Matches KMM's DeleteTaskUseCase
class DeleteTaskUseCase {
  final TaskRepository _repository;

  DeleteTaskUseCase(this._repository);

  Future<Result<void>> call(String taskId) async {
    return await _repository.deleteTask(taskId);
  }
}

