import 'package:flutter/material.dart';

abstract final class AppColors {
  static const primaryLight = Color(0xFF575992);
  static const onPrimaryLight = Color(0xFFFFFFFF);
  static const primaryContainerLight = Color(0xFFE1E0FF);
  static const onPrimaryContainerLight = Color(0xFF3F4178);
  static const secondaryLight = Color(0xFF575992);
  static const onSecondaryLight = Color(0xFFFFFFFF);
  static const secondaryContainerLight = Color(0xFFE1E0FF);
  static const onSecondaryContainerLight = Color(0xFF3F4178);
  static const tertiaryLight = Color(0xFF69548D);
  static const onTertiaryLight = Color(0xFFFFFFFF);
  static const tertiaryContainerLight = Color(0xFFECDCFF);
  static const onTertiaryContainerLight = Color(0xFF513C73);
  static const errorLight = Color(0xFF904A43);
  static const onErrorLight = Color(0xFFFFFFFF);
  static const errorContainerLight = Color(0xFFFFDAD6);
  static const onErrorContainerLight = Color(0xFF73332D);
  static const backgroundLight = Color(0xFFFCF8FF);
  static const onBackgroundLight = Color(0xFF1B1B21);
  static const surfaceLight = Color(0xFFFCF8FF);
  static const onSurfaceLight = Color(0xFF1B1B21);
  static const surfaceVariantLight = Color(0xFFE4E1EC);
  static const onSurfaceVariantLight = Color(0xFF46464F);
  static const outlineLight = Color(0xFF777680);
  static const outlineVariantLight = Color(0xFFC8C5D0);
  static const scrimLight = Color(0xFF000000);
  static const inverseSurfaceLight = Color(0xFF303036);
  static const inverseOnSurfaceLight = Color(0xFFF3EFF7);
  static const inversePrimaryLight = Color(0xFFC0C1FF);
  static const surfaceDimLight = Color(0xFFDCD9E0);
  static const surfaceBrightLight = Color(0xFFFCF8FF);
  static const surfaceContainerLowestLight = Color(0xFFFFFFFF);
  static const surfaceContainerLowLight = Color(0xFFF6F2FA);
  static const surfaceContainerLight = Color(0xFFF0ECF4);
  static const surfaceContainerHighLight = Color(0xFFEAE7EF);
  static const surfaceContainerHighestLight = Color(0xFFE4E1E9);

  static const primaryDark = Color(0xFFC0C1FF);
  static const onPrimaryDark = Color(0xFF292A60);
  static const primaryContainerDark = Color(0xFF3F4178);
  static const onPrimaryContainerDark = Color(0xFFE1E0FF);
  static const secondaryDark = Color(0xFFC0C1FF);
  static const onSecondaryDark = Color(0xFF292A60);
  static const secondaryContainerDark = Color(0xFF3F4178);
  static const onSecondaryContainerDark = Color(0xFFE1E0FF);
  static const tertiaryDark = Color(0xFFD4BBFC);
  static const onTertiaryDark = Color(0xFF3A255B);
  static const tertiaryContainerDark = Color(0xFF513C73);
  static const onTertiaryContainerDark = Color(0xFFECDCFF);
  static const errorDark = Color(0xFFFFB4AB);
  static const onErrorDark = Color(0xFF561E19);
  static const errorContainerDark = Color(0xFF73332D);
  static const onErrorContainerDark = Color(0xFFFFDAD6);
  static const backgroundDark = Color(0xFF131318);
  static const onBackgroundDark = Color(0xFFE4E1E9);
  static const surfaceDark = Color(0xFF131318);
  static const onSurfaceDark = Color(0xFFE4E1E9);
  static const surfaceVariantDark = Color(0xFF46464F);
  static const onSurfaceVariantDark = Color(0xFFC8C5D0);
  static const outlineDark = Color(0xFF918F9A);
  static const outlineVariantDark = Color(0xFF46464F);
  static const scrimDark = Color(0xFF000000);
  static const inverseSurfaceDark = Color(0xFFE4E1E9);
  static const inverseOnSurfaceDark = Color(0xFF303036);
  static const inversePrimaryDark = Color(0xFF575992);
  static const surfaceDimDark = Color(0xFF131318);
  static const surfaceBrightDark = Color(0xFF39383F);
  static const surfaceContainerLowestDark = Color(0xFF0E0E13);
  static const surfaceContainerLowDark = Color(0xFF1B1B21);
  static const surfaceContainerDark = Color(0xFF1F1F25);
  static const surfaceContainerHighDark = Color(0xFF2A292F);
  static const surfaceContainerHighestDark = Color(0xFF35343A);
}

ColorScheme lightScheme() {
  return const ColorScheme(
    brightness: Brightness.light,
    primary: AppColors.primaryLight,
    surfaceTint: AppColors.primaryLight,
    onPrimary: AppColors.onPrimaryLight,
    primaryContainer: AppColors.primaryContainerLight,
    onPrimaryContainer: AppColors.onPrimaryContainerLight,
    secondary: AppColors.secondaryLight,
    onSecondary: AppColors.onSecondaryLight,
    secondaryContainer: AppColors.secondaryContainerLight,
    onSecondaryContainer: AppColors.onSecondaryContainerLight,
    tertiary: AppColors.tertiaryLight,
    onTertiary: AppColors.onTertiaryLight,
    tertiaryContainer: AppColors.tertiaryContainerLight,
    onTertiaryContainer: AppColors.onTertiaryContainerLight,
    error: AppColors.errorLight,
    onError: AppColors.onErrorLight,
    errorContainer: AppColors.errorContainerLight,
    onErrorContainer: AppColors.onErrorContainerLight,
    surface: AppColors.surfaceLight,
    onSurface: AppColors.onSurfaceLight,
    onSurfaceVariant: AppColors.onSurfaceVariantLight,
    outline: AppColors.outlineLight,
    outlineVariant: AppColors.outlineVariantLight,
    shadow: AppColors.scrimLight,
    scrim: AppColors.scrimLight,
    inverseSurface: AppColors.inverseSurfaceLight,
    inversePrimary: AppColors.inversePrimaryLight,
    primaryFixed: AppColors.primaryContainerLight,
    onPrimaryFixed: AppColors.primaryContainerLight,
    primaryFixedDim: AppColors.inversePrimaryLight,
    onPrimaryFixedVariant: AppColors.onPrimaryContainerLight,
    secondaryFixed: AppColors.secondaryContainerLight,
    onSecondaryFixed: AppColors.onSecondaryContainerLight,
    secondaryFixedDim: AppColors.secondaryContainerLight,
    onSecondaryFixedVariant: AppColors.onSecondaryContainerLight,
    tertiaryFixed: AppColors.tertiaryContainerLight,
    onTertiaryFixed: AppColors.onTertiaryContainerLight,
    tertiaryFixedDim: AppColors.onTertiaryLight,
    onTertiaryFixedVariant: AppColors.onTertiaryContainerLight,
    surfaceDim: AppColors.surfaceDimLight,
    surfaceBright: AppColors.surfaceBrightLight,
    surfaceContainerLowest: AppColors.surfaceContainerLowestLight,
    surfaceContainerLow: AppColors.surfaceContainerLowLight,
    surfaceContainer: AppColors.surfaceContainerLight,
    surfaceContainerHigh: AppColors.surfaceContainerHighLight,
    surfaceContainerHighest: AppColors.surfaceContainerHighestLight,
  );
}

ColorScheme darkScheme() {
  return const ColorScheme(
    brightness: Brightness.dark,
    primary: AppColors.primaryDark,
    surfaceTint: AppColors.primaryDark,
    onPrimary: AppColors.onPrimaryDark,
    primaryContainer: AppColors.primaryContainerDark,
    onPrimaryContainer: AppColors.onPrimaryContainerDark,
    secondary: AppColors.secondaryDark,
    onSecondary: AppColors.onSecondaryDark,
    secondaryContainer: AppColors.secondaryContainerDark,
    onSecondaryContainer: AppColors.onSecondaryContainerDark,
    tertiary: AppColors.tertiaryDark,
    onTertiary: AppColors.onTertiaryDark,
    tertiaryContainer: AppColors.tertiaryContainerDark,
    onTertiaryContainer: AppColors.onTertiaryContainerDark,
    error: AppColors.errorDark,
    onError: AppColors.onErrorDark,
    errorContainer: AppColors.errorContainerDark,
    onErrorContainer: AppColors.onErrorContainerDark,
    surface: AppColors.surfaceDark,
    onSurface: AppColors.onSurfaceDark,
    onSurfaceVariant: AppColors.onSurfaceVariantDark,
    outline: AppColors.outlineDark,
    outlineVariant: AppColors.outlineVariantDark,
    shadow: AppColors.scrimDark,
    scrim: AppColors.scrimDark,
    inverseSurface: AppColors.inverseSurfaceDark,
    inversePrimary: AppColors.inversePrimaryDark,
    primaryFixed: AppColors.primaryContainerDark,
    onPrimaryFixed: AppColors.onPrimaryContainerDark,
    primaryFixedDim: AppColors.inversePrimaryDark,
    onPrimaryFixedVariant: AppColors.onPrimaryContainerDark,
    secondaryFixed: AppColors.secondaryContainerDark,
    onSecondaryFixed: AppColors.onSecondaryContainerDark,
    secondaryFixedDim: AppColors.secondaryContainerDark,
    onSecondaryFixedVariant: AppColors.onSecondaryContainerDark,
    tertiaryFixed: AppColors.tertiaryContainerDark,
    onTertiaryFixed: AppColors.onTertiaryContainerDark,
    tertiaryFixedDim: AppColors.onTertiaryDark,
    onTertiaryFixedVariant: AppColors.onTertiaryContainerDark,
    surfaceDim: AppColors.surfaceDimDark,
    surfaceBright: AppColors.surfaceBrightDark,
    surfaceContainerLowest: AppColors.surfaceContainerLowestDark,
    surfaceContainerLow: AppColors.surfaceContainerLowDark,
    surfaceContainer: AppColors.surfaceContainerDark,
    surfaceContainerHigh: AppColors.surfaceContainerHighDark,
    surfaceContainerHighest: AppColors.surfaceContainerHighestDark,
  );
}
