import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_app/core/theme/theme.dart';
import 'package:task_manager_app/l10n/app_localizations.dart';

import 'core/di/providers.dart';
import 'navigation/app_router.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // TODO: Initialize services when dependencies are available
  // await StorageService.instance.init();
  // await ApiService.instance.init();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    final brightness = View.of(context).platformDispatcher.platformBrightness;

    return MultiProvider(
      providers: DependencyProviders.providers,
      child: MaterialApp.router(
        title: 'TaskIt',
        theme: brightness == Brightness.light ? TaskItTheme.light() : TaskItTheme.dark(),
        localizationsDelegates: const [
          AppLocalizations.delegate,
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        supportedLocales: const [
          Locale('en'), // English
          Locale('es'), // Spanish
          Locale('pt', 'BR'), // Portuguese Brazil
        ],
        routerConfig: AppRouter.router,
    ));
  }
} 