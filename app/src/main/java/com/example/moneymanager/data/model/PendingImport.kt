package com.example.moneymanager.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_imports",
    indices = [Index(value = ["referenceNo"], unique = true)]
)
data class PendingImport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val date: Long,
    val paymentMode: PaymentMode = PaymentMode.UPI,
    val scope: TransactionScope = TransactionScope.PERSONAL,
    val referenceNo: String? = null,
    val sender: String = ""
)
