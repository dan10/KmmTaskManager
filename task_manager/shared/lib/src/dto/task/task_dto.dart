import 'package:freezed_annotation/freezed_annotation.dart';

import '../user_public_response_dto.dart';

part 'task_dto.freezed.dart';
part 'task_dto.g.dart';

enum TaskStatus {
  @JsonValue('TODO')
  todo,
  @JsonValue('IN_PROGRESS')
  inProgress,
  @JsonValue('DONE')
  done
}

enum Priority {
  @JsonValue('LOW')
  low,
  @JsonValue('MEDIUM')
  medium,
  @JsonValue('HIGH')
  high
}

@freezed
abstract class TaskDto with _$TaskDto {
  const factory TaskDto({
    required String id,
    required String title,
    required String description,
    required TaskStatus status,
    required Priority priority,
    DateTime? dueDate,
    String? projectId,
    String? assigneeId,
    required String creatorId,
    DateTime? createdAt,
    DateTime? updatedAt,
    // Optional user information for API responses
    UserPublicResponseDto? assignee,
    UserPublicResponseDto? creator,
  }) = _TaskDto;

  factory TaskDto.fromJson(Map<String, dynamic> json) =>
      _$TaskDtoFromJson(json);
} 