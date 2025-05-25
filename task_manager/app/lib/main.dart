import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:provider/provider.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import 'core/di/providers.dart';
import 'navigation/app_router.dart';
import 'presentation/providers/auth_provider.dart';
import 'presentation/providers/task_provider.dart';
import 'presentation/providers/project_provider.dart';
import 'presentation/viewmodels/task_viewmodel.dart';
import 'presentation/viewmodels/project_viewmodel.dart';

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
    return MultiProvider(
      providers: [
        // Dependency injection providers
        ...DependencyProviders.providers,

        // Legacy providers (keeping for backward compatibility)
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => TaskProvider()),
        ChangeNotifierProvider(create: (_) => ProjectProvider()),
        ChangeNotifierProvider(create: (_) => TaskViewModel()),
        ChangeNotifierProvider(create: (_) => ProjectViewModel()),
      ],
      child: MaterialApp.router(
        title: 'TaskIt',
        theme: ThemeData(
          useMaterial3: false,
          // Material 2 design to match Compose
          primaryColor: const Color(0xFF575992),
          // primaryLight from Compose
          scaffoldBackgroundColor: const Color(0XFFF1F5F9),
          // backgroundLight from Compose

          // Add proper ColorScheme for consistent primary color usage
          colorScheme: const ColorScheme.light(
            primary: Color(0xFF575992),
            // primaryLight from Compose
            onPrimary: Color(0xFFFFFFFF),
            // onPrimaryLight from Compose
            primaryContainer: Color(0xFFE1E0FF),
            // primaryContainerLight from Compose
            onPrimaryContainer: Color(0xFF13144B),
            // onPrimaryContainerLight from Compose
            secondary: Color(0xFF5F5E72),
            // secondaryLight from Compose
            onSecondary: Color(0xFFFFFFFF),
            // onSecondaryLight from Compose
            secondaryContainer: Color(0xFFE4E1F9),
            // secondaryContainerLight from Compose
            onSecondaryContainer: Color(0xFF1B1B2F),
            // onSecondaryContainerLight from Compose
            tertiary: Color(0xFF7A5570),
            // tertiaryLight from Compose
            onTertiary: Color(0xFFFFFFFF),
            // onTertiaryLight from Compose
            tertiaryContainer: Color(0xFFFFD7F0),
            // tertiaryContainerLight from Compose
            onTertiaryContainer: Color(0xFF31152A),
            // onTertiaryContainerLight from Compose
            error: Color(0xFFBA1A1A),
            // errorLight from Compose
            onError: Color(0xFFFFFFFF),
            // onErrorLight from Compose
            errorContainer: Color(0xFFFFDAD6),
            // errorContainerLight from Compose
            onErrorContainer: Color(0xFF410002),
            // onErrorContainerLight from Compose
            surface: Color(0xFFFCF8FF),
            // surfaceLight from Compose
            onSurface: Color(0xFF1B1B21),
            // onSurfaceLight from Compose
            surfaceVariant: Color(0xFFE4E1EC),
            // surfaceVariantLight from Compose
            onSurfaceVariant: Color(0xFF46464F),
            // onSurfaceVariantLight from Compose
            outline: Color(0xFF777680),
            // outlineLight from Compose
            outlineVariant: Color(0xFFC8C5D0),
            // outlineVariantLight from Compose
            shadow: Color(0xFF000000),
            // shadowLight from Compose
            scrim: Color(0xFF000000),
            // scrimLight from Compose
            inverseSurface: Color(0xFF2F2F37),
            // inverseSurfaceLight from Compose
            onInverseSurface: Color(0xFFF1F0F7),
            // inverseOnSurfaceLight from Compose
            inversePrimary: Color(0xFFC0C1FF),
            // inversePrimaryLight from Compose
            surfaceTint: Color(0xFF575992), // surfaceTintLight from Compose
          ),

          // Color scheme matching Compose colors
          cardColor: const Color(0xFFFCF8FF),
          // surfaceLight

          // Input Decoration Theme for consistent OutlinedTextField styling
          inputDecorationTheme: InputDecorationTheme(
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFF777680), // outlineLight from Compose
                width: 1.5,
              ),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFF777680), // outlineLight from Compose
                width: 1.5,
              ),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFF575992), // primaryLight from Compose
                width: 2.0,
              ),
            ),
            errorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFFBA1A1A), // errorLight from Compose
                width: 1.5,
              ),
            ),
            focusedErrorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFFBA1A1A), // errorLight from Compose
                width: 2.0,
              ),
            ),
            disabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFFC8C5D0), // outlineVariantLight from Compose
                width: 1.5,
              ),
            ),
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 16,
            ),
            labelStyle: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w400,
              color: Color(0xFF46464F), // onSurfaceVariantLight from Compose
            ),
            hintStyle: const TextStyle(
              fontSize: 16,
              color: Color(0xFF777680), // outlineLight from Compose
            ),
          ),

          // Card Theme for consistent rounded corners
          cardTheme: CardTheme(
            elevation: 4,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            clipBehavior: Clip.antiAlias,
            color: const Color(0xFFFCF8FF), // surfaceLight from Compose
          ),

          // Elevated Button Theme
          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              foregroundColor: const Color(0xFFFFFFFF),
              // onPrimaryLight from Compose
              backgroundColor: const Color(0xFF575992),
              // primaryLight from Compose
              padding: const EdgeInsets.symmetric(
                horizontal: 24,
                vertical: 16,
              ),
              minimumSize: const Size(88, 52),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              elevation: 2,
              textStyle: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),

          // Text Button Theme
          textButtonTheme: TextButtonThemeData(
            style: TextButton.styleFrom(
              foregroundColor: const Color(0xFF575992),
              // primaryLight from Compose
              padding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 12,
              ),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              textStyle: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),

          // Bottom Navigation Bar Theme
          bottomNavigationBarTheme: const BottomNavigationBarThemeData(
            type: BottomNavigationBarType.fixed,
            selectedItemColor: Color(0xFF575992),
            // primaryLight from Compose
            unselectedItemColor: Color(0xFF46464F),
            // onSurfaceVariantLight from Compose
            backgroundColor: Color(0xFFFCF8FF),
            // surfaceLight from Compose
            selectedLabelStyle: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
            ),
            unselectedLabelStyle: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
            ),
            elevation: 8,
          ),

          // App Bar Theme
          appBarTheme: const AppBarTheme(
            elevation: 0,
            centerTitle: true,
            backgroundColor: Color(0xFFFCF8FF),
            // surfaceLight from Compose
            foregroundColor: Color(0xFF1B1B21),
            // onBackgroundLight from Compose
            titleTextStyle: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w600,
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
            ),
            iconTheme: IconThemeData(
                color: Color(0xFF1B1B21)), // onBackgroundLight from Compose
          ),

          // Chip Theme
          chipTheme: ChipThemeData(
            backgroundColor: const Color(0xFFE4E1EC),
            // surfaceVariantLight from Compose
            deleteIconColor: const Color(0xFF46464F),
            // onSurfaceVariantLight from Compose
            disabledColor: const Color(0xFFC8C5D0),
            // outlineVariantLight from Compose
            selectedColor: const Color(0xFFE1E0FF),
            // primaryContainerLight from Compose
            secondarySelectedColor: const Color(0xFFE1E0FF),
            // primaryContainerLight from Compose
            labelPadding: const EdgeInsets.symmetric(horizontal: 8),
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20),
            ),
            labelStyle: const TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
            ),
            secondaryLabelStyle: const TextStyle(
              color: Color(0xFF13144B), // onPrimaryContainerLight from Compose
            ),
            brightness: Brightness.light,
          ),

          // Floating Action Button Theme
          floatingActionButtonTheme: const FloatingActionButtonThemeData(
            backgroundColor: Color(0xFF575992), // primaryLight from Compose
            foregroundColor: Color(0xFFFFFFFF), // onPrimaryLight from Compose
            elevation: 6,
          ),

          // Icon Theme
          iconTheme: const IconThemeData(
            color: Color(0xFF46464F), // onSurfaceVariantLight from Compose
          ),

          // Text Theme
          textTheme: const TextTheme(
            headlineLarge: TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
              fontWeight: FontWeight.bold,
            ),
            headlineMedium: TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
              fontWeight: FontWeight.bold,
            ),
            headlineSmall: TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
              fontWeight: FontWeight.w600,
            ),
            titleLarge: TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
              fontWeight: FontWeight.w600,
            ),
            bodyLarge: TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
            ),
            bodyMedium: TextStyle(
              color: Color(0xFF1B1B21), // onBackgroundLight from Compose
            ),
          ),
        ),

        // Dark theme matching Compose dark colors
        darkTheme: ThemeData(
          useMaterial3: false,
          // Material 2 design to match Compose
          brightness: Brightness.dark,
          primaryColor: const Color(0xFFC0C1FF),
          // primaryDark from Compose
          scaffoldBackgroundColor: const Color(0xFF131318),
          // backgroundDark from Compose

          // Add proper ColorScheme for dark theme
          colorScheme: const ColorScheme.dark(
            primary: Color(0xFFC0C1FF),
            // primaryDark from Compose
            onPrimary: Color(0xFF292A60),
            // onPrimaryDark from Compose
            primaryContainer: Color(0xFF404178),
            // primaryContainerDark from Compose
            onPrimaryContainer: Color(0xFFE1E0FF),
            // onPrimaryContainerDark from Compose
            secondary: Color(0xFFC8C5D3),
            // secondaryDark from Compose
            onSecondary: Color(0xFF313045),
            // onSecondaryDark from Compose
            secondaryContainer: Color(0xFF48475C),
            // secondaryContainerDark from Compose
            onSecondaryContainer: Color(0xFFE4E1F9),
            // onSecondaryContainerDark from Compose
            tertiary: Color(0xFFE6B7D4),
            // tertiaryDark from Compose
            onTertiary: Color(0xFF482940),
            // onTertiaryDark from Compose
            tertiaryContainer: Color(0xFF603F57),
            // tertiaryContainerDark from Compose
            onTertiaryContainer: Color(0xFFFFD7F0),
            // onTertiaryContainerDark from Compose
            error: Color(0xFFFFB4AB),
            // errorDark from Compose
            onError: Color(0xFF690005),
            // onErrorDark from Compose
            errorContainer: Color(0xFF93000A),
            // errorContainerDark from Compose
            onErrorContainer: Color(0xFFFFDAD6),
            // onErrorContainerDark from Compose
            surface: Color(0xFF131318),
            // surfaceDark from Compose
            onSurface: Color(0xFFE4E1E9),
            // onSurfaceDark from Compose
            surfaceVariant: Color(0xFF46464F),
            // surfaceVariantDark from Compose
            onSurfaceVariant: Color(0xFFC8C5D0),
            // onSurfaceVariantDark from Compose
            outline: Color(0xFF918F9A),
            // outlineDark from Compose
            outlineVariant: Color(0xFF46464F),
            // outlineVariantDark from Compose
            shadow: Color(0xFF000000),
            // shadowDark from Compose
            scrim: Color(0xFF000000),
            // scrimDark from Compose
            inverseSurface: Color(0xFFE4E1E9),
            // inverseSurfaceDark from Compose
            onInverseSurface: Color(0xFF2F2F37),
            // inverseOnSurfaceDark from Compose
            inversePrimary: Color(0xFF575992),
            // inversePrimaryDark from Compose
            surfaceTint: Color(0xFFC0C1FF), // surfaceTintDark from Compose
          ),

          // Input Decoration Theme for dark mode
          inputDecorationTheme: InputDecorationTheme(
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFF918F9A), // outlineDark from Compose
                width: 1.5,
              ),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFF918F9A), // outlineDark from Compose
                width: 1.5,
              ),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFFC0C1FF), // primaryDark from Compose
                width: 2.0,
              ),
            ),
            errorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFFFFB4AB), // errorDark from Compose
                width: 1.5,
              ),
            ),
            focusedErrorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFFFFB4AB), // errorDark from Compose
                width: 2.0,
              ),
            ),
            disabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(
                color: Color(0xFF46464F), // outlineVariantDark from Compose
                width: 1.5,
              ),
            ),
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 16,
            ),
            labelStyle: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w400,
              color: Color(0xFFC8C5D0), // onSurfaceVariantDark from Compose
            ),
            hintStyle: const TextStyle(
              fontSize: 16,
              color: Color(0xFF918F9A), // outlineDark from Compose
            ),
          ),

          // Consistent themes for dark mode
          cardTheme: CardTheme(
            elevation: 4,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            clipBehavior: Clip.antiAlias,
            color: const Color(0xFF131318), // surfaceDark from Compose
          ),

          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              foregroundColor: const Color(0xFF292A60),
              // onPrimaryDark from Compose
              backgroundColor: const Color(0xFFC0C1FF),
              // primaryDark from Compose
              padding: const EdgeInsets.symmetric(
                horizontal: 24,
                vertical: 16,
              ),
              minimumSize: const Size(88, 52),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              elevation: 2,
              textStyle: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),

          bottomNavigationBarTheme: const BottomNavigationBarThemeData(
            type: BottomNavigationBarType.fixed,
            selectedItemColor: Color(0xFFC0C1FF),
            // primaryDark from Compose
            unselectedItemColor: Color(0xFFC8C5D0),
            // onSurfaceVariantDark from Compose
            backgroundColor: Color(0xFF131318),
            // surfaceDark from Compose
            elevation: 8,
          ),

          appBarTheme: const AppBarTheme(
            elevation: 0,
            centerTitle: true,
            backgroundColor: Color(0xFF131318),
            // surfaceDark from Compose
            foregroundColor: Color(0xFFE4E1E9),
            // onBackgroundDark from Compose
            titleTextStyle: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w600,
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
            ),
            iconTheme: IconThemeData(
                color: Color(0xFFE4E1E9)), // onBackgroundDark from Compose
          ),

          // Floating Action Button Theme for dark mode
          floatingActionButtonTheme: const FloatingActionButtonThemeData(
            backgroundColor: Color(0xFFC0C1FF), // primaryDark from Compose
            foregroundColor: Color(0xFF292A60), // onPrimaryDark from Compose
            elevation: 6,
          ),

          textTheme: const TextTheme(
            headlineLarge: TextStyle(
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
              fontWeight: FontWeight.bold,
            ),
            headlineMedium: TextStyle(
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
              fontWeight: FontWeight.bold,
            ),
            headlineSmall: TextStyle(
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
              fontWeight: FontWeight.w600,
            ),
            titleLarge: TextStyle(
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
              fontWeight: FontWeight.w600,
            ),
            bodyLarge: TextStyle(
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
            ),
            bodyMedium: TextStyle(
              color: Color(0xFFE4E1E9), // onBackgroundDark from Compose
            ),
          ),
        ),

        // Localization setup
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
      ),
    );
  }
} 