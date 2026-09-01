package com.bigfatfish.release.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val pageBackground: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val balancePrimary: Color,
    val errorTitle: Color,
    val errorText: Color,
    val errorBg: Color,
    val errorBtnBg: Color,
    val errorBtnText: Color,
    val reasoningBackground: Color,
    val tealBtn: Color,
    val indigoBtn: Color,
    val blueGreyBtn: Color,
    val successBtn: Color,
    val dangerBtn: Color,
    val plushBtn: Color,
    val btnBgAlpha: Float
)

val LightAppColors = AppColors(
    pageBackground = LightPageBackground,
    cardBackground = LightCardBackground,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textHint = LightTextHint,
    balancePrimary = LightBalancePrimary,
    errorTitle = LightErrorTitle,
    errorText = LightErrorText,
    errorBg = LightErrorBg,
    errorBtnBg = LightErrorBtnBg,
    errorBtnText = LightErrorBtnText,
    reasoningBackground = LightReasoningBackground,
    tealBtn = TealBtnText,
    indigoBtn = IndigoBtnText,
    blueGreyBtn = BlueGreyBtnText,
    successBtn = SuccessBtnText,
    dangerBtn = DangerBtnText,
    plushBtn = PlushBtnText,
    btnBgAlpha = 0.25f
)

val DarkAppColors = AppColors(
    pageBackground = DarkPageBackground,
    cardBackground = DarkCardBackground,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textHint = DarkTextHint,
    balancePrimary = DarkBalancePrimary,
    errorTitle = DarkErrorTitle,
    errorText = DarkErrorText,
    errorBg = DarkErrorBg,
    errorBtnBg = DarkErrorBtnBg,
    errorBtnText = DarkErrorBtnText,
    reasoningBackground = DarkReasoningBackground,
    tealBtn = DarkTealBtn,
    indigoBtn = DarkIndigoBtn,
    blueGreyBtn = DarkBlueGreyBtn,
    successBtn = DarkSuccessBtn,
    dangerBtn = DarkDangerBtn,
    plushBtn = DarkPlushBtn,
    btnBgAlpha = 0.18f
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

@Composable
fun BigFatFishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    skin: String = "default",
    content: @Composable () -> Unit
) {
    val base = if (darkTheme) DarkAppColors else LightAppColors
    val appColors = if (skin == "fishBlue") {
        if (darkTheme) {
            base.copy(
                balancePrimary = FishBlueDarkPrimary,
                pageBackground = FishBlueDarkBg,
                cardBackground = FishBlueDarkCard,
                tealBtn = FishBlueDarkTealBtn,
                indigoBtn = FishBlueDarkIndigoBtn,
                blueGreyBtn = FishBlueDarkBlueGreyBtn,
                reasoningBackground = FishBlueDarkReasoning
            )
        } else {
            base.copy(
                balancePrimary = FishBlueLightPrimary,
                pageBackground = FishBlueLightBg,
                cardBackground = FishBlueLightCard,
                tealBtn = FishBlueLightTealBtn,
                indigoBtn = FishBlueLightIndigoBtn,
                blueGreyBtn = FishBlueLightBlueGreyBtn,
                reasoningBackground = FishBlueLightReasoning
            )
        }
    } else base
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appColors.balancePrimary,
            background = appColors.pageBackground,
            surface = appColors.cardBackground
        )
    } else {
        lightColorScheme(
            primary = appColors.balancePrimary,
            background = appColors.pageBackground,
            surface = appColors.cardBackground
        )
    }
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
