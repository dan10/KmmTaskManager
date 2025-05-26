// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_update_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_TaskUpdateRequestDto _$TaskUpdateRequestDtoFromJson(
        Map<String, dynamic> json) =>
    _TaskUpdateRequestDto(
      title: json['title'] as String?,
      description: json['description'] as String?,
      status: $enumDecodeNullable(_$TaskStatusEnumMap, json['status']),
      priority: $enumDecodeNullable(_$PriorityEnumMap, json['priority']),
      dueDate: json['dueDate'] == null
          ? null
          : DateTime.parse(json['dueDate'] as String),
      projectId: json['projectId'] as String?,
      assigneeId: json['assigneeId'] as String?,
    );

Map<String, dynamic> _$TaskUpdateRequestDtoToJson(
        _TaskUpdateRequestDto instance) =>
    <String, dynamic>{
      'title': instance.title,
      'description': instance.description,
      'status': _$TaskStatusEnumMap[instance.status],
      'priority': _$PriorityEnumMap[instance.priority],
      'dueDate': instance.dueDate?.toIso8601String(),
      'projectId': instance.projectId,
      'assigneeId': instance.assigneeId,
    };

const _$TaskStatusEnumMap = {
  TaskStatus.todo: 'TODO',
  TaskStatus.inProgress: 'IN_PROGRESS',
  TaskStatus.done: 'DONE',
};

const _$PriorityEnumMap = {
  Priority.low: 'LOW',
  Priority.medium: 'MEDIUM',
  Priority.high: 'HIGH',
};
