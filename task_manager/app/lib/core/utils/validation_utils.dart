import 'package:flutter_gen/gen_l10n/app_localizations.dart';

/// Comprehensive validation utility class for form validation
class ValidationUtils {
  // Email validation regex
  static final RegExp _emailRegex = RegExp(
    r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$',
  );

  // Password strength regex patterns
  static final RegExp _hasUppercase = RegExp(r'[A-Z]');
  static final RegExp _hasLowercase = RegExp(r'[a-z]');
  static final RegExp _hasDigits = RegExp(r'[0-9]');
  static final RegExp _hasSpecialCharacters = RegExp(r'[!@#$%^&*(),.?":{}|<>]');

  /// Validates email format
  static String? validateEmail(String? email, AppLocalizations l10n) {
    if (email == null || email
        .trim()
        .isEmpty) {
      return l10n.validationEmailRequired;
    }

    if (!_emailRegex.hasMatch(email.trim())) {
      return l10n.validationEmailInvalid;
    }

    return null;
  }

  /// Validates password with comprehensive rules
  static String? validatePassword(String? password, AppLocalizations l10n) {
    if (password == null || password.isEmpty) {
      return l10n.validationPasswordRequired;
    }

    if (password.length < 8) {
      return l10n.validationPasswordTooShort;
    }

    if (password.length > 128) {
      return l10n.validationPasswordTooLong;
    }

    if (!_hasLowercase.hasMatch(password)) {
      return l10n.validationPasswordNeedsLowercase;
    }

    if (!_hasUppercase.hasMatch(password)) {
      return l10n.validationPasswordNeedsUppercase;
    }

    if (!_hasDigits.hasMatch(password)) {
      return l10n.validationPasswordNeedsNumber;
    }

    if (!_hasSpecialCharacters.hasMatch(password)) {
      return l10n.validationPasswordNeedsSpecialChar;
    }

    return null;
  }

  /// Validates password confirmation
  static String? validatePasswordConfirmation(String? password,
      String? confirmPassword,
      AppLocalizations l10n,) {
    if (confirmPassword == null || confirmPassword.isEmpty) {
      return l10n.validationConfirmPasswordRequired;
    }

    if (password != confirmPassword) {
      return l10n.validationPasswordsDoNotMatch;
    }

    return null;
  }

  /// Validates name/display name
  static String? validateName(String? name, AppLocalizations l10n) {
    if (name == null || name
        .trim()
        .isEmpty) {
      return l10n.validationNameRequired;
    }

    if (name
        .trim()
        .length < 2) {
      return l10n.validationNameTooShort;
    }

    if (name
        .trim()
        .length > 50) {
      return l10n.validationNameTooLong;
    }

    // Check for valid characters (letters, spaces, hyphens, apostrophes)
    final nameRegex = RegExp(r"^[a-zA-ZÀ-ÿ\s\-']+$");
    if (!nameRegex.hasMatch(name.trim())) {
      return l10n.validationNameInvalidCharacters;
    }

    return null;
  }

  /// Validates required field
  static String? validateRequired(String? value, String fieldName,
      AppLocalizations l10n) {
    if (value == null || value
        .trim()
        .isEmpty) {
      return l10n.validationFieldRequired(fieldName);
    }
    return null;
  }

  /// Gets password strength level (0-4)
  static int getPasswordStrength(String password) {
    int strength = 0;

    if (password.length >= 8) strength++;
    if (_hasLowercase.hasMatch(password)) strength++;
    if (_hasUppercase.hasMatch(password)) strength++;
    if (_hasDigits.hasMatch(password)) strength++;
    if (_hasSpecialCharacters.hasMatch(password)) strength++;

    return strength;
  }

  /// Gets password strength description
  static String getPasswordStrengthText(int strength, AppLocalizations l10n) {
    switch (strength) {
      case 0:
      case 1:
        return l10n.passwordStrengthVeryWeak;
      case 2:
        return l10n.passwordStrengthWeak;
      case 3:
        return l10n.passwordStrengthMedium;
      case 4:
        return l10n.passwordStrengthStrong;
      case 5:
        return l10n.passwordStrengthVeryStrong;
      default:
        return l10n.passwordStrengthVeryWeak;
    }
  }

  /// Gets password strength color
  static int getPasswordStrengthColor(int strength) {
    switch (strength) {
      case 0:
      case 1:
        return 0xFFE53E3E; // Red
      case 2:
        return 0xFFDD6B20; // Orange
      case 3:
        return 0xFFD69E2E; // Yellow
      case 4:
        return 0xFF38A169; // Green
      case 5:
        return 0xFF2F855A; // Dark Green
      default:
        return 0xFFE53E3E; // Red
    }
  }

  /// Validates login form
  static Map<String, String?> validateLoginForm({
    required String email,
    required String password,
    required AppLocalizations l10n,
  }) {
    return {
      'email': validateEmail(email, l10n),
      'password': password.isEmpty ? l10n.validationPasswordRequired : null,
    };
  }

  /// Validates register form
  static Map<String, String?> validateRegisterForm({
    required String name,
    required String email,
    required String password,
    required String confirmPassword,
    required AppLocalizations l10n,
  }) {
    return {
      'name': validateName(name, l10n),
      'email': validateEmail(email, l10n),
      'password': validatePassword(password, l10n),
      'confirmPassword': validatePasswordConfirmation(
          password, confirmPassword, l10n),
    };
  }

  /// Checks if form has any errors
  static bool hasErrors(Map<String, String?> validationResults) {
    return validationResults.values.any((error) => error != null);
  }
} 