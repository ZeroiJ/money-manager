package com.example.moneymanager.util

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Rich haptics with a feature-checked ladder, never throwing:
 *
 * 1. API 30+ with a vibrator present  -> `VibrationEffect` primitives (true
 *    THUD / TICK / QUICK_RISE waveforms, the app-facing PWLE path).
 * 2. API 34+ fallback                  -> `performHapticFeedback` CONFIRM/REJECT
 *    (input-pipeline haptics, no permission needed).
 * 3. Universal fallback                -> KEYBOARD_TAP / LONG_PRESS.
 *
 * Every call site passes `LocalView.current`; null views and missing vibrators
 * are no-ops. Haptics are strictly best-effort and must never crash, including
 * on emulators without any haptic hardware.
 */
object Haptics {

    /** Calculator keypad tap. */
    fun keyPress(view: View?) {
        if (playPrimitive(view, VibrationEffect.Composition.PRIMITIVE_TICK)) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /** Transaction committed. */
    fun saveConfirmed(view: View?) {
        if (playPrimitive(view, VibrationEffect.Composition.PRIMITIVE_THUD)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /** Monthly budget limit crossed. */
    fun budgetLimitCrossed(view: View?) {
        if (playPrimitive(view, VibrationEffect.Composition.PRIMITIVE_QUICK_RISE)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Play a `VibrationEffect` primitive via the legacy Vibrator service.
     * Returns false when unavailable so callers fall back to feedback constants.
     */
    private fun playPrimitive(view: View?, primitive: Int): Boolean {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return runCatching {
            val vibrator = view.context.getSystemService(Vibrator::class.java)
            if (vibrator != null && vibrator.hasVibrator()) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(primitive)
                    .compose()
                vibrator.vibrate(effect)
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }
}

/**
 * Compose wrapper: fires [effect] exactly once per false → true edge of
 * [crossed]. State is scoped to [key], so per-item callers (e.g. one entry per
 * category in a list) get independent crossing detection. Returns without a
 * haptic on true → false, recomposition, and the very first false state.
 */
@Composable
fun HapticOnCrossing(
    key: Any?,
    view: View,
    crossed: Boolean,
    effect: (View) -> Unit
) {
    var wasCrossed by remember(key) { mutableStateOf(crossed) }
    LaunchedEffect(key, crossed) {
        if (crossed && !wasCrossed) {
            effect(view)
        }
        wasCrossed = crossed
    }
}