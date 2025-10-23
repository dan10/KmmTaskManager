// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_l10n.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appName => 'TaskIt';

  @override
  String get authAppName => 'Task Manager';

  @override
  String get authLoginTitle => 'Welcome Back';

  @override
  String get authRegisterTitle => 'Create Account';

  @override
  String get authEmail => 'Email';

  @override
  String get authEmailHint => 'Enter your email';

  @override
  String get authPassword => 'Password';

  @override
  String get authPasswordHint => 'Enter your password';

  @override
  String get authConfirmPassword => 'Confirm Password';

  @override
  String get authName => 'Name';

  @override
  String get authNameHint => 'Enter your full name';

  @override
  String get authLoginButton => 'Login';

  @override
  String get authRegisterButton => 'Create Account';

  @override
  String get authSignUp => 'Sign Up';

  @override
  String get authSignIn => 'Sign In';

  @override
  String get authWithoutAccount => 'Don\'t have an account?';

  @override
  String get authAlreadyHaveAccount => 'Already have an account?';

  @override
  String get authEmailError => 'Please enter a valid email address';

  @override
  String get authPasswordError => 'Password must be at least 8 characters';

  @override
  String authPasswordTooShort(int minLength) {
    return 'Password must be at least $minLength characters';
  }

  @override
  String get authNameError => 'Name should not be empty';

  @override
  String get authNameTooShort => 'Name must be at least 2 characters';

  @override
  String get authConfirmPasswordError => 'Passwords do not match';

  @override
  String authLoginError(String error) {
    return 'Login failed: $error';
  }

  @override
  String authRegisterError(String error) {
    return 'Registration failed: $error';
  }

  @override
  String get authForgotPassword => 'Forgot Password?';

  @override
  String get authResetPassword => 'Reset Password';

  @override
  String get authLogout => 'Logout';

  @override
  String get authLoggingIn => 'Logging in...';

  @override
  String get authCreatingAccount => 'Creating account...';
}
