import 'package:freezed_annotation/freezed_annotation.dart';

part 'user_public_response_dto.freezed.dart';
part 'user_public_response_dto.g.dart';

@freezed
class UserPublicResponseDto with _$UserPublicResponseDto {
  const factory UserPublicResponseDto({
    required String id,
    required String name,
    required String email,
  }) = _UserPublicResponseDto;

  factory UserPublicResponseDto.fromJson(Map<String, dynamic> json) =>
      _$UserPublicResponseDtoFromJson(json);
}
