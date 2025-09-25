import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_assign_request_dto.freezed.dart';
part 'task_assign_request_dto.g.dart';

@freezed
abstract class TaskAssignRequestDto with _$TaskAssignRequestDto {
  const factory TaskAssignRequestDto({
    required String assigneeId,
  }) = _TaskAssignRequestDto;

  factory TaskAssignRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskAssignRequestDtoFromJson(json);
}

extension TaskAssignRequestDtoExtension on TaskAssignRequestDto {
  Map<String, String> validate() {
    final details = <String, String>{};
    if (assigneeId.isEmpty) {
      details['assigneeId'] = 'Assignee ID cannot be empty.';
    }
    return details;
  }

  bool get isValid => validate().isEmpty;
} 