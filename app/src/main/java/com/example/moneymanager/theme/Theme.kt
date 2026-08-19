package com.example.moneymanager.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ChromaDarkColorScheme = darkColorScheme(
    primary = ChromaOrange,
    onPrimary = ChromaWhite,
    primaryContainer = ChromaBlack,
    onPrimaryContainer = ChromaOrange,
    secondary = ChromaCyan,
    onSecondary = ChromaBlack,
    secondaryContainer = ChromaStone900,
    onSecondaryContainer = ChromaCyan,
    tertiary = ChromaGreen,
    onTertiary = ChromaBlack,
    background = ChromaStone950,
    onBackground = ChromaStone50,
    surface = ChromaStone900,
    onSurface = ChromaStone50,
    surfaceVariant = ChromaStone800,
    onSurfaceVariant = ChromaStone300,
    surfaceContainer = ChromaStone900,
    outline = ChromaStone50,
    error = ChromaRed,
    onError = ChromaWhite
)

private val ChromaLightColorScheme = lightColorScheme(
    primary = ChromaBlack,
    onPrimary = ChromaWhite,
    primaryContainer = ChromaOrange,
    onPrimaryContainer = ChromaWhite,
    secondary = ChromaBlue,
    onSecondary = ChromaWhite,
    secondaryContainer = ChromaStone100,
    onSecondaryContainer = ChromaBlack,
    tertiary = ChromaGreen,
    onTertiary = ChromaWhite,
    background = ChromaStone50,
    onBackground = ChromaBlack,
    surface = ChromaWhite,
    onSurface = ChromaBlack,
    surfaceVariant = ChromaStone100,
    onSurfaceVariant = ChromaStone600,
    outline = ChromaBlack,
    error = ChromaRed,
    onError = ChromaWhite
)

@Composable
fun MoneyManagerTheme(
    darkTheme: Boolean = false, // Chroma warm stone light mode default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ChromaDarkColorScheme
        else -> ChromaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
