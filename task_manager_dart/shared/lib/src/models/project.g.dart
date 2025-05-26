// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'project.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_Project _$ProjectFromJson(Map<String, dynamic> json) => _Project(
      id: json['id'] as String,
      name: json['name'] as String,
      completed: (json['completed'] as num?)?.toInt() ?? 0,
      inProgress: (json['inProgress'] as num?)?.toInt() ?? 0,
      total: (json['total'] as num?)?.toInt() ?? 0,
      description: json['description'] as String?,
      creatorId: json['creatorId'] as String?,
      memberIds: (json['memberIds'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
    );

Map<String, dynamic> _$ProjectToJson(_Project instance) => <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'completed': instance.completed,
      'inProgress': instance.inProgress,
      'total': instance.total,
      'description': instance.description,
      'creatorId': instance.creatorId,
      'memberIds': instance.memberIds,
    };
