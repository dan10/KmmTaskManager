// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_TaskDto _$TaskDtoFromJson(Map<String, dynamic> json) => _TaskDto(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String,
      status: $enumDecode(_$TaskStatusEnumMap, json['status']),
      priority: $enumDecode(_$PriorityEnumMap, json['priority']),
      dueDate: json['dueDate'] == null
          ? null
          : DateTime.parse(json['dueDate'] as String),
      projectId: json['projectId'] as String?,
      assigneeId: json['assigneeId'] as String?,
      creatorId: json['creatorId'] as String,
      createdAt: json['createdAt'] == null
          ? null
          : DateTime.parse(json['createdAt'] as String),
      updatedAt: json['updatedAt'] == null
          ? null
          : DateTime.parse(json['updatedAt'] as String),
      assignee: json['assignee'] == null
          ? null
          : UserPublicResponseDto.fromJson(
              json['assignee'] as Map<String, dynamic>),
      creator: json['creator'] == null
          ? null
          : UserPublicResponseDto.fromJson(
              json['creator'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$TaskDtoToJson(_TaskDto instance) => <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'description': instance.description,
      'status': _$TaskStatusEnumMap[instance.status]!,
      'priority': _$PriorityEnumMap[instance.priority]!,
      'dueDate': instance.dueDate?.toIso8601String(),
      'projectId': instance.projectId,
      'assigneeId': instance.assigneeId,
      'creatorId': instance.creatorId,
      'createdAt': instance.createdAt?.toIso8601String(),
      'updatedAt': instance.updatedAt?.toIso8601String(),
      'assignee': instance.assignee,
      'creator': instance.creator,
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
