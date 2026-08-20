package com.example.moneymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType { EXPENSE, INCOME }
enum class TransactionScope { PERSONAL, HOUSEHOLD }
enum class PaymentMode { CASH, UPI, CARD }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val note: String,
    val date: Long, // Storing as Unix timestamp for simplicity
    val paymentMode: PaymentMode,
    val scope: TransactionScope,
    val paidBy: String? = null,
    val receiptUri: String? = null
)
