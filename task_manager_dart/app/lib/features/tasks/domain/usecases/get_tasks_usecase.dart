import 'package:task_manager_shared/models.dart';
import '../../../../core/utils/result.dart';
import '../../data/repositories/task_repository.dart';

/// Use case for getting paginated tasks
/// Matches KMM's GetTasksUseCase
class GetTasksUseCase {
  final TaskRepository _repository;

  GetTasksUseCase(this._repository);

  Future<Result<PaginatedResponse<TaskDto>>> call({
    int page = 0,
    int size = 20,
    String? query,
    String? projectId,
  }) async {
    return await _repository.getTasks(
      page: page,
      size: size,
      query: query,
      projectId: projectId,
    );
  }
}

