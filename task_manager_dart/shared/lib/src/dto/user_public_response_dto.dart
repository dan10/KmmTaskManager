import 'package:freezed_annotation/freezed_annotation.dart';
import '../models/user.dart';

part 'user_public_response_dto.freezed.dart';

part 'user_public_response_dto.g.dart';

@freezed
abstract class UserPublicResponseDto with _$UserPublicResponseDto {
  const factory UserPublicResponseDto({
    required String id,
    required String displayName,
    required String email,
  }) = _UserPublicResponseDto;

  factory UserPublicResponseDto.fromJson(Map<String, dynamic> json) =>
      _$UserPublicResponseDtoFromJson(json);

  factory UserPublicResponseDto.fromUser(User user) {
    return UserPublicResponseDto(
      id: user.id,
      displayName: user.displayName,
      email: user.email,
    );
  }
} 