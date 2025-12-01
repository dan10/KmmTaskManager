package com.danioliveira.taskmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.inter_medium
import kmmtaskmanager.composeapp.generated.resources.inter_regular
import kmmtaskmanager.composeapp.generated.resources.poppins_bold
import kmmtaskmanager.composeapp.generated.resources.poppins_medium
import kmmtaskmanager.composeapp.generated.resources.poppins_regular
import kmmtaskmanager.composeapp.generated.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

val bodyFontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.inter_regular, weight = FontWeight.Normal),
        Font(Res.font.inter_medium, weight = FontWeight.Medium),
    )


val displayFontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.poppins_regular, weight = FontWeight.Normal),
        Font(Res.font.poppins_medium, weight = FontWeight.Medium),
        Font(Res.font.poppins_semibold, weight = FontWeight.SemiBold),
        Font(Res.font.poppins_bold, weight = FontWeight.Bold),
    )


// Default Material 3 typography values
val baseline = Typography()

val AppTypography
    @Composable
    get() =
        Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
            displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
            displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
            titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
            titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
            titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
            bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
            labelLarge = baseline.labelLarge.copy(fontFamily = displayFontFamily),
            labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
            labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily)
        )
