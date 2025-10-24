import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

import '../data/repositories/auth_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

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

    emailHasError.value = email.isNotEmpty && !emailValid;
    passwordHasError.value = password.isNotEmpty && !passwordValid;
    isFormValid.value = emailValid && passwordValid;
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
      return Error('Please fix validation errors');
    }

    final result = await _authRepository.login(
      email: email,
      password: password,
    );
    if (result is Error<void>) {
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


