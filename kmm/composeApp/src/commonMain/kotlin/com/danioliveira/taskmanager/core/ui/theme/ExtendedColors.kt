package com.danioliveira.taskmanager.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color system for TaskIt app.
 * Provides semantic color tokens beyond Material Design 3.
 */
@Immutable
data class TaskItExtendedColors(
    // Task item colors
    val taskContainerDefault: Color,
    val taskContainerDone: Color,
    val taskContainerOverdue: Color,
    val taskIndicatorOverdue: Color,
    val taskTitleDefault: Color,
    val taskTitleDone: Color,
    val taskDescriptionDefault: Color,
    val taskDescriptionDone: Color,

    // Status badge colors
    val statusTodoText: Color,
    val statusTodoContainer: Color,
    val statusInProgressText: Color,
    val statusInProgressContainer: Color,
    val statusDoneText: Color,
    val statusDoneContainer: Color,

    // Chip colors
    val chipDueDateText: Color,
    val chipDueDateContainer: Color,
    val chipDueDateOverdueText: Color,
    val chipDueDateOverdueContainer: Color,
    val chipProjectText: Color,
    val chipProjectContainer: Color,

    // Checkbox colors
    val checkboxUnchecked: Color,
    val checkboxCheckmark: Color,

    // Priority colors
    val priorityNoneText: Color,
    val priorityNoneContainer: Color,
    val priorityHighText: Color,
    val priorityHighContainer: Color,
    val priorityMediumText: Color,
    val priorityMediumContainer: Color,
    val priorityLowText: Color,
    val priorityLowContainer: Color,

    // Neutral text colors
    val textPrimary: Color,
    val textSecondary: Color,
    val iconNeutral: Color,
    val iconPurple: Color,

    // Surface colors
    val surfaceCard: Color,
    val trackNeutral: Color,

    // Accent colors
    val accentSuccessText: Color,
    val accentSuccessContainer: Color,
    val accentWarningText: Color,
    val accentWarningContainer: Color,
    val accentDangerText: Color,
    val accentDangerContainer: Color,

    // Swipe action colors
    val swipeCompleteBackground: Color,
    val swipeDeleteBackground: Color,
    val swipeDeleteForeground: Color
)

/**
 * CompositionLocal for extended colors.
 */
val LocalTaskItExtendedColors = staticCompositionLocalOf {
    defaultExtendedColors()
}

/**
 * Default extended colors (light theme as fallback).
 */
private fun defaultExtendedColors(): TaskItExtendedColors {
    return extendedLightColors()
}

/**
 * Creates extended colors for light theme.
 */
fun extendedLightColors(): TaskItExtendedColors {
    return TaskItExtendedColors(
        // Task item colors
        taskContainerDefault = taskContainerDefaultLight,
        taskContainerDone = taskContainerDoneLight,
        taskContainerOverdue = taskContainerOverdueLight,
        taskIndicatorOverdue = taskIndicatorOverdueLight,
        taskTitleDefault = taskTitleDefaultLight,
        taskTitleDone = taskTitleDoneLight,
        taskDescriptionDefault = taskDescriptionDefaultLight,
        taskDescriptionDone = taskDescriptionDoneLight,

        // Status badge colors
        statusTodoText = statusTodoTextLight,
        statusTodoContainer = statusTodoContainerLight,
        statusInProgressText = statusInProgressTextLight,
        statusInProgressContainer = statusInProgressContainerLight,
        statusDoneText = statusDoneTextLight,
        statusDoneContainer = statusDoneContainerLight,

        // Chip colors
        chipDueDateText = chipDueDateTextLight,
        chipDueDateContainer = chipDueDateContainerLight,
        chipDueDateOverdueText = chipDueDateOverdueTextLight,
        chipDueDateOverdueContainer = chipDueDateOverdueContainerLight,
        chipProjectText = chipProjectTextLight,
        chipProjectContainer = chipProjectContainerLight,

        // Checkbox colors
        checkboxUnchecked = checkboxUncheckedLight,
        checkboxCheckmark = checkboxCheckmarkLight,

        // Priority colors
        priorityNoneText = priorityNoneTextLight,
        priorityNoneContainer = priorityNoneContainerLight,
        priorityHighText = priorityHighTextLight,
        priorityHighContainer = priorityHighContainerLight,
        priorityMediumText = priorityMediumTextLight,
        priorityMediumContainer = priorityMediumContainerLight,
        priorityLowText = priorityLowTextLight,
        priorityLowContainer = priorityLowContainerLight,

        // Neutral text colors
        textPrimary = textPrimaryLight,
        textSecondary = textSecondaryLight,
        iconNeutral = iconNeutralLight,
        iconPurple = iconPurpleLight,

        // Surface colors
        surfaceCard = surfaceCardLight,
        trackNeutral = trackNeutralLight,

        // Accent colors
        accentSuccessText = accentSuccessTextLight,
        accentSuccessContainer = accentSuccessContainerLight,
        accentWarningText = accentWarningTextLight,
        accentWarningContainer = accentWarningContainerLight,
        accentDangerText = accentDangerTextLight,
        accentDangerContainer = accentDangerContainerLight,

        // Swipe action colors
        swipeCompleteBackground = swipeCompleteBackgroundLight,
        swipeDeleteBackground = swipeDeleteBackgroundLight,
        swipeDeleteForeground = swipeDeleteForegroundLight
    )
}

/**
 * Creates extended colors for dark theme.
 */
fun extendedDarkColors(): TaskItExtendedColors {
    return TaskItExtendedColors(
        // Task item colors
        taskContainerDefault = taskContainerDefaultDark,
        taskContainerDone = taskContainerDoneDark,
        taskContainerOverdue = taskContainerOverdueDark,
        taskIndicatorOverdue = taskIndicatorOverdueDark,
        taskTitleDefault = taskTitleDefaultDark,
        taskTitleDone = taskTitleDoneDark,
        taskDescriptionDefault = taskDescriptionDefaultDark,
        taskDescriptionDone = taskDescriptionDoneDark,

        // Status badge colors
        statusTodoText = statusTodoTextDark,
        statusTodoContainer = statusTodoContainerDark,
        statusInProgressText = statusInProgressTextDark,
        statusInProgressContainer = statusInProgressContainerDark,
        statusDoneText = statusDoneTextDark,
        statusDoneContainer = statusDoneContainerDark,

        // Chip colors
        chipDueDateText = chipDueDateTextDark,
        chipDueDateContainer = chipDueDateContainerDark,
        chipDueDateOverdueText = chipDueDateOverdueTextDark,
        chipDueDateOverdueContainer = chipDueDateOverdueContainerDark,
        chipProjectText = chipProjectTextDark,
        chipProjectContainer = chipProjectContainerDark,

        // Checkbox colors
        checkboxUnchecked = checkboxUncheckedDark,
        checkboxCheckmark = checkboxCheckmarkDark,

        // Priority colors
        priorityNoneText = priorityNoneTextDark,
        priorityNoneContainer = priorityNoneContainerDark,
        priorityHighText = priorityHighTextDark,
        priorityHighContainer = priorityHighContainerDark,
        priorityMediumText = priorityMediumTextDark,
        priorityMediumContainer = priorityMediumContainerDark,
        priorityLowText = priorityLowTextDark,
        priorityLowContainer = priorityLowContainerDark,

        // Neutral text colors
        textPrimary = textPrimaryDark,
        textSecondary = textSecondaryDark,
        iconNeutral = iconNeutralDark,
        iconPurple = iconPurpleDark,

        // Surface colors
        surfaceCard = surfaceCardDark,
        trackNeutral = trackNeutralDark,

        // Accent colors
        accentSuccessText = accentSuccessTextDark,
        accentSuccessContainer = accentSuccessContainerDark,
        accentWarningText = accentWarningTextDark,
        accentWarningContainer = accentWarningContainerDark,
        accentDangerText = accentDangerTextDark,
        accentDangerContainer = accentDangerContainerDark,

        // Swipe action colors
        swipeCompleteBackground = swipeCompleteBackgroundDark,
        swipeDeleteBackground = swipeDeleteBackgroundDark,
        swipeDeleteForeground = swipeDeleteForegroundDark
    )
}

/**
 * Accessor object for extended colors in theme.
 */
object TaskItThemeExt {
    val colors: TaskItExtendedColors
        @Composable
        get() = LocalTaskItExtendedColors.current
}

