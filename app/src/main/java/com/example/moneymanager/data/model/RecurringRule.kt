package com.example.moneymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

enum class Frequency { DAILY, WEEKLY, MONTHLY, YEARLY }

@Entity(tableName = "recurring_rules")
data class RecurringRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val note: String,
    val paymentMode: PaymentMode,
    val scope: TransactionScope,
    val frequency: Frequency,
    val nextDueDate: Long
) {
    fun toTransaction(date: Long): Transaction = Transaction(
        amount = amount,
        type = type,
        categoryId = categoryId,
        note = "[Recurring] $note".trim(),
        date = date,
        paymentMode = paymentMode,
        scope = scope
    )

    fun advanceNextDue(): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = nextDueDate }
        when (frequency) {
            Frequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            Frequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            Frequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            Frequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}
