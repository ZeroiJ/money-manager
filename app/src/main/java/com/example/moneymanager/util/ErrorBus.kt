package com.example.moneymanager.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide error bus. Any layer (ViewModels, workers, utils) reports here;
 * the nav shell shows a snackbar, and Settings → Diagnostics lists history.
 *
 * Plain `object` (not Hilt) so non-injected classes like Workers can report.
 * Everything stays on-device — this is a logbook, not telemetry.
 */
object ErrorBus {
    const val MAX_RECENT = 20

    private val _events = MutableSharedFlow<AppError>(extraBufferCapacity = 8)
    val events: SharedFlow<AppError> = _events.asSharedFlow()

    private val _recent = MutableStateFlow<List<AppError>>(emptyList())
    val recent: StateFlow<List<AppError>> = _recent.asStateFlow()

    /** Record + broadcast. Never throws — reporting must not crash the app. */
    fun report(error: AppError) {
        _recent.value = (_recent.value + error).takeLast(MAX_RECENT)
        _events.tryEmit(error)
    }

    fun report(kind: ErrorKind, where: String, message: String, cause: Throwable? = null) {
        report(AppError.of(kind, where, message, cause))
    }

    fun clear() {
        _recent.value = emptyList()
    }
}
