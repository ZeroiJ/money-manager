package com.example.moneymanager.worker

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import com.example.moneymanager.util.LiveUpdateHelper

class RecurringExpenseWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = LiveUpdateHelper.buildDatabase(context)
            val dao = db.moneyDao()
            val now = System.currentTimeMillis()
            val dueRules = dao.getDueRecurringRules(now)

            if (dueRules.isNotEmpty()) {
                for (rule in dueRules) {
                    dao.insertTransaction(rule.toTransaction(now))
                    dao.updateRecurringRule(rule.copy(nextDueDate = rule.advanceNextDue()))
                }
                LiveUpdateHelper.ensureChannels(context)
                LiveUpdateHelper.showProcessedNotification(context, dueRules.size)
            }

            val futureDate = now + 7L * 24 * 60 * 60 * 1000
            val upcomingRules = dao.getUpcomingRecurringRules(now, futureDate)
            if (upcomingRules.isNotEmpty()) {
                LiveUpdateHelper.ensureChannels(context)
                LiveUpdateHelper.postUpcomingBillsLiveUpdate(context, upcomingRules, now)
            }

            db.close()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}