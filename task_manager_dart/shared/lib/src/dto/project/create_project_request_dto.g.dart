// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_project_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_CreateProjectRequestDto _$CreateProjectRequestDtoFromJson(
        Map<String, dynamic> json) =>
    _CreateProjectRequestDto(
      name: json['name'] as String,
      description: json['description'] as String?,
    );

Map<String, dynamic> _$CreateProjectRequestDtoToJson(
        _CreateProjectRequestDto instance) =>
    <String, dynamic>{
      'name': instance.name,
      'description': instance.description,
    };
