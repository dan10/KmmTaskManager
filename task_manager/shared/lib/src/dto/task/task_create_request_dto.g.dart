// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_create_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_TaskCreateRequestDto _$TaskCreateRequestDtoFromJson(
        Map<String, dynamic> json) =>
    _TaskCreateRequestDto(
      title: json['title'] as String,
      description: json['description'] as String,
      priority: $enumDecode(_$PriorityEnumMap, json['priority']),
      dueDate: json['dueDate'] == null
          ? null
          : DateTime.parse(json['dueDate'] as String),
      projectId: json['projectId'] as String?,
      assigneeId: json['assigneeId'] as String?,
    );

Map<String, dynamic> _$TaskCreateRequestDtoToJson(
        _TaskCreateRequestDto instance) =>
    <String, dynamic>{
      'title': instance.title,
      'description': instance.description,
      'priority': _$PriorityEnumMap[instance.priority]!,
      'dueDate': instance.dueDate?.toIso8601String(),
      'projectId': instance.projectId,
      'assigneeId': instance.assigneeId,
    };

const _$PriorityEnumMap = {
  Priority.low: 'LOW',
  Priority.medium: 'MEDIUM',
  Priority.high: 'HIGH',
};
