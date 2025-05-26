import 'package:flutter/material.dart';

class AppTheme {
  // Colors matching the Compose app theme
  static const Color primaryLight = Color(0xFF575992);
  static const Color onPrimaryLight = Color(0xFFFFFFFF);
  static const Color primaryContainerLight = Color(0xFFE1E0FF);
  static const Color onPrimaryContainerLight = Color(0xFF13144B);
  static const Color secondaryLight = Color(0xFF5D5C72);
  static const Color onSecondaryLight = Color(0xFFFFFFFF);
  static const Color secondaryContainerLight = Color(0xFFE2E0F9);
  static const Color onSecondaryContainerLight = Color(0xFF191A2C);
  static const Color tertiaryLight = Color(0xFF795369);
  static const Color onTertiaryLight = Color(0xFFFFFFFF);
  static const Color tertiaryContainerLight = Color(0xFFFFD8EC);
  static const Color onTertiaryContainerLight = Color(0xFF2E1125);
  static const Color errorLight = Color(0xFFBA1A1A);
  static const Color onErrorLight = Color(0xFFFFFFFF);
  static const Color errorContainerLight = Color(0xFFFFDAD6);
  static const Color onErrorContainerLight = Color(0xFF410002);
  static const Color backgroundLight = Color(0xFFFCF8FF);
  static const Color onBackgroundLight = Color(0xFF1B1B21);
  static const Color surfaceLight = Color(0xFFFCF8FF);
  static const Color onSurfaceLight = Color(0xFF1B1B21);
  static const Color surfaceVariantLight = Color(0xFFE4E1EC);
  static const Color onSurfaceVariantLight = Color(0xFF46464F);
  static const Color outlineLight = Color(0xFF777680);

  // Dark theme colors
  static const Color primaryDark = Color(0xFFC0C1FF);
  static const Color onPrimaryDark = Color(0xFF292A60);
  static const Color primaryContainerDark = Color(0xFF3F4178);
  static const Color onPrimaryContainerDark = Color(0xFFE1E0FF);
  static const Color secondaryDark = Color(0xFFC6C4DD);
  static const Color onSecondaryDark = Color(0xFF2E2F42);
  static const Color secondaryContainerDark = Color(0xFF454559);
  static const Color onSecondaryContainerDark = Color(0xFFE2E0F9);
  static const Color tertiaryDark = Color(0xFFE9B9D3);
  static const Color onTertiaryDark = Color(0xFF46263A);
  static const Color tertiaryContainerDark = Color(0xFF5F3C51);
  static const Color onTertiaryContainerDark = Color(0xFFFFD8EC);
  static const Color errorDark = Color(0xFFFFB4AB);
  static const Color onErrorDark = Color(0xFF690005);
  static const Color errorContainerDark = Color(0xFF93000A);
  static const Color onErrorContainerDark = Color(0xFFFFDAD6);
  static const Color backgroundDark = Color(0xFF131318);
  static const Color onBackgroundDark = Color(0xFFE4E1E9);
  static const Color surfaceDark = Color(0xFF131318);
  static const Color onSurfaceDark = Color(0xFFE4E1E9);
  static const Color surfaceVariantDark = Color(0xFF46464F);
  static const Color onSurfaceVariantDark = Color(0xFFC8C5D0);
  static const Color outlineDark = Color(0xFF918F9A);

  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: false, // Using Material 2 to match Compose app
      primarySwatch: _createMaterialColor(primaryLight),
      primaryColor: primaryLight,
      colorScheme: const ColorScheme.light(
        primary: primaryLight,
        onPrimary: onPrimaryLight,
        primaryContainer: primaryContainerLight,
        onPrimaryContainer: onPrimaryContainerLight,
        secondary: secondaryLight,
        onSecondary: onSecondaryLight,
        secondaryContainer: secondaryContainerLight,
        onSecondaryContainer: onSecondaryContainerLight,
        tertiary: tertiaryLight,
        onTertiary: onTertiaryLight,
        tertiaryContainer: tertiaryContainerLight,
        onTertiaryContainer: onTertiaryContainerLight,
        error: errorLight,
        onError: onErrorLight,
        errorContainer: errorContainerLight,
        onErrorContainer: onErrorContainerLight,
        background: backgroundLight,
        onBackground: onBackgroundLight,
        surface: surfaceLight,
        onSurface: onSurfaceLight,
        surfaceVariant: surfaceVariantLight,
        onSurfaceVariant: onSurfaceVariantLight,
        outline: outlineLight,
      ),
      appBarTheme: const AppBarTheme(
        centerTitle: true,
        elevation: 4,
        backgroundColor: primaryLight,
        foregroundColor: onPrimaryLight,
        iconTheme: IconThemeData(color: onPrimaryLight),
      ),
      cardTheme: const CardTheme(
        elevation: 2,
        margin: EdgeInsets.all(8),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(16)), // Medium = 16dp in Compose
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primaryLight,
          foregroundColor: onPrimaryLight,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8), // Small = 8dp in Compose
          ),
          elevation: 2,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: primaryLight, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: primaryLight,
        foregroundColor: onPrimaryLight,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: surfaceVariantLight,
        labelStyle: const TextStyle(color: onSurfaceVariantLight),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }

  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: false, // Using Material 2 to match Compose app
      brightness: Brightness.dark,
      primarySwatch: _createMaterialColor(primaryDark),
      primaryColor: primaryDark,
      colorScheme: const ColorScheme.dark(
        primary: primaryDark,
        onPrimary: onPrimaryDark,
        primaryContainer: primaryContainerDark,
        onPrimaryContainer: onPrimaryContainerDark,
        secondary: secondaryDark,
        onSecondary: onSecondaryDark,
        secondaryContainer: secondaryContainerDark,
        onSecondaryContainer: onSecondaryContainerDark,
        tertiary: tertiaryDark,
        onTertiary: onTertiaryDark,
        tertiaryContainer: tertiaryContainerDark,
        onTertiaryContainer: onTertiaryContainerDark,
        error: errorDark,
        onError: onErrorDark,
        errorContainer: errorContainerDark,
        onErrorContainer: onErrorContainerDark,
        background: backgroundDark,
        onBackground: onBackgroundDark,
        surface: surfaceDark,
        onSurface: onSurfaceDark,
        surfaceVariant: surfaceVariantDark,
        onSurfaceVariant: onSurfaceVariantDark,
        outline: outlineDark,
      ),
      appBarTheme: const AppBarTheme(
        centerTitle: true,
        elevation: 4,
        backgroundColor: primaryContainerDark,
        foregroundColor: onPrimaryContainerDark,
        iconTheme: IconThemeData(color: onPrimaryContainerDark),
      ),
      cardTheme: const CardTheme(
        elevation: 2,
        margin: EdgeInsets.all(8),
        color: surfaceDark,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(16)), // Medium = 16dp in Compose
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primaryDark,
          foregroundColor: onPrimaryDark,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8), // Small = 8dp in Compose
          ),
          elevation: 2,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: primaryDark, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: primaryDark,
        foregroundColor: onPrimaryDark,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: surfaceVariantDark,
        labelStyle: const TextStyle(color: onSurfaceVariantDark),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }

  // Helper method to create MaterialColor from Color
  static MaterialColor _createMaterialColor(Color color) {
    List strengths = <double>[.05];
    Map<int, Color> swatch = {};
    final int r = color.red, g = color.green, b = color.blue;

    for (int i = 1; i < 10; i++) {
      strengths.add(0.1 * i);
    }
    for (var strength in strengths) {
      final double ds = 0.5 - strength;
      swatch[(strength * 1000).round()] = Color.fromRGBO(
        r + ((ds < 0 ? r : (255 - r)) * ds).round(),
        g + ((ds < 0 ? g : (255 - g)) * ds).round(),
        b + ((ds < 0 ? b : (255 - b)) * ds).round(),
        1,
      );
    }
    return MaterialColor(color.value, swatch);
  }
} 