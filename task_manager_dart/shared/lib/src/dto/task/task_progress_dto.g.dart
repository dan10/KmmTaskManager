// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_progress_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_TaskProgress _$TaskProgressFromJson(Map<String, dynamic> json) =>
    _TaskProgress(
      totalTasks: (json['totalTasks'] as num).toInt(),
      completedTasks: (json['completedTasks'] as num).toInt(),
    );

Map<String, dynamic> _$TaskProgressToJson(_TaskProgress instance) =>
    <String, dynamic>{
      'totalTasks': instance.totalTasks,
      'completedTasks': instance.completedTasks,
    };
