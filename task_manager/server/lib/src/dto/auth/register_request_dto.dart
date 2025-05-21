import 'package:freezed_annotation/freezed_annotation.dart';

part 'register_request_dto.freezed.dart';
part 'register_request_dto.g.dart';

// It's good practice to ensure ValidationException is accessible.
// Assuming it's in:
import '../../../src/exceptions/custom_exceptions.dart';


@freezed
class RegisterRequestDto with _$RegisterRequestDto {
  const factory RegisterRequestDto({
    required String name,
    required String email,
    required String password,
  }) = _RegisterRequestDto;

  factory RegisterRequestDto.fromJson(Map<String, dynamic> json) =>
      _$RegisterRequestDtoFromJson(json);

  // Add the validation method
  void validateOrThrow() {
    final details = <String, String>{};

    if (name.isEmpty) {
      details['name'] = 'Name cannot be empty.';
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

    if (details.isNotEmpty) {
      throw ValidationException(
        message: 'Registration validation failed.',
        details: details,
      );
    }
  }
}
