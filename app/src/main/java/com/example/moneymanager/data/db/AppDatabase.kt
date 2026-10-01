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
import com.example.moneymanager.data.model.PendingImport
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction

@Database(
    entities = [Transaction::class, Category::class, Budget::class, RecurringRule::class, HouseholdMember::class, PendingImport::class],
    version = 3,
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

        // v2 -> v3: add pending_imports table for the SMS review queue
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pending_imports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `merchant` TEXT NOT NULL, `date` INTEGER NOT NULL, `paymentMode` TEXT NOT NULL, `scope` TEXT NOT NULL, `referenceNo` TEXT, `sender` TEXT NOT NULL)"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_imports_referenceNo` ON `pending_imports` (`referenceNo`)")
            }
        }
    }
}
