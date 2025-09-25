import 'package:freezed_annotation/freezed_annotation.dart';

import 'task_dto.dart';

part 'task_status_change_request_dto.freezed.dart';
part 'task_status_change_request_dto.g.dart';

@freezed
abstract class TaskStatusChangeRequestDto with _$TaskStatusChangeRequestDto {
  const factory TaskStatusChangeRequestDto({
    required TaskStatus status,
  }) = _TaskStatusChangeRequestDto;

  factory TaskStatusChangeRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskStatusChangeRequestDtoFromJson(json);
}

extension TaskStatusChangeRequestDtoExtension on TaskStatusChangeRequestDto {
  Map<String, String> validate() {
    // TaskStatus is an enum, so it's already validated
    return {};
  }

  bool get isValid => true;
} 