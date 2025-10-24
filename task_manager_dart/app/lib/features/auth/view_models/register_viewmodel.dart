import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

import '../data/repositories/auth_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

class RegisterViewModel {
  RegisterViewModel({required AuthRepository authRepository})
      : _authRepository = authRepository {
    register = Command1<void, (String name, String email, String password, String confirmPassword)>(_register);
  }

  final AuthRepository _authRepository;
  final _log = Logger('RegisterViewModel');

  late Command1 register;

  // Validation state
  final ValueNotifier<bool> nameHasError = ValueNotifier<bool>(false);
  final ValueNotifier<bool> emailHasError = ValueNotifier<bool>(false);
  final ValueNotifier<bool> passwordHasError = ValueNotifier<bool>(false);
  final ValueNotifier<bool> confirmPasswordHasError = ValueNotifier<bool>(false);
  final ValueNotifier<bool> isFormValid = ValueNotifier<bool>(false);

  // Name validation
  bool _isNameValid(String name) {
    return name.trim().length >= 2;
  }

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

  // Confirm password validation
  bool _isConfirmPasswordValid(String password, String confirmPassword) {
    return confirmPassword.isNotEmpty && password == confirmPassword;
  }

  // Validate form
  void validateForm(String name, String email, String password, String confirmPassword) {
    final nameValid = _isNameValid(name);
    final emailValid = _isEmailValid(email);
    final passwordValid = _isPasswordValid(password);
    final confirmPasswordValid = _isConfirmPasswordValid(password, confirmPassword);

    nameHasError.value = name.isNotEmpty && !nameValid;
    emailHasError.value = email.isNotEmpty && !emailValid;
    passwordHasError.value = password.isNotEmpty && !passwordValid;
    confirmPasswordHasError.value = confirmPassword.isNotEmpty && !confirmPasswordValid;
    isFormValid.value = nameValid && emailValid && passwordValid && confirmPasswordValid;
  }

  // Clear validation errors
  void clearValidation() {
    nameHasError.value = false;
    emailHasError.value = false;
    passwordHasError.value = false;
    confirmPasswordHasError.value = false;
    isFormValid.value = false;
  }

  Future<Result<void>> _register((String, String, String, String) data) async {
    final (name, email, password, confirmPassword) = data;
    
    // Validate before attempting registration
    validateForm(name, email, password, confirmPassword);
    
    if (!isFormValid.value) {
      return Error('Please fix validation errors');
    }

    final result = await _authRepository.register(
      name: name,
      email: email,
      password: password,
    );
    if (result is Error<void>) {
      _log.warning('Register failed! ${result.error}');
    }
    return result;
  }

  void dispose() {
    nameHasError.dispose();
    emailHasError.dispose();
    passwordHasError.dispose();
    confirmPasswordHasError.dispose();
    isFormValid.dispose();
  }
}


