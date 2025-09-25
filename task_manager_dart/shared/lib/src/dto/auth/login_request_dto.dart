import 'package:freezed_annotation/freezed_annotation.dart';

part 'login_request_dto.freezed.dart';
part 'login_request_dto.g.dart';

@freezed
abstract class LoginRequestDto with _$LoginRequestDto {
  const factory LoginRequestDto({
    required String email,
    required String password,
  }) = _LoginRequestDto;

  factory LoginRequestDto.fromJson(Map<String, dynamic> json) =>
      _$LoginRequestDtoFromJson(json);
}

extension LoginRequestDtoExtension on LoginRequestDto {
  Map<String, String> validate() {
    final details = <String, String>{};

    if (email.isEmpty) {
      details['email'] = 'Email cannot be empty.';
    }

    if (password.isEmpty) {
      details['password'] = 'Password cannot be empty.';
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
} 