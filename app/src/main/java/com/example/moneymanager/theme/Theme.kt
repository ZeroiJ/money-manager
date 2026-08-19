package com.example.moneymanager.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NeoDarkColorScheme = darkColorScheme(
    primary = NeoYellow,
    onPrimary = NeoBlack,
    primaryContainer = NeoBlack,
    onPrimaryContainer = NeoYellow,
    secondary = NeoCyan,
    onSecondary = NeoBlack,
    secondaryContainer = NeoGray900,
    onSecondaryContainer = NeoCyan,
    tertiary = NeoLime,
    onTertiary = NeoBlack,
    background = NeoBlack,
    onBackground = NeoWhite,
    surface = NeoGray900,
    onSurface = NeoWhite,
    surfaceVariant = NeoDarkCard,
    onSurfaceVariant = NeoGray200,
    surfaceContainer = NeoDarkSurface,
    outline = NeoWhite,
    error = NeoRed,
    onError = NeoWhite
)

private val NeoLightColorScheme = lightColorScheme(
    primary = NeoBlack,
    onPrimary = NeoWhite,
    primaryContainer = NeoYellow,
    onPrimaryContainer = NeoBlack,
    secondary = NeoLime,
    onSecondary = NeoBlack,
    secondaryContainer = NeoGray100,
    onSecondaryContainer = NeoBlack,
    tertiary = NeoOrange,
    onTertiary = NeoWhite,
    background = NeoOffWhite,
    onBackground = NeoBlack,
    surface = NeoWhite,
    onSurface = NeoBlack,
    surfaceVariant = NeoGray100,
    onSurfaceVariant = NeoGray700,
    outline = NeoBlack,
    error = NeoRed,
    onError = NeoWhite
)

@Composable
fun MoneyManagerTheme(
    darkTheme: Boolean = false, // High-contrast crisp Neo-Brutalist default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NeoDarkColorScheme
        else -> NeoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
