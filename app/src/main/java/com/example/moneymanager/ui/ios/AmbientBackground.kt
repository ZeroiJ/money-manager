package com.example.moneymanager.ui.ios

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.moneymanager.theme.Ios

enum class AmbientMode { Animated, Static }

/** Static unless the user opted in AND system animation is not disabled. */
fun resolveAmbientMode(animatorScale: Float, userEnabled: Boolean): AmbientMode =
    if (!userEnabled || animatorScale == 0f) AmbientMode.Static else AmbientMode.Animated

/** System animator scale; 0 means reduced-motion. Thin wrapper, read at composition time. */
fun readAnimatorScale(context: Context): Float =
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )

/**
 * Single full-screen gradient layer (§3b). Content must be drawn ABOVE this —
 * never put bare text on it; text lives on solid opaque cards.
 */
@Composable
fun AmbientBackground(
    mode: AmbientMode,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = (if (dark) Ios.Dark else Ios.Light).gradient
    if (mode == AmbientMode.Static) {
        Box(modifier.fillMaxSize().background(Brush.linearGradient(colors)))
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Quiet ledger: a frozen mesh still (texture, not motion) + grain.
        // The loop is off — ambient texture costs zero battery. The toggle
        // now means texture on/off; Static is the flat canvas.
        MeshGradientBackground(dark = dark, modifier = modifier)
    } else {
        val transition = rememberInfiniteTransition(label = "ambient")
        val drift by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "drift"
        )
        Box(
            modifier.fillMaxSize().background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(x = drift * 1080f, y = 0f),
                    end = Offset(x = 1080f - drift * 1080f, y = 1920f)
                )
            )
        )
    }
}
