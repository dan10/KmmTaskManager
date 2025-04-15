package com.danioliveira.taskmanager.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val lightScheme = lightColors(
    primary = primaryLight,
    primaryVariant = secondaryLight,
    secondary = tertiaryLight,
    secondaryVariant = tertiaryContainerLight,
    background = backgroundLight,
    surface = surfaceLight,
    onPrimary = onPrimaryLight,
    onSecondary = onSecondaryLight,
    onBackground = onBackgroundLight,
    onSurface = onSurfaceLight,
    error = errorLight,
    onError = onErrorLight,
)

private val darkScheme = darkColors(
    primary = primaryDark,
    primaryVariant = secondaryDark,
    secondary = tertiaryDark,
    secondaryVariant = tertiaryContainerDark,
    background = backgroundDark,
    surface = surfaceDark,
    onPrimary = onPrimaryDark,
    onSecondary = onSecondaryDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    error = errorDark,
    onError = onErrorDark,
)


@Composable
fun TaskItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
  MaterialTheme(
    colors = if (darkTheme) darkScheme else lightScheme,
    typography = AppTypography,
      shapes = TaskItShapes,
    content = content
  )
}

