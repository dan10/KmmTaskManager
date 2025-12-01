import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/result.dart';
import '../../data/services/task_api_service.dart';

/// UseCase for fetching a single task by ID
class GetTaskUseCase {
  final TaskApiService _taskApiService;

  GetTaskUseCase(this._taskApiService);

  Future<Result<TaskDto>> call(String taskId) async {
    return await _taskApiService.getTask(taskId);
  }
}

