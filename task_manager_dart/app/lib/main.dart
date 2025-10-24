import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_app/core/theme/theme.dart';

import 'core/di/providers.dart';
import 'core/l10n/app_l10n.dart';
import 'core/routing/app_router.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // TODO: Initialize services when dependencies are available
  // await StorageService.instance.init();
  // await ApiService.instance.init();

  debugRepaintRainbowEnabled = true;
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {

    return MultiProvider(
      providers: DependencyProviders.providers,
      child: MaterialApp.router(
        localizationsDelegates: const [
          AppLocalizations.delegate,
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        themeMode: ThemeMode.system,
        theme:TaskItTheme.light(),
        darkTheme: TaskItTheme.dark(),
        supportedLocales: AppLocalizations.supportedLocales,
        routerConfig: appRouter,
    ));
  }
} 