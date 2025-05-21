import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:shared/models.dart'; // Assuming TaskStatus is exported from here

part 'task_status_change_request_dto.freezed.dart';
part 'task_status_change_request_dto.g.dart';

@freezed
class TaskStatusChangeRequestDto with _$TaskStatusChangeRequestDto {
  const factory TaskStatusChangeRequestDto({
    required TaskStatus status,
  }) = _TaskStatusChangeRequestDto;

  factory TaskStatusChangeRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskStatusChangeRequestDtoFromJson(json);
}
