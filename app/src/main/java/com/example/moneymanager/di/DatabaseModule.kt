package com.example.moneymanager.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "money_manager.db"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed Indian default categories (ARGB colors)
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Groceries', 'shopping_cart', 4282561914, 1)") // Teal
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Chai & Snacks', 'local_cafe', 4293498930, 1)") // Warm Orange
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Food Delivery', 'restaurant', 4294936370, 1)") // Coral Red
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Rent', 'home', 4284979146, 1)") // Indigo
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Electricity', 'bolt', 4294947634, 1)") // Amber/Yellow
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Mobile Recharge', 'phone_android', 4280391411, 1)") // Blue
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Transport', 'directions_car', 4288585324, 1)") // Cyan
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Education', 'school', 4287320010, 1)") // Purple
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Subscriptions', 'subscriptions', 4291843257, 1)") // Deep Pink
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Medical', 'medical_services', 4293949774, 1)") // Red
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Shopping', 'shopping_bag', 4291398867, 1)") // Violet
                db.execSQL("INSERT INTO categories (name, icon, color, isDefault) VALUES ('Misc', 'category', 4286545791, 1)") // Grey/Slate

                // Seed Default Household Members
                db.execSQL("INSERT INTO household_members (name) VALUES ('Me')")
                db.execSQL("INSERT INTO household_members (name) VALUES ('Roommate')")
            }
        })
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideMoneyDao(appDatabase: AppDatabase): MoneyDao {
        return appDatabase.moneyDao()
    }
}
