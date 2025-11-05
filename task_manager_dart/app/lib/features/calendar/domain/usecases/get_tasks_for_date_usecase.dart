import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/result.dart';
import '../repositories/calendar_repository.dart';

/// Use case for getting tasks due on a specific date
class GetTasksForDateUseCase {
  final CalendarRepository _repository;

  GetTasksForDateUseCase(this._repository);

  Future<Result<PaginatedResponse<TaskDto>>> call({
    required DateTime date,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _repository.getTasksDueOn(
        date: date,
        page: page,
        size: size,
      );
      return Result.ok(response);
    } catch (e) {
      return Result.error(Exception(e.toString()));
    }
  }
}

