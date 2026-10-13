package com.example.moneymanager.ui.ios

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalDensity
import com.example.moneymanager.theme.Ios

/**
 * SPIKE (uncommitted experiment): ShaderGradient-style flowing mesh background.
 *
 * Real GPU shader (AGSL, API 33+) instead of the animated linear-gradient wash.
 * Caller must keep the old Brush path for API < 33 — see AmbientBackground.
 * Revert = delete this file + restore the animated branch there.
 */
private const val MESH_SHADER = """
uniform vec2 iResolution;
uniform float iTime;
uniform vec3 uColor0;
uniform vec3 uColor1;
uniform vec3 uColor2;
uniform vec3 uColor3;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

vec4 main(vec2 fragCoord) {
    vec2 uv = fragCoord / iResolution;
    float t = iTime;
    vec2 warp = vec2(
        fbm(uv * 2.0 + vec2(t * 0.05, -t * 0.035)),
        fbm(uv * 2.0 + vec2(-t * 0.03, t * 0.05))
    );
    float m1 = fbm(uv * 2.5 + warp * 1.5 + vec2(t * 0.025, 0.0));
    float m2 = fbm(uv * 3.0 - warp * 1.2 + vec2(0.0, t * 0.02));
    vec3 col = mix(uColor0, uColor1, smoothstep(0.15, 0.75, m1));
    col = mix(col, uColor2, smoothstep(0.25, 0.85, m2));
    float glow = smoothstep(0.55, 0.95, fbm(uv * 1.5 + warp + t * 0.015));
    col = mix(col, uColor3, glow * 0.4);
    float g = hash(fragCoord + fract(t) * 100.0) - 0.5;
    col += g * 0.06;
    return vec4(col, 1.0);
}
"""

private fun rgb(c: Color): Triple<Float, Float, Float> =
    Triple(c.red, c.green, c.blue)

@Composable
fun MeshGradientBackground(
    dark: Boolean,
    modifier: Modifier = Modifier,
    animate: Boolean = false
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Pre-33 fallback: static wash (caller normally routes here only on 33+).
        val colors = (if (dark) Ios.Dark else Ios.Light).gradient
        Box(modifier.fillMaxSize().background(Brush.linearGradient(colors)))
        return
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        // Quiet ledger: the still is the design (texture, not motion). The loop
        // stays available but off — ambient texture costs zero battery.
        val t = if (animate) {
            val transition = rememberInfiniteTransition(label = "mesh")
            val drifted by transition.animateFloat(
                initialValue = 0f,
                targetValue = 24f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 24000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "meshTime"
            )
            drifted
        } else {
            7.3f
        }
        // Muted near-monochrome quads: texture reads as paper grain's big
        // brother, never as a neon band (see v1.11.0 device feedback).
        val quad = if (dark) {
            listOf(
                Color(0xFF050914), Color(0xFF0D1738),
                Color(0xFF1B2A6B), Color(0xFF35509E)
            )
        } else {
            listOf(
                Color(0xFFFFF6EA), Color(0xFFF5E7CF),
                Color(0xFFE3C491), Color(0xFFD0934F)
            )
        }
        val brush = remember(t, dark, wPx, hPx) {
            RuntimeShader(MESH_SHADER).apply {
                setFloatUniform("iResolution", wPx, hPx)
                setFloatUniform("iTime", t)
                quad.forEachIndexed { i, c ->
                    val (r, g, b) = rgb(c)
                    setFloatUniform("uColor$i", r, g, b)
                }
            }.let(::ShaderBrush)
        }
        Box(Modifier.fillMaxSize().background(brush))
    }
}
