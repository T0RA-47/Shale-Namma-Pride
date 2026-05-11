package com.shalenammapride.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ShaleColorScheme = lightColorScheme(
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

@Composable
fun ShaleNammaPrideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShaleColorScheme,
        typography = ShaleTypography,
        content = content
    )
}
