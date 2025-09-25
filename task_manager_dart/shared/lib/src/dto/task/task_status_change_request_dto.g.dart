// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_status_change_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_TaskStatusChangeRequestDto _$TaskStatusChangeRequestDtoFromJson(
        Map<String, dynamic> json) =>
    _TaskStatusChangeRequestDto(
      status: $enumDecode(_$TaskStatusEnumMap, json['status']),
    );

Map<String, dynamic> _$TaskStatusChangeRequestDtoToJson(
        _TaskStatusChangeRequestDto instance) =>
    <String, dynamic>{
      'status': _$TaskStatusEnumMap[instance.status]!,
    };

const _$TaskStatusEnumMap = {
  TaskStatus.todo: 'TODO',
  TaskStatus.inProgress: 'IN_PROGRESS',
  TaskStatus.done: 'DONE',
};
