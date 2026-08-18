package com.example.moneymanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction

@Database(
    entities = [Transaction::class, Category::class, Budget::class, RecurringRule::class, HouseholdMember::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moneyDao(): MoneyDao
}
