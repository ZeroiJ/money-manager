package com.example.moneymanager

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.moneymanager.worker.RecurringExpenseWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MoneyManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupRecurringWorker()
    }

    private fun setupRecurringWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_expense_checker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
