import 'package:freezed_annotation/freezed_annotation.dart';

import 'task_dto.dart';

part 'task_create_request_dto.freezed.dart';
part 'task_create_request_dto.g.dart';

@freezed
abstract class TaskCreateRequestDto with _$TaskCreateRequestDto {
  const factory TaskCreateRequestDto({
    required String title,
    required String description,
    required Priority priority,
    DateTime? dueDate,
    String? projectId,
    String? assigneeId,
  }) = _TaskCreateRequestDto;

  factory TaskCreateRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskCreateRequestDtoFromJson(json);
}

extension TaskCreateRequestDtoExtension on TaskCreateRequestDto {
  Map<String, String> validate() {
    final details = <String, String>{};
    
    if (title.trim().isEmpty) {
      details['title'] = 'Title cannot be empty.';
    } else if (title.trim().length < 3) {
      details['title'] = 'Title must be at least 3 characters long.';
    } else if (title.trim().length > 100) {
      details['title'] = 'Title cannot exceed 100 characters.';
    }
    
    if (description.trim().isEmpty) {
      details['description'] = 'Description cannot be empty.';
    } else if (description.trim().length < 10) {
      details['description'] = 'Description must be at least 10 characters long.';
    } else if (description.trim().length > 500) {
      details['description'] = 'Description cannot exceed 500 characters.';
    }
    
    if (dueDate != null && dueDate!.isBefore(DateTime.now())) {
      details['dueDate'] = 'Due date cannot be in the past.';
    }
    
    return details;
  }

  bool get isValid => validate().isEmpty;
} 