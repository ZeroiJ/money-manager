package com.example.moneymanager.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IosTokensTest {

    @Test fun gradients_light() {
        assertEquals(
            listOf(Color(0xFFFFF6EA), Color(0xFFFFDFB5), Color(0xFFFF9500)),
            Ios.Light.gradient
        )
    }

    @Test fun gradients_dark() {
        assertEquals(
            listOf(Color(0xFF060B24), Color(0xFF101C4E), Color(0xFF0A84FF)),
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

    @Test fun radii_matchSpec() {
        assertEquals(12.dp, Ios.CardRadius)
        assertEquals(16.dp, Ios.SheetRadius)
    }
}
