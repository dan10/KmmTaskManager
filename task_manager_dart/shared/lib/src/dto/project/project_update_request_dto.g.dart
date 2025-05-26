// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'project_update_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_ProjectUpdateRequestDto _$ProjectUpdateRequestDtoFromJson(
        Map<String, dynamic> json) =>
    _ProjectUpdateRequestDto(
      name: json['name'] as String?,
      description: json['description'] as String?,
      memberIds: (json['memberIds'] as List<dynamic>?)
          ?.map((e) => e as String)
          .toList(),
    );

Map<String, dynamic> _$ProjectUpdateRequestDtoToJson(
        _ProjectUpdateRequestDto instance) =>
    <String, dynamic>{
      'name': instance.name,
      'description': instance.description,
      'memberIds': instance.memberIds,
    };
