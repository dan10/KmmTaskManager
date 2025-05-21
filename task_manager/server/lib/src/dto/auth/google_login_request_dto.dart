import 'package:freezed_annotation/freezed_annotation.dart';

part 'google_login_request_dto.freezed.dart';
part 'google_login_request_dto.g.dart';

import '../../../src/exceptions/custom_exceptions.dart';


@freezed
class GoogleLoginRequestDto with _$GoogleLoginRequestDto {
  const factory GoogleLoginRequestDto({
    required String idToken,
  }) = _GoogleLoginRequestDto;

  factory GoogleLoginRequestDto.fromJson(Map<String, dynamic> json) =>
      _$GoogleLoginRequestDtoFromJson(json);

  void validateOrThrow() {
    final details = <String, String>{};

    if (idToken.isEmpty) {
      details['idToken'] = 'ID token cannot be empty.';
    }
    // Further validation of idToken format could be done here if needed,
    // but actual token verification happens in the AuthService.

    if (details.isNotEmpty) {
      throw ValidationException(
        message: 'Google login validation failed.',
        details: details,
      );
    }
  }
}
