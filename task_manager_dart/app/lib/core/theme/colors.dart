import 'package:flutter/material.dart';

/// Extended color system for TaskIt app.
/// Provides semantic color tokens beyond Material Design 3.
@immutable
class AppExtendedColors extends ThemeExtension<AppExtendedColors> {
  // Task item colors
  final Color taskContainerDefault;
  final Color taskContainerDone;
  final Color taskContainerOverdue;
  final Color taskIndicatorOverdue;
  final Color taskTitleDefault;
  final Color taskTitleDone;
  final Color taskDescriptionDefault;
  final Color taskDescriptionDone;

  // Status badge colors
  final Color statusTodoText;
  final Color statusTodoContainer;
  final Color statusInProgressText;
  final Color statusInProgressContainer;
  final Color statusDoneText;
  final Color statusDoneContainer;

  // Chip colors
  final Color chipDueDateText;
  final Color chipDueDateContainer;
  final Color chipDueDateOverdueText;
  final Color chipDueDateOverdueContainer;
  final Color chipProjectText;
  final Color chipProjectContainer;

  // Checkbox colors
  final Color checkboxUnchecked;
  final Color checkboxCheckmark;

  // Priority colors
  final Color priorityNoneText;
  final Color priorityNoneContainer;
  final Color priorityHighText;
  final Color priorityHighContainer;
  final Color priorityMediumText;
  final Color priorityMediumContainer;
  final Color priorityLowText;
  final Color priorityLowContainer;

  // Neutral text colors
  final Color textPrimary;
  final Color textSecondary;
  final Color iconNeutral;
  final Color iconPurple;

  // Surface colors
  final Color surfaceCard;
  final Color trackNeutral;

  // Accent colors
  final Color accentSuccessText;
  final Color accentSuccessContainer;
  final Color accentWarningText;
  final Color accentWarningContainer;
  final Color accentDangerText;
  final Color accentDangerContainer;

  // Swipe action colors
  final Color swipeCompleteBackground;
  final Color swipeDeleteBackground;
  final Color swipeDeleteForeground;

  const AppExtendedColors({
    required this.taskContainerDefault,
    required this.taskContainerDone,
    required this.taskContainerOverdue,
    required this.taskIndicatorOverdue,
    required this.taskTitleDefault,
    required this.taskTitleDone,
    required this.taskDescriptionDefault,
    required this.taskDescriptionDone,
    required this.statusTodoText,
    required this.statusTodoContainer,
    required this.statusInProgressText,
    required this.statusInProgressContainer,
    required this.statusDoneText,
    required this.statusDoneContainer,
    required this.chipDueDateText,
    required this.chipDueDateContainer,
    required this.chipDueDateOverdueText,
    required this.chipDueDateOverdueContainer,
    required this.chipProjectText,
    required this.chipProjectContainer,
    required this.checkboxUnchecked,
    required this.checkboxCheckmark,
    required this.priorityNoneText,
    required this.priorityNoneContainer,
    required this.priorityHighText,
    required this.priorityHighContainer,
    required this.priorityMediumText,
    required this.priorityMediumContainer,
    required this.priorityLowText,
    required this.priorityLowContainer,
    required this.textPrimary,
    required this.textSecondary,
    required this.iconNeutral,
    required this.iconPurple,
    required this.surfaceCard,
    required this.trackNeutral,
    required this.accentSuccessText,
    required this.accentSuccessContainer,
    required this.accentWarningText,
    required this.accentWarningContainer,
    required this.accentDangerText,
    required this.accentDangerContainer,
    required this.swipeCompleteBackground,
    required this.swipeDeleteBackground,
    required this.swipeDeleteForeground,
  });

  @override
  AppExtendedColors copyWith({
    Color? taskContainerDefault,
    Color? taskContainerDone,
    Color? taskContainerOverdue,
    Color? taskIndicatorOverdue,
    Color? taskTitleDefault,
    Color? taskTitleDone,
    Color? taskDescriptionDefault,
    Color? taskDescriptionDone,
    Color? statusTodoText,
    Color? statusTodoContainer,
    Color? statusInProgressText,
    Color? statusInProgressContainer,
    Color? statusDoneText,
    Color? statusDoneContainer,
    Color? chipDueDateText,
    Color? chipDueDateContainer,
    Color? chipDueDateOverdueText,
    Color? chipDueDateOverdueContainer,
    Color? chipProjectText,
    Color? chipProjectContainer,
    Color? checkboxUnchecked,
    Color? checkboxCheckmark,
    Color? priorityNoneText,
    Color? priorityNoneContainer,
    Color? priorityHighText,
    Color? priorityHighContainer,
    Color? priorityMediumText,
    Color? priorityMediumContainer,
    Color? priorityLowText,
    Color? priorityLowContainer,
    Color? textPrimary,
    Color? textSecondary,
    Color? iconNeutral,
    Color? iconPurple,
    Color? surfaceCard,
    Color? trackNeutral,
    Color? accentSuccessText,
    Color? accentSuccessContainer,
    Color? accentWarningText,
    Color? accentWarningContainer,
    Color? accentDangerText,
    Color? accentDangerContainer,
    Color? swipeCompleteBackground,
    Color? swipeDeleteBackground,
    Color? swipeDeleteForeground,
  }) {
    return AppExtendedColors(
      taskContainerDefault: taskContainerDefault ?? this.taskContainerDefault,
      taskContainerDone: taskContainerDone ?? this.taskContainerDone,
      taskContainerOverdue: taskContainerOverdue ?? this.taskContainerOverdue,
      taskIndicatorOverdue: taskIndicatorOverdue ?? this.taskIndicatorOverdue,
      taskTitleDefault: taskTitleDefault ?? this.taskTitleDefault,
      taskTitleDone: taskTitleDone ?? this.taskTitleDone,
      taskDescriptionDefault: taskDescriptionDefault ?? this.taskDescriptionDefault,
      taskDescriptionDone: taskDescriptionDone ?? this.taskDescriptionDone,
      statusTodoText: statusTodoText ?? this.statusTodoText,
      statusTodoContainer: statusTodoContainer ?? this.statusTodoContainer,
      statusInProgressText: statusInProgressText ?? this.statusInProgressText,
      statusInProgressContainer: statusInProgressContainer ?? this.statusInProgressContainer,
      statusDoneText: statusDoneText ?? this.statusDoneText,
      statusDoneContainer: statusDoneContainer ?? this.statusDoneContainer,
      chipDueDateText: chipDueDateText ?? this.chipDueDateText,
      chipDueDateContainer: chipDueDateContainer ?? this.chipDueDateContainer,
      chipDueDateOverdueText: chipDueDateOverdueText ?? this.chipDueDateOverdueText,
      chipDueDateOverdueContainer: chipDueDateOverdueContainer ?? this.chipDueDateOverdueContainer,
      chipProjectText: chipProjectText ?? this.chipProjectText,
      chipProjectContainer: chipProjectContainer ?? this.chipProjectContainer,
      checkboxUnchecked: checkboxUnchecked ?? this.checkboxUnchecked,
      checkboxCheckmark: checkboxCheckmark ?? this.checkboxCheckmark,
      priorityNoneText: priorityNoneText ?? this.priorityNoneText,
      priorityNoneContainer: priorityNoneContainer ?? this.priorityNoneContainer,
      priorityHighText: priorityHighText ?? this.priorityHighText,
      priorityHighContainer: priorityHighContainer ?? this.priorityHighContainer,
      priorityMediumText: priorityMediumText ?? this.priorityMediumText,
      priorityMediumContainer: priorityMediumContainer ?? this.priorityMediumContainer,
      priorityLowText: priorityLowText ?? this.priorityLowText,
      priorityLowContainer: priorityLowContainer ?? this.priorityLowContainer,
      textPrimary: textPrimary ?? this.textPrimary,
      textSecondary: textSecondary ?? this.textSecondary,
      iconNeutral: iconNeutral ?? this.iconNeutral,
      iconPurple: iconPurple ?? this.iconPurple,
      surfaceCard: surfaceCard ?? this.surfaceCard,
      trackNeutral: trackNeutral ?? this.trackNeutral,
      accentSuccessText: accentSuccessText ?? this.accentSuccessText,
      accentSuccessContainer: accentSuccessContainer ?? this.accentSuccessContainer,
      accentWarningText: accentWarningText ?? this.accentWarningText,
      accentWarningContainer: accentWarningContainer ?? this.accentWarningContainer,
      accentDangerText: accentDangerText ?? this.accentDangerText,
      accentDangerContainer: accentDangerContainer ?? this.accentDangerContainer,
      swipeCompleteBackground: swipeCompleteBackground ?? this.swipeCompleteBackground,
      swipeDeleteBackground: swipeDeleteBackground ?? this.swipeDeleteBackground,
      swipeDeleteForeground: swipeDeleteForeground ?? this.swipeDeleteForeground,
    );
  }

  @override
  AppExtendedColors lerp(covariant ThemeExtension<AppExtendedColors>? other, double t) {
    if (other is! AppExtendedColors) {
      return this;
    }
    return AppExtendedColors(
      taskContainerDefault: Color.lerp(taskContainerDefault, other.taskContainerDefault, t)!,
      taskContainerDone: Color.lerp(taskContainerDone, other.taskContainerDone, t)!,
      taskContainerOverdue: Color.lerp(taskContainerOverdue, other.taskContainerOverdue, t)!,
      taskIndicatorOverdue: Color.lerp(taskIndicatorOverdue, other.taskIndicatorOverdue, t)!,
      taskTitleDefault: Color.lerp(taskTitleDefault, other.taskTitleDefault, t)!,
      taskTitleDone: Color.lerp(taskTitleDone, other.taskTitleDone, t)!,
      taskDescriptionDefault: Color.lerp(taskDescriptionDefault, other.taskDescriptionDefault, t)!,
      taskDescriptionDone: Color.lerp(taskDescriptionDone, other.taskDescriptionDone, t)!,
      statusTodoText: Color.lerp(statusTodoText, other.statusTodoText, t)!,
      statusTodoContainer: Color.lerp(statusTodoContainer, other.statusTodoContainer, t)!,
      statusInProgressText: Color.lerp(statusInProgressText, other.statusInProgressText, t)!,
      statusInProgressContainer: Color.lerp(statusInProgressContainer, other.statusInProgressContainer, t)!,
      statusDoneText: Color.lerp(statusDoneText, other.statusDoneText, t)!,
      statusDoneContainer: Color.lerp(statusDoneContainer, other.statusDoneContainer, t)!,
      chipDueDateText: Color.lerp(chipDueDateText, other.chipDueDateText, t)!,
      chipDueDateContainer: Color.lerp(chipDueDateContainer, other.chipDueDateContainer, t)!,
      chipDueDateOverdueText: Color.lerp(chipDueDateOverdueText, other.chipDueDateOverdueText, t)!,
      chipDueDateOverdueContainer: Color.lerp(chipDueDateOverdueContainer, other.chipDueDateOverdueContainer, t)!,
      chipProjectText: Color.lerp(chipProjectText, other.chipProjectText, t)!,
      chipProjectContainer: Color.lerp(chipProjectContainer, other.chipProjectContainer, t)!,
      checkboxUnchecked: Color.lerp(checkboxUnchecked, other.checkboxUnchecked, t)!,
      checkboxCheckmark: Color.lerp(checkboxCheckmark, other.checkboxCheckmark, t)!,
      priorityNoneText: Color.lerp(priorityNoneText, other.priorityNoneText, t)!,
      priorityNoneContainer: Color.lerp(priorityNoneContainer, other.priorityNoneContainer, t)!,
      priorityHighText: Color.lerp(priorityHighText, other.priorityHighText, t)!,
      priorityHighContainer: Color.lerp(priorityHighContainer, other.priorityHighContainer, t)!,
      priorityMediumText: Color.lerp(priorityMediumText, other.priorityMediumText, t)!,
      priorityMediumContainer: Color.lerp(priorityMediumContainer, other.priorityMediumContainer, t)!,
      priorityLowText: Color.lerp(priorityLowText, other.priorityLowText, t)!,
      priorityLowContainer: Color.lerp(priorityLowContainer, other.priorityLowContainer, t)!,
      textPrimary: Color.lerp(textPrimary, other.textPrimary, t)!,
      textSecondary: Color.lerp(textSecondary, other.textSecondary, t)!,
      iconNeutral: Color.lerp(iconNeutral, other.iconNeutral, t)!,
      iconPurple: Color.lerp(iconPurple, other.iconPurple, t)!,
      surfaceCard: Color.lerp(surfaceCard, other.surfaceCard, t)!,
      trackNeutral: Color.lerp(trackNeutral, other.trackNeutral, t)!,
      accentSuccessText: Color.lerp(accentSuccessText, other.accentSuccessText, t)!,
      accentSuccessContainer: Color.lerp(accentSuccessContainer, other.accentSuccessContainer, t)!,
      accentWarningText: Color.lerp(accentWarningText, other.accentWarningText, t)!,
      accentWarningContainer: Color.lerp(accentWarningContainer, other.accentWarningContainer, t)!,
      accentDangerText: Color.lerp(accentDangerText, other.accentDangerText, t)!,
      accentDangerContainer: Color.lerp(accentDangerContainer, other.accentDangerContainer, t)!,
      swipeCompleteBackground: Color.lerp(swipeCompleteBackground, other.swipeCompleteBackground, t)!,
      swipeDeleteBackground: Color.lerp(swipeDeleteBackground, other.swipeDeleteBackground, t)!,
      swipeDeleteForeground: Color.lerp(swipeDeleteForeground, other.swipeDeleteForeground, t)!,
    );
  }
}

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

/// Light theme extended colors
const AppExtendedColors lightExtendedColors = AppExtendedColors(
  // Task item colors
  taskContainerDefault: Color(0xFFFFFFFF),
  taskContainerDone: Color(0xFFF9FAFB),
  taskContainerOverdue: Color(0xFFFFF5F5),
  taskIndicatorOverdue: Color(0xFFEF4444),
  taskTitleDefault: Color(0xFF1A1A1A),
  taskTitleDone: Color(0xFF9CA3AF),
  taskDescriptionDefault: Color(0xFF6B7280),
  taskDescriptionDone: Color(0xFFD1D5DB),

  // Status badge colors
  statusTodoText: Color(0xFF6B7280),
  statusTodoContainer: Color(0xFFF3F4F6),
  statusInProgressText: Color(0xFF3B82F6),
  statusInProgressContainer: Color(0xFFDCEEFE),
  statusDoneText: Color(0xFF10B981),
  statusDoneContainer: Color(0xFFD1FAE5),

  // Chip colors
  chipDueDateText: Color(0xFF4B5563),
  chipDueDateContainer: Color(0xFFF3F4F6),
  chipDueDateOverdueText: Color(0xFFDC2626),
  chipDueDateOverdueContainer: Color(0xFFFEE2E2),
  chipProjectText: Color(0xFF7C3AED),
  chipProjectContainer: Color(0xFFEDE9FE),

  // Checkbox colors
  checkboxUnchecked: Color(0xFFD1D5DB),
  checkboxCheckmark: Color(0xFFFFFFFF),

  // Priority colors
  priorityNoneText: Color(0xFF6B7280),
  priorityNoneContainer: Color(0xFFF3F4F6),
  priorityHighText: Color(0xFFDC2626),
  priorityHighContainer: Color(0xFFFFE4E4),
  priorityMediumText: Color(0xFFEAB308),
  priorityMediumContainer: Color(0xFFFEF9C3),
  priorityLowText: Color(0xFF22C55E),
  priorityLowContainer: Color(0xFFDCFCE7),

  // Neutral text colors
  textPrimary: Color(0xFF1A1A1A),
  textSecondary: Color(0xFF6B7280),
  iconNeutral: Color(0xFF9CA3AF),
  iconPurple: Color(0xFF7C3AED),

  // Surface colors
  surfaceCard: Color(0xFFFFFFFF),
  trackNeutral: Color(0xFFE5E7EB),

  // Accent colors
  accentSuccessText: Color(0xFF2E7D32),
  accentSuccessContainer: Color(0xFFE8F5E9),
  accentWarningText: Color(0xFFEAB308),
  accentWarningContainer: Color(0xFFFEF9C3),
  accentDangerText: Color(0xFFD32F2F),
  accentDangerContainer: Color(0xFFFFEBEE),

  // Swipe action colors
  swipeCompleteBackground: Color(0xFF2E7D32),
  swipeDeleteBackground: Color(0xFFFFEBEE),
  swipeDeleteForeground: Color(0xFFD32F2F),
);

/// Dark theme extended colors
const AppExtendedColors darkExtendedColors = AppExtendedColors(
  // Task item colors
  taskContainerDefault: Color(0xFF1F1F25),
  taskContainerDone: Color(0xFF1B1B21),
  taskContainerOverdue: Color(0xFF2A1F1F),
  taskIndicatorOverdue: Color(0xFFD65C5C),
  taskTitleDefault: Color(0xFFE4E1E9),
  taskTitleDone: Color(0xFF8B8990),
  taskDescriptionDefault: Color(0xFFA5A3AB),
  taskDescriptionDone: Color(0xFF6B6870),

  // Status badge colors
  statusTodoText: Color(0xFF9CA3AF),
  statusTodoContainer: Color(0xFF2A292F),
  statusInProgressText: Color(0xFF6BA3F5),
  statusInProgressContainer: Color(0xFF1F2A3F),
  statusDoneText: Color(0xFF4FD1A7),
  statusDoneContainer: Color(0xFF1F2F2A),

  // Chip colors
  chipDueDateText: Color(0xFFA5A3AB),
  chipDueDateContainer: Color(0xFF2A292F),
  chipDueDateOverdueText: Color(0xFFE65C5C),
  chipDueDateOverdueContainer: Color(0xFF3F2A2A),
  chipProjectText: Color(0xFF9D7AFF),
  chipProjectContainer: Color(0xFF2F2540),

  // Checkbox colors
  checkboxUnchecked: Color(0xFF6B6870),
  checkboxCheckmark: Color(0xFFFFFFFF),

  // Priority colors
  priorityNoneText: Color(0xFF9CA3AF),
  priorityNoneContainer: Color(0xFF2A292F),
  priorityHighText: Color(0xFFE65C5C),
  priorityHighContainer: Color(0xFF3F2A2A),
  priorityMediumText: Color(0xFFD4B020),
  priorityMediumContainer: Color(0xFF3F3A1F),
  priorityLowText: Color(0xFF4FD1A7),
  priorityLowContainer: Color(0xFF1F2F2A),

  // Neutral text colors
  textPrimary: Color(0xFFE4E1E9),
  textSecondary: Color(0xFFA5A3AB),
  iconNeutral: Color(0xFF8B8990),
  iconPurple: Color(0xFF9D7AFF),

  // Surface colors
  surfaceCard: Color(0xFF1F1F25),
  trackNeutral: Color(0xFF35343A),

  // Accent colors
  accentSuccessText: Color(0xFF4FD1A7),
  accentSuccessContainer: Color(0xFF1F2F2A),
  accentWarningText: Color(0xFFD4B020),
  accentWarningContainer: Color(0xFF3F3A1F),
  accentDangerText: Color(0xFFE65C5C),
  accentDangerContainer: Color(0xFF3F2A2A),

  // Swipe action colors
  swipeCompleteBackground: Color(0xFF1F3F25),
  swipeDeleteBackground: Color(0xFF3F2A2A),
  swipeDeleteForeground: Color(0xFFE65C5C),
);
