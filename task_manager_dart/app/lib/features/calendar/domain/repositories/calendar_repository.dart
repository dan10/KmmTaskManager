import 'package:task_manager_shared/models.dart';

/// Domain repository interface for calendar
/// Follows the same pattern as TaskRepository
abstract class CalendarRepository {
  Future<PaginatedResponse<TaskDto>> getTasksDueOn({
    required DateTime date,
    int page = 0,
    int size = 20,
  });
}



