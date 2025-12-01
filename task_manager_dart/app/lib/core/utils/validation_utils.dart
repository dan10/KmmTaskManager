class ValidationUtils {
  static final RegExp _emailRegex = RegExp(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,} $');

  static String? validateEmail(String? value) {
    if (value == null || value.isEmpty) return 'Email required';
    if (!_emailRegex.hasMatch(value)) return 'Invalid email';
    return null;
  }

  static String? validatePassword(String? password) {
    if (password == null || password.isEmpty) return 'Password required';
    if (password.length < 8) return 'Password too short';
    return null;
  }

  static String? validatePasswordConfirmation(String? password, String? confirmPassword) {
    if (confirmPassword == null || confirmPassword.isEmpty) return 'Confirm your password';
    if (password != confirmPassword) return 'Passwords do not match';
    return null;
  }

  static String? validateName(String? name) {
    if (name == null || name.trim().isEmpty) return 'Name required';
    if (name.trim().length < 2) return 'Name too short';
    return null;
  }

  static String? validateRequired(String? value, String fieldName) {
    if (value == null || value.trim().isEmpty) return '$fieldName required';
    return null;
  }
} 