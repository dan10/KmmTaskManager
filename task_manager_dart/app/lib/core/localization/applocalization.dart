import 'package:flutter/widgets.dart';

import '../l10n/app_l10n.dart';

class AppLocalization {
  AppLocalization(this._strings);
  final AppLocalizations _strings;

  static AppLocalization of(BuildContext context) => AppLocalization(AppLocalizations.of(context)!);

  // App name
  String get appName => _strings.appName;
  
  // Auth
  String get login => _strings.authLoginButton;
  String get tryAgain => _strings.authSignIn;
  String get errorWhileLogin => _strings.authLoginError('');
  String get email => _strings.authEmail;
  String get password => _strings.authPassword;
  String get emailError => _strings.authEmailError;
  String get passwordError => _strings.authPasswordError;
  String get withoutAccount => _strings.authWithoutAccount;
  String get signUp => _strings.authSignUp;
  
  // Register
  String get createAccount => _strings.authRegisterTitle;
  String get name => _strings.authName;
  String get confirmPassword => _strings.authConfirmPassword;
  String get nameError => _strings.authNameError;
  String get confirmPasswordError => _strings.authConfirmPasswordError;
  String get alreadyHaveAccount => _strings.authAlreadyHaveAccount;
  String get signIn => _strings.authSignIn;
  String get register => _strings.authRegisterButton;
}


