import 'package:freezed_annotation/freezed_annotation.dart';

part 'register_request_dto.freezed.dart';
part 'register_request_dto.g.dart';

@freezed
abstract class RegisterRequestDto with _$RegisterRequestDto {
  const factory RegisterRequestDto({
    required String displayName,
    required String email,
    required String password,
  }) = _RegisterRequestDto;

  factory RegisterRequestDto.fromJson(Map<String, dynamic> json) =>
      _$RegisterRequestDtoFromJson(json);
}

extension RegisterRequestDtoExtension on RegisterRequestDto {
  Map<String, String> validate() {
    final details = <String, String>{};

    if (displayName.isEmpty) {
      details['displayName'] = 'Display name cannot be empty.';
    }

    if (email.isEmpty) {
      details['email'] = 'Email cannot be empty.';
    } else if (!email.contains('@')) {
      details['email'] = 'Invalid email format.';
    }

    if (password.isEmpty) {
      details['password'] = 'Password cannot be empty.';
    } else if (password.length < 6) {
      details['password'] = 'Password must be at least 6 characters long.';
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
} 