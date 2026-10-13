package com.example.moneymanager.ui.ios

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NessieLogicTest {

    @Test fun firstLoad_neverCelebrates() {
        assertFalse(firstEntryTransition(wasEmpty = null, isEmptyNow = false, celebrated = false))
        assertFalse(firstEntryTransition(wasEmpty = null, isEmptyNow = true, celebrated = false))
    }

    @Test fun emptyToNonEmpty_celebratesOnce() {
        assertTrue(firstEntryTransition(wasEmpty = true, isEmptyNow = false, celebrated = false))
    }

    @Test fun alreadyCelebrated_neverAgain() {
        assertFalse(firstEntryTransition(wasEmpty = true, isEmptyNow = false, celebrated = true))
    }

    @Test fun steadyStates_neverCelebrate() {
        assertFalse(firstEntryTransition(wasEmpty = true, isEmptyNow = true, celebrated = false))
        assertFalse(firstEntryTransition(wasEmpty = false, isEmptyNow = false, celebrated = false))
        assertFalse(firstEntryTransition(wasEmpty = false, isEmptyNow = true, celebrated = false))
    }
}
