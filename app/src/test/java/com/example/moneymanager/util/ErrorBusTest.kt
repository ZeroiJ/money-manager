package com.example.moneymanager.util

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ErrorBusTest {

    @Before
    fun setUp() {
        ErrorBus.clear()
    }

    @After
    fun tearDown() {
        ErrorBus.clear()
    }

    @Test fun report_broadcastsEvent() = runTest {
        val seen = mutableListOf<AppError>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ErrorBus.events.collect { seen.add(it) }
        }

        ErrorBus.report(ErrorKind.DB_WRITE, "Add expense", "Couldn't save", null)
        testScheduler.advanceUntilIdle()

        assertEquals(1, seen.size)
        assertEquals(ErrorKind.DB_WRITE, seen.first().kind)
        assertEquals("Add expense", seen.first().where)
        assertNull(seen.first().cause)
    }

    @Test fun report_formatsCause() = runTest {
        ErrorBus.report(
            ErrorKind.IMPORT_FAILED, "Settings → Import",
            "Couldn't read file", IllegalStateException("boom")
        )

        val last = ErrorBus.recent.value.last()
        assertEquals("IllegalStateException: boom", last.cause)
        assertTrue(last.at > 0)
    }

    @Test fun recent_capsAtMax() = runTest {
        repeat(ErrorBus.MAX_RECENT + 5) { i ->
            ErrorBus.report(ErrorKind.DB_READ, "Feed $i", "Read failed $i")
        }

        val recent = ErrorBus.recent.value
        assertEquals(ErrorBus.MAX_RECENT, recent.size)
        assertEquals("Feed 5", recent.first().where)
        assertEquals("Feed ${ErrorBus.MAX_RECENT + 4}", recent.last().where)
    }

    @Test fun clear_emptiesHistory() = runTest {
        ErrorBus.report(ErrorKind.EXPORT_FAILED, "Settings → Export", "Write failed")
        assertEquals(1, ErrorBus.recent.value.size)

        ErrorBus.clear()

        assertTrue(ErrorBus.recent.value.isEmpty())
    }

    @Test fun report_neverThrows() = runTest {
        // events flow starts with no collector here; tryEmit must not crash,
        // and subscribing afterwards still gets subsequently reported events.
        ErrorBus.report(ErrorKind.DB_WRITE, "Edit", "Couldn't attach photo")
        assertEquals(1, ErrorBus.recent.value.size)

        val seen = mutableListOf<AppError>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ErrorBus.events.collect { seen.add(it) }
        }
        ErrorBus.report(ErrorKind.DB_WRITE, "Edit", "Couldn't attach photo")
        testScheduler.advanceUntilIdle()
        assertEquals(1, seen.size)
    }
}
