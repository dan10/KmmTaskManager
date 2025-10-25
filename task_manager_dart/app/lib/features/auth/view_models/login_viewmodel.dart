import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

import '../data/repositories/auth_repository.dart';
import '../../../core/utils/command.dart';
import '../../../core/utils/result.dart';

class LoginViewModel {
  LoginViewModel({required AuthRepository authRepository})
      : _authRepository = authRepository {
    login = Command1<void, (String email, String password)>(_login);
  }

  final AuthRepository _authRepository;
  final _log = Logger('LoginViewModel');

  late Command1 login;

  // Validation state
  final ValueNotifier<bool> emailHasError = ValueNotifier<bool>(false);
  final ValueNotifier<bool> passwordHasError = ValueNotifier<bool>(false);
  final ValueNotifier<bool> isFormValid = ValueNotifier<bool>(false);

  // Email validation
  bool _isEmailValid(String email) {
    if (email.isEmpty) return false;
    // Basic email regex pattern
    final emailRegex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    return emailRegex.hasMatch(email);
  }

  // Password validation
  bool _isPasswordValid(String password) {
    return password.length >= 8;
  }

  // Validate form
  void validateForm(String email, String password) {
    final emailValid = _isEmailValid(email);
    final passwordValid = _isPasswordValid(password);

    final newEmailHasError = email.isNotEmpty && !emailValid;
    final newPasswordHasError = password.isNotEmpty && !passwordValid;
    final newIsFormValid = emailValid && passwordValid;

    // Only update if values changed to prevent unnecessary rebuilds
    if (emailHasError.value != newEmailHasError) {
      emailHasError.value = newEmailHasError;
    }
    if (passwordHasError.value != newPasswordHasError) {
      passwordHasError.value = newPasswordHasError;
    }
    if (isFormValid.value != newIsFormValid) {
      isFormValid.value = newIsFormValid;
    }
  }

  // Clear validation errors
  void clearValidation() {
    emailHasError.value = false;
    passwordHasError.value = false;
    isFormValid.value = false;
  }

  Future<Result<void>> _login((String, String) credentials) async {
    final (email, password) = credentials;
    
    // Validate before attempting login
    validateForm(email, password);
    
    if (!isFormValid.value) {
      return Result.error(Exception('Please fix validation errors'));
    }

    final result = await _authRepository.login(
      email: email,
      password: password,
    );
    if (result is Error) {
      _log.warning('Login failed! ${result.error}');
    }
    return result;
  }

  void dispose() {
    emailHasError.dispose();
    passwordHasError.dispose();
    isFormValid.dispose();
  }
}


