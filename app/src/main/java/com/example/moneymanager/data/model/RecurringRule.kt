package com.example.moneymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
