package com.danioliveira.taskmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.danioliveira.taskmanager.core.ui.theme.backgroundDark
import com.danioliveira.taskmanager.core.ui.theme.backgroundLight
import com.danioliveira.taskmanager.core.ui.theme.errorContainerDark
import com.danioliveira.taskmanager.core.ui.theme.errorContainerLight
import com.danioliveira.taskmanager.core.ui.theme.errorDark
import com.danioliveira.taskmanager.core.ui.theme.errorLight
import com.danioliveira.taskmanager.core.ui.theme.inverseOnSurfaceDark
import com.danioliveira.taskmanager.core.ui.theme.inverseOnSurfaceLight
import com.danioliveira.taskmanager.core.ui.theme.inversePrimaryDark
import com.danioliveira.taskmanager.core.ui.theme.inversePrimaryLight
import com.danioliveira.taskmanager.core.ui.theme.inverseSurfaceDark
import com.danioliveira.taskmanager.core.ui.theme.inverseSurfaceLight
import com.danioliveira.taskmanager.core.ui.theme.onBackgroundDark
import com.danioliveira.taskmanager.core.ui.theme.onBackgroundLight
import com.danioliveira.taskmanager.core.ui.theme.onErrorContainerDark
import com.danioliveira.taskmanager.core.ui.theme.onErrorContainerLight
import com.danioliveira.taskmanager.core.ui.theme.onErrorDark
import com.danioliveira.taskmanager.core.ui.theme.onErrorLight
import com.danioliveira.taskmanager.core.ui.theme.onPrimaryContainerDark
import com.danioliveira.taskmanager.core.ui.theme.onPrimaryContainerLight
import com.danioliveira.taskmanager.core.ui.theme.onPrimaryDark
import com.danioliveira.taskmanager.core.ui.theme.onPrimaryLight
import com.danioliveira.taskmanager.core.ui.theme.onSecondaryContainerDark
import com.danioliveira.taskmanager.core.ui.theme.onSecondaryContainerLight
import com.danioliveira.taskmanager.core.ui.theme.onSecondaryDark
import com.danioliveira.taskmanager.core.ui.theme.onSecondaryLight
import com.danioliveira.taskmanager.core.ui.theme.onSurfaceDark
import com.danioliveira.taskmanager.core.ui.theme.onSurfaceLight
import com.danioliveira.taskmanager.core.ui.theme.onSurfaceVariantDark
import com.danioliveira.taskmanager.core.ui.theme.onSurfaceVariantLight
import com.danioliveira.taskmanager.core.ui.theme.onTertiaryContainerDark
import com.danioliveira.taskmanager.core.ui.theme.onTertiaryContainerLight
import com.danioliveira.taskmanager.core.ui.theme.onTertiaryDark
import com.danioliveira.taskmanager.core.ui.theme.onTertiaryLight
import com.danioliveira.taskmanager.core.ui.theme.outlineDark
import com.danioliveira.taskmanager.core.ui.theme.outlineLight
import com.danioliveira.taskmanager.core.ui.theme.outlineVariantDark
import com.danioliveira.taskmanager.core.ui.theme.outlineVariantLight
import com.danioliveira.taskmanager.core.ui.theme.primaryContainerDark
import com.danioliveira.taskmanager.core.ui.theme.primaryContainerLight
import com.danioliveira.taskmanager.core.ui.theme.primaryDark
import com.danioliveira.taskmanager.core.ui.theme.primaryLight
import com.danioliveira.taskmanager.core.ui.theme.scrimDark
import com.danioliveira.taskmanager.core.ui.theme.scrimLight
import com.danioliveira.taskmanager.core.ui.theme.secondaryContainerDark
import com.danioliveira.taskmanager.core.ui.theme.secondaryContainerLight
import com.danioliveira.taskmanager.core.ui.theme.secondaryDark
import com.danioliveira.taskmanager.core.ui.theme.secondaryLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceBrightDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceBrightLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerHighDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerHighLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerHighestDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerHighestLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerLowDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerLowLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerLowestDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceContainerLowestLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceDimDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceDimLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceLight
import com.danioliveira.taskmanager.core.ui.theme.surfaceVariantDark
import com.danioliveira.taskmanager.core.ui.theme.surfaceVariantLight
import com.danioliveira.taskmanager.core.ui.theme.tertiaryContainerDark
import com.danioliveira.taskmanager.core.ui.theme.tertiaryContainerLight
import com.danioliveira.taskmanager.core.ui.theme.tertiaryDark
import com.danioliveira.taskmanager.core.ui.theme.tertiaryLight

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun TaskItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        typography = AppTypography,
        shapes = TaskItShapes,
        content = content
    )
}

