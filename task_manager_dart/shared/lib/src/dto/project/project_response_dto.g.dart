// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'project_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_ProjectResponseDto _$ProjectResponseDtoFromJson(Map<String, dynamic> json) =>
    _ProjectResponseDto(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String?,
      completed: (json['completed'] as num?)?.toInt() ?? 0,
      inProgress: (json['inProgress'] as num?)?.toInt() ?? 0,
      total: (json['total'] as num?)?.toInt() ?? 0,
      creatorId: json['creatorId'] as String?,
      memberIds: (json['memberIds'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
      creator: json['creator'] == null
          ? null
          : User.fromJson(json['creator'] as Map<String, dynamic>),
      members: (json['members'] as List<dynamic>?)
              ?.map((e) => User.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
      createdAt: json['createdAt'] == null
          ? null
          : DateTime.parse(json['createdAt'] as String),
      updatedAt: json['updatedAt'] == null
          ? null
          : DateTime.parse(json['updatedAt'] as String),
    );

Map<String, dynamic> _$ProjectResponseDtoToJson(_ProjectResponseDto instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': instance.description,
      'completed': instance.completed,
      'inProgress': instance.inProgress,
      'total': instance.total,
      'creatorId': instance.creatorId,
      'memberIds': instance.memberIds,
      'creator': instance.creator,
      'members': instance.members,
      'createdAt': instance.createdAt?.toIso8601String(),
      'updatedAt': instance.updatedAt?.toIso8601String(),
    };
