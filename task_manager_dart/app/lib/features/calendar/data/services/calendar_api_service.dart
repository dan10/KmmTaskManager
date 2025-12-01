import 'package:task_manager_shared/models.dart';

import '../../../../core/constants/api_routes.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/utils/result.dart';

abstract class CalendarApiService {
  Future<Result<PaginatedResponse<TaskDto>>> getTasksDueOn({
    required DateTime date,
    int page = 0,
    int size = 20,
  });
}

class CalendarApiServiceImpl implements CalendarApiService {
  final ApiClient _client;

  CalendarApiServiceImpl(this._client);

  @override
  Future<Result<PaginatedResponse<TaskDto>>> getTasksDueOn({
    required DateTime date,
    int page = 0,
    int size = 20,
  }) async {
    final dateString = '${date.year.toString().padLeft(4, '0')}-'
        '${date.month.toString().padLeft(2, '0')}-'
        '${date.day.toString().padLeft(2, '0')}';
    
    final tzOffsetMinutes = date.timeZoneOffset.inMinutes;
    
    final queryParams = <String, String>{
      'date': dateString,
      'tzOffsetMinutes': tzOffsetMinutes.toString(),
      'page': page.toString(),
      'size': size.toString(),
    };

    return _client.get<PaginatedResponse<TaskDto>>(
      ApiRoutes.tasksAssignedDueOn,
      queryParameters: queryParams,
      fromJson: (json) => PaginatedResponse<TaskDto>.fromJson(
        json,
        (item) => TaskDto.fromJson(item as Map<String, dynamic>),
      ),
    );
  }
}
