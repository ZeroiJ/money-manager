package com.example.moneymanager.data.db

import androidx.room.TypeConverter
import com.example.moneymanager.data.model.Frequency
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = runCatching {
        TransactionType.valueOf(value)
    }.getOrDefault(TransactionType.EXPENSE)

    @TypeConverter
    fun fromTransactionScope(value: TransactionScope): String = value.name

    @TypeConverter
    fun toTransactionScope(value: String): TransactionScope = runCatching {
        TransactionScope.valueOf(value)
    }.getOrDefault(TransactionScope.PERSONAL)

    @TypeConverter
    fun fromPaymentMode(value: PaymentMode): String = value.name

    @TypeConverter
    fun toPaymentMode(value: String): PaymentMode = runCatching {
        PaymentMode.valueOf(value)
    }.getOrDefault(PaymentMode.UPI)

    @TypeConverter
    fun fromFrequency(value: Frequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): Frequency = runCatching {
        Frequency.valueOf(value)
    }.getOrDefault(Frequency.MONTHLY)
}
