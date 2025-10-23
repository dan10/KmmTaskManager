import '../repositories/task_repository.dart';

/// Use case for deleting a task
/// Matches KMM's DeleteTaskUseCase
class DeleteTaskUseCase {
  final TaskRepository _repository;

  DeleteTaskUseCase(this._repository);

  Future<void> call(String taskId) async {
    await _repository.deleteTask(taskId);
  }
}

