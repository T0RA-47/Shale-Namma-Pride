package com.shalenammapride.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ShaleLightColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = PureWhite,
    primaryContainer = SaffronLight,
    onPrimaryContainer = SaffronDark,
    secondary = IndiaGreen,
    onSecondary = PureWhite,
    secondaryContainer = IndiaGreenLight,
    onSecondaryContainer = IndiaGreenDark,
    background = LightGray,
    onBackground = DarkText,
    surface = PureWhite,
    onSurface = DarkText,
    surfaceVariant = OffWhite,
    onSurfaceVariant = MediumText,
    error = ErrorRed,
    onError = PureWhite,
    outline = CardBorder,
)

private val ShaleDarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = DarkText,
    primaryContainer = SaffronDark,
    onPrimaryContainer = SaffronLight,
    secondary = IndiaGreen,
    onSecondary = DarkText,
    secondaryContainer = IndiaGreenDark,
    onSecondaryContainer = IndiaGreenLight,
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextMedium,
    error = ErrorRed,
    onError = PureWhite,
    outline = NightCardBorder,
)

@Composable
fun ShaleNammaPrideTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) ShaleDarkColorScheme else ShaleLightColorScheme,
        typography = ShaleTypography,
        content = content
    )
}
