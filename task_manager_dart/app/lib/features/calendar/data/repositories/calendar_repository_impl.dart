import 'package:task_manager_shared/models.dart';

import '../../../../core/utils/result.dart';
import '../../domain/repositories/calendar_repository.dart';
import '../services/calendar_api_service.dart';

class CalendarRepositoryImpl implements CalendarRepository {
  final CalendarApiService _apiService;

  CalendarRepositoryImpl(this._apiService);

  @override
  Future<PaginatedResponse<TaskDto>> getTasksDueOn({
    required DateTime date,
    int page = 0,
    int size = 20,
  }) async {
    final result = await _apiService.getTasksDueOn(
      date: date,
      page: page,
      size: size,
    );

    return switch (result) {
      Ok<PaginatedResponse<TaskDto>>() => result.value,
      Error<PaginatedResponse<TaskDto>>() => throw Exception(result.error),
    };
  }
}

