package com.example.moneymanager.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tokens must match the Kinetic Ledger PRD §5 (sage/ochre/terracotta). */
class IosTokensTest {

    @Test fun light_canvasIsSage() {
        assertEquals(Color(0xFFB5BFAE), Ios.Light.background)
        assertEquals(Color(0xFFD9C852), Ios.Light.accent)   // ochre
        assertEquals(Color(0xFFD76449), Ios.Light.destructive) // terracotta
        assertEquals(Color(0xFF1E241C), Ios.Light.slab)
        assertEquals(Color(0xFF4A1510), Ios.Light.maroon)
    }

    @Test fun dark_canvasIsObsidian() {
        assertEquals(Color(0xFF0A0F0D), Ios.Dark.background)
        assertEquals(Color(0xFFEFBF3A), Ios.Dark.accent) // safety amber
    }

    @Test fun gradients_light() {
        assertEquals(
            listOf(Color(0xFFB5BFAE), Color(0xFFC1CBB9), Color(0xFFA8B2A0)),
            Ios.Light.gradient
        )
    }

    @Test fun gradients_dark() {
        assertEquals(
            listOf(Color(0xFF0A0F0D), Color(0xFF101613), Color(0xFF1A2320)),
            Ios.Dark.gradient
        )
    }

    @Test fun contrast_lightSecondaryOnCard() {
        assertTrue(contrastRatio(Ios.Light.secondaryLabel, Ios.Light.card) >= 4.5)
    }

    @Test fun contrast_darkSecondaryOnCard() {
        assertTrue(contrastRatio(Ios.Dark.secondaryLabel, Ios.Dark.card) >= 4.5)
    }

    @Test fun contrast_primaryOnCard_bothSchemes() {
        assertTrue(contrastRatio(Ios.Light.label, Ios.Light.card) >= 4.5)
        assertTrue(contrastRatio(Ios.Dark.label, Ios.Dark.card) >= 4.5)
    }

    @Test fun contrast_onSlab_bothSchemes() {
        assertTrue(contrastRatio(Ios.Light.onSlab, Ios.Light.slab) >= 4.5)
        assertTrue(contrastRatio(Ios.Dark.onSlab, Ios.Dark.slab) >= 4.5)
        assertTrue(contrastRatio(Ios.Light.onSlabSecondary, Ios.Light.slab) >= 4.5)
    }

    @Test fun contrast_inkOnOchre() {
        assertTrue(contrastRatio(Ios.Light.label, Ios.Light.accent) >= 4.5)
    }

    @Test fun radii_matchSpec() {
        // Brutalist: squares for cards, small radius only on sheet grabbers.
        assertEquals(0.dp, Ios.CardRadius)
        assertEquals(14.dp, Ios.SheetRadius)
    }
}
