import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_assign_request_dto.freezed.dart';
part 'task_assign_request_dto.g.dart';

import '../../../src/exceptions/custom_exceptions.dart';


@freezed
class TaskAssignRequestDto with _$TaskAssignRequestDto {
  const factory TaskAssignRequestDto({
    required String assigneeId,
  }) = _TaskAssignRequestDto;

  factory TaskAssignRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskAssignRequestDtoFromJson(json);

  void validateOrThrow() {
    final details = <String, String>{};
    if (assigneeId.isEmpty) {
      details['assigneeId'] = 'Assignee ID cannot be empty.';
    }
    if (details.isNotEmpty) {
      throw ValidationException(
        message: 'Task assignment validation failed.',
        details: details,
      );
    }
  }
}
