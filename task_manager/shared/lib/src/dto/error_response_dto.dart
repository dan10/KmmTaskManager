import 'package:freezed_annotation/freezed_annotation.dart';

part 'error_response_dto.freezed.dart';
part 'error_response_dto.g.dart';

@freezed
abstract class ErrorResponseDto with _$ErrorResponseDto {
  const factory ErrorResponseDto({
    required int statusCode,
    required String error, // e.g., "Not Found", "Bad Request"
    String? message,
    Map<String, dynamic>? details,
  }) = _ErrorResponseDto;

  factory ErrorResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ErrorResponseDtoFromJson(json);
} 