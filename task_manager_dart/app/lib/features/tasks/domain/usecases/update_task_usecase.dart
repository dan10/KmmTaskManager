import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/result.dart';
import '../../data/services/task_api_service.dart';

/// Use case for updating a task
/// Matches KMM's UpdateTaskUseCase
class UpdateTaskUseCase {
  final TaskApiService _apiService;

  UpdateTaskUseCase(this._apiService);

  Future<Result<TaskDto>> call(String taskId, TaskUpdateRequestDto request) async {
    return await _apiService.updateTask(taskId, request);
  }
}

