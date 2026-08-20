package com.example.moneymanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction

@Database(
    entities = [Transaction::class, Category::class, Budget::class, RecurringRule::class, HouseholdMember::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moneyDao(): MoneyDao

    companion object {
        // v1 -> v2: add optional receiptUri column to transactions
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN receiptUri TEXT DEFAULT NULL")
            }
        }
    }
}
