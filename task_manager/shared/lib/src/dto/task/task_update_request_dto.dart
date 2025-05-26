import 'package:freezed_annotation/freezed_annotation.dart';

import 'task_dto.dart';

part 'task_update_request_dto.freezed.dart';
part 'task_update_request_dto.g.dart';

@freezed
abstract class TaskUpdateRequestDto with _$TaskUpdateRequestDto {
  const factory TaskUpdateRequestDto({
    String? title,
    String? description,
    TaskStatus? status,
    Priority? priority,
    DateTime? dueDate,
    String? projectId,
    String? assigneeId,
  }) = _TaskUpdateRequestDto;

  factory TaskUpdateRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskUpdateRequestDtoFromJson(json);
}

extension TaskUpdateRequestDtoExtension on TaskUpdateRequestDto {
  Map<String, dynamic> toJsonWithNulls() {
    final result = <String, dynamic>{};
    
    if (title != null) result['title'] = title;
    if (description != null) result['description'] = description;
    if (status != null) result['status'] = status;
    if (priority != null) result['priority'] = priority;
    if (dueDate != null) result['dueDate'] = dueDate!.toIso8601String();
    if (projectId != null) result['projectId'] = projectId;
    if (assigneeId != null) result['assigneeId'] = assigneeId;
    
    return result;
  }

  Map<String, String> validate() {
    final details = <String, String>{};
    
    if (title != null) {
      if (title!.trim().isEmpty) {
        details['title'] = 'Title cannot be empty.';
      } else if (title!.trim().length < 3) {
        details['title'] = 'Title must be at least 3 characters long.';
      } else if (title!.trim().length > 100) {
        details['title'] = 'Title cannot exceed 100 characters.';
      }
    }
    
    if (description != null) {
      if (description!.trim().isEmpty) {
        details['description'] = 'Description cannot be empty.';
      } else if (description!.trim().length < 10) {
        details['description'] = 'Description must be at least 10 characters long.';
      } else if (description!.trim().length > 500) {
        details['description'] = 'Description cannot exceed 500 characters.';
      }
    }
    
    if (dueDate != null && dueDate!.isBefore(DateTime.now())) {
      details['dueDate'] = 'Due date cannot be in the past.';
    }
    
    return details;
  }

  bool get isValid => validate().isEmpty;
  
  bool get hasUpdates => title != null || 
                        description != null || 
                        status != null || 
                        priority != null || 
                        dueDate != null || 
                        projectId != null || 
                        assigneeId != null;
} 