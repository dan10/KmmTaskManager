// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_public_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_UserPublicResponseDto _$UserPublicResponseDtoFromJson(
        Map<String, dynamic> json) =>
    _UserPublicResponseDto(
      id: json['id'] as String,
      displayName: json['displayName'] as String,
      email: json['email'] as String,
    );

Map<String, dynamic> _$UserPublicResponseDtoToJson(
        _UserPublicResponseDto instance) =>
    <String, dynamic>{
      'id': instance.id,
      'displayName': instance.displayName,
      'email': instance.email,
    };
