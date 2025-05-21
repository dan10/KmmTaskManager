import 'package:freezed_annotation/freezed_annotation.dart';

part 'login_request_dto.freezed.dart';
part 'login_request_dto.g.dart';

import '../../../src/exceptions/custom_exceptions.dart';


@freezed
class LoginRequestDto with _$LoginRequestDto {
  const factory LoginRequestDto({
    required String email,
    required String password,
  }) = _LoginRequestDto;

  factory LoginRequestDto.fromJson(Map<String, dynamic> json) =>
      _$LoginRequestDtoFromJson(json);

  void validateOrThrow() {
    final details = <String, String>{};

    if (email.isEmpty) {
      details['email'] = 'Email cannot be empty.';
    }
    // Basic email format check could be added if desired, e.g. contains '@'
    // else if (!email.contains('@')) {
    //   details['email'] = 'Invalid email format.';
    // }

    if (password.isEmpty) {
      details['password'] = 'Password cannot be empty.';
    }

    if (details.isNotEmpty) {
      throw ValidationException(
        message: 'Login validation failed.',
        details: details,
      );
    }
  }
}
