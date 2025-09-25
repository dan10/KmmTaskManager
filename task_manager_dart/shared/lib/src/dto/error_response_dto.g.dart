// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'error_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_ErrorResponseDto _$ErrorResponseDtoFromJson(Map<String, dynamic> json) =>
    _ErrorResponseDto(
      statusCode: (json['statusCode'] as num).toInt(),
      error: json['error'] as String,
      message: json['message'] as String?,
      details: json['details'] as Map<String, dynamic>?,
    );

Map<String, dynamic> _$ErrorResponseDtoToJson(_ErrorResponseDto instance) =>
    <String, dynamic>{
      'statusCode': instance.statusCode,
      'error': instance.error,
      'message': instance.message,
      'details': instance.details,
    };
