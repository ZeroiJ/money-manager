package com.example.moneymanager.util

/**
 * What went wrong, where, and why — user-visible form.
 * [where] names the screen or job (e.g. "Home feed", "Save expense",
 * "Recurring auto-log"). [cause] is the raw exception message, may be null.
 * Everything stays on-device: this never leaves the phone.
 */
enum class ErrorKind(val label: String) {
    DB_READ("Database read"),
    DB_WRITE("Database write"),
    IMPORT_FAILED("Import"),
    EXPORT_FAILED("Export"),
    SMS_SCAN_FAILED("SMS scan"),
    RECURRING("Recurring bills")
}

data class AppError(
    val kind: ErrorKind,
    val where: String,
    val message: String,
    val cause: String? = null,
    val at: Long = System.currentTimeMillis()
) {
    /** One-line user-facing summary shown in the snackbar. */
    fun summary(): String = "$where: $message"

    companion object {
        fun of(kind: ErrorKind, where: String, message: String, cause: Throwable?): AppError =
            AppError(
                kind = kind,
                where = where,
                message = message,
                cause = cause?.let { "${it.javaClass.simpleName}: ${it.message}" }
            )
    }
}
