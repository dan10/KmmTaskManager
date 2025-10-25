import 'package:task_manager_shared/models.dart';
import '../../../../core/utils/result.dart';
import '../../data/repositories/task_repository.dart';

/// Use case for getting task progress (completed/total)
/// Matches KMM's GetTaskProgressUseCase
/// Now uses dedicated /tasks/progress endpoint instead of paginated fetch
class GetTaskProgressUseCase {
  final TaskRepository _repository;

  GetTaskProgressUseCase(this._repository);

  Future<Result<TaskProgress>> call() async {
    try {
      final result = await _repository.getTaskProgress();
      return result;
    } catch (e) {
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }
}

