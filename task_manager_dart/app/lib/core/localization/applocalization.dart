import 'package:flutter/widgets.dart';

import '../l10n/app_l10n.dart';

class AppLocalization {
  AppLocalization(this._strings);
  final AppLocalizations _strings;

  static AppLocalization of(BuildContext context) => AppLocalization(AppLocalizations.of(context)!);

  String get login => _strings.authLoginButton;
  String get tryAgain => _strings.authSignIn;
  String get errorWhileLogin => _strings.authLoginError('');
}


