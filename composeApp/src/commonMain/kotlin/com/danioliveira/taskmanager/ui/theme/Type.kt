package com.danioliveira.taskmanager.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.poppins_bold
import kmmtaskmanager.composeapp.generated.resources.poppins_medium
import kmmtaskmanager.composeapp.generated.resources.poppins_regular
import kmmtaskmanager.composeapp.generated.resources.poppins_semibold
import kmmtaskmanager.composeapp.generated.resources.inter_medium
import kmmtaskmanager.composeapp.generated.resources.inter_regular
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
            h1 = baseline.h1.copy(fontFamily = displayFontFamily),
            h2 = baseline.h2.copy(fontFamily = displayFontFamily),
            h3 = baseline.h3.copy(fontFamily = displayFontFamily),
            h4 = baseline.h4.copy(fontFamily = displayFontFamily),
            h5 = baseline.h5.copy(fontFamily = displayFontFamily),
            h6 = baseline.h6.copy(fontFamily = displayFontFamily),
            subtitle1 = baseline.subtitle1.copy(fontFamily = displayFontFamily),
            subtitle2 = baseline.subtitle2.copy(fontFamily = displayFontFamily),
            body1 = baseline.body1.copy(fontFamily = bodyFontFamily),
            body2 = baseline.body2.copy(fontFamily = bodyFontFamily),
            button = baseline.button.copy(fontFamily = displayFontFamily),
            caption = baseline.caption.copy(fontFamily = bodyFontFamily),
            overline = baseline.overline.copy(fontFamily = bodyFontFamily)
        )
