import "package:flutter/material.dart";
import 'package:google_fonts/google_fonts.dart';
import 'colors.dart';

class TaskItTheme {
  static ThemeData light() => _buildTheme(lightScheme());
  static ThemeData dark() => _buildTheme(darkScheme());

  static ThemeData _buildTheme(ColorScheme colorScheme) {
    final textTheme = _buildTextTheme(colorScheme);

    return ThemeData(
      useMaterial3: true,
      brightness: colorScheme.brightness,
      colorScheme: colorScheme,
      textTheme: textTheme,
      scaffoldBackgroundColor: colorScheme.surface,
      canvasColor: colorScheme.surface,
      // Enable predictive back animations for Android 13+
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: PredictiveBackPageTransitionsBuilder(),
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
        },
      ),
    );
  }

  static TextTheme _buildTextTheme(ColorScheme colorScheme) {
    final baseTextTheme = GoogleFonts.interTextTheme();
    final displayTextTheme = GoogleFonts.manropeTextTheme();

    return displayTextTheme.copyWith(
      bodyLarge: baseTextTheme.bodyLarge?.copyWith(color: colorScheme.onSurface),
      bodyMedium: baseTextTheme.bodyMedium?.copyWith(color: colorScheme.onSurface),
      bodySmall: baseTextTheme.bodySmall?.copyWith(color: colorScheme.onSurface),
      labelLarge: baseTextTheme.labelLarge?.copyWith(color: colorScheme.onSurface),
      labelMedium: baseTextTheme.labelMedium?.copyWith(color: colorScheme.onSurface),
      labelSmall: baseTextTheme.labelSmall?.copyWith(color: colorScheme.onSurface),
      displayLarge: displayTextTheme.displayLarge?.copyWith(color: colorScheme.onSurface),
      displayMedium: displayTextTheme.displayMedium?.copyWith(color: colorScheme.onSurface),
      displaySmall: displayTextTheme.displaySmall?.copyWith(color: colorScheme.onSurface),
      headlineLarge: displayTextTheme.headlineLarge?.copyWith(color: colorScheme.onSurface),
      headlineMedium: displayTextTheme.headlineMedium?.copyWith(color: colorScheme.onSurface),
      headlineSmall: displayTextTheme.headlineSmall?.copyWith(color: colorScheme.onSurface),
      titleLarge: displayTextTheme.titleLarge?.copyWith(color: colorScheme.onSurface),
      titleMedium: displayTextTheme.titleMedium?.copyWith(color: colorScheme.onSurface),
      titleSmall: displayTextTheme.titleSmall?.copyWith(color: colorScheme.onSurface),
    );
  }
}
