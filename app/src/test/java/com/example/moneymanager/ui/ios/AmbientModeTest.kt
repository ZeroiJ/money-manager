package com.example.moneymanager.ui.ios

import org.junit.Assert.assertEquals
import org.junit.Test

class AmbientModeTest {

    @Test fun reducedMotion_forcesStatic() {
        assertEquals(AmbientMode.Static, resolveAmbientMode(animatorScale = 0f, userEnabled = true))
    }

    @Test fun toggleOff_forcesStatic() {
        assertEquals(AmbientMode.Static, resolveAmbientMode(animatorScale = 1f, userEnabled = false))
    }

    @Test fun default_animates() {
        assertEquals(AmbientMode.Animated, resolveAmbientMode(animatorScale = 1f, userEnabled = true))
    }
}
