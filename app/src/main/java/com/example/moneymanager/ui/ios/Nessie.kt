package com.example.moneymanager.ui.ios

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.theme.PaperMono
import com.example.moneymanager.util.Haptics
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.material3.Text

/** Global privacy mask: when true, [IosAmount] renders bullets instead of figures. */
val LocalHideAmounts = compositionLocalOf { false }

/** Mask-aware currency: bullets when privacy is on, ₹ single-decimal otherwise. */
@Composable
fun maskedCurrency(amount: Double): String =
    if (LocalHideAmounts.current) "••••••"
    else com.example.moneymanager.util.FormatUtils.formatLedger(amount)

/**
 * Editorial hero word. Kinetic Ledger voice = Space Grotesk bold (the
 * Playfair serif was retired with the Nessie direction).
 */
@Composable
fun IosHeroWord(
    text: String,
    color: Color,
    fontSize: TextUnit = 30.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Shrink-a-touch press feedback (~0.2s budget like the reference).
 * Compose-safe alongside Button's own ripple/indication.
 */
fun Modifier.pressShrink(enabled: Boolean = true): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "press"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                }
            )
        }
}

/**
 * Pure state machine for the first-entry celebration.
 * Returns true only on the empty -> non-empty transition, once ever.
 */
fun firstEntryTransition(
    wasEmpty: Boolean?,
    isEmptyNow: Boolean,
    celebrated: Boolean
): Boolean = wasEmpty == true && !isEmptyNow && !celebrated

private data class Particle(
    val dx: Float, // x drift as fraction of width
    val vy: Float, // downward speed as fraction of height per unit progress
    val sizePx: Float,
    val color: Color,
    val circle: Boolean,
    val spin: Float, // rotations over the burst
    val delay: Float
)

/**
 * One-shot confetti burst from top-center + serif celebration card.
 * Reduced-motion: static card, no particles. Auto-dismisses via [onDone].
 */
@Composable
fun ConfettiOverlay(
    show: Boolean,
    title: String,
    subtitle: String,
    onDone: () -> Unit,
    scheme: IosScheme = currentScheme(),
    colors: List<Color> = listOf(
        scheme.accent,
        scheme.success,
        Color(0xFFFF9500),
        Color.White,
        Color(0xFFAF52DE)
    )
) {
    if (!show) return
    val view = LocalView.current
    val animated = remember {
        try {
            readAnimatorScale(view.context) != 0f
        } catch (_: Exception) {
            true
        }
    }
    val particles = remember {
        val rnd = Random(42)
        List(90) {
            Particle(
                dx = (rnd.nextFloat() - 0.5f) * 1.2f,
                vy = 0.45f + rnd.nextFloat() * 0.75f,
                sizePx = 8f + rnd.nextFloat() * 10f,
                color = colors[rnd.nextInt(colors.size)],
                circle = rnd.nextBoolean(),
                spin = (rnd.nextFloat() - 0.5f) * 3f,
                delay = rnd.nextFloat() * 0.25f
            )
        }
    }
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(show) {
        Haptics.saveConfirmed(view)
        if (!animated) {
            delay(1200)
            onDone()
            return@LaunchedEffect
        }
        val start = System.nanoTime()
        while (true) {
            val p = (System.nanoTime() - start) / 1_600_000_000f
            if (p >= 1f) break
            progress = p
            kotlinx.coroutines.delay(16)
        }
        progress = 1f
        onDone()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { /* swallow touches during the party */ }
            },
        contentAlignment = Alignment.Center
    ) {
        if (animated) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                particles.forEach { pt ->
                    val t = ((progress - pt.delay) / (1f - pt.delay)).coerceIn(0f, 1f)
                    if (t <= 0f) return@forEach
                    val x = w * 0.5f + pt.dx * w * t
                    val y = -0.02f * h + pt.vy * h * t + 0.35f * h * t * t
                    val alpha = (1f - t).coerceIn(0f, 1f)
                    val c = pt.color.copy(alpha = alpha)
                    if (pt.circle) {
                        drawCircle(c, radius = pt.sizePx / 2f, center = Offset(x, y))
                    } else {
                        rotate(pt.spin * 360f * t, Offset(x, y)) {
                            drawRect(c, topLeft = Offset(x - pt.sizePx / 2f, y - pt.sizePx / 4f), size = androidx.compose.ui.geometry.Size(pt.sizePx, pt.sizePx / 2f))
                        }
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            IosHeroWord(
                text = title,
                color = scheme.label,
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontFamily = SpaceGrotesk,
                fontSize = 15.sp,
                color = scheme.secondaryLabel,
                textAlign = TextAlign.Center
            )
        }
    }
}
