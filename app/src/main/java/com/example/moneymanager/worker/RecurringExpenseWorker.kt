package com.example.moneymanager.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moneymanager.R
import com.example.moneymanager.data.db.AppDatabase
import com.example.moneymanager.data.model.Frequency
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RecurringExpenseWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "money_manager.db"
            ).build()

            val dao = db.moneyDao()
            val now = System.currentTimeMillis()
            val dueRules = dao.getDueRecurringRules(now)

            if (dueRules.isNotEmpty()) {
                val cal = Calendar.getInstance()
                for (rule in dueRules) {
                    // Create transaction for due rule
                    val transaction = Transaction(
                        amount = rule.amount,
                        type = rule.type,
                        categoryId = rule.categoryId,
                        note = "[Recurring] ${rule.note}".trim(),
                        date = now,
                        paymentMode = rule.paymentMode,
                        scope = rule.scope
                    )
                    dao.insertTransaction(transaction)

                    // Compute next due date
                    cal.timeInMillis = rule.nextDueDate
                    when (rule.frequency) {
                        Frequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                        Frequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                        Frequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                        Frequency.YEARLY -> cal.add(Calendar.YEAR, 1)
                    }
                    val updatedRule = rule.copy(nextDueDate = cal.timeInMillis)
                    dao.updateRecurringRule(updatedRule)
                }

                showProcessedNotification(dueRules.size)
            }

            val futureDate = now + 7L * 24 * 60 * 60 * 1000
            val upcomingRules = dao.getUpcomingRecurringRules(now, futureDate)
            if (upcomingRules.isNotEmpty()) {
                showUpcomingBillsNotification(upcomingRules)
            }

            db.close()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showProcessedNotification(count: Int) {
        val channelId = "recurring_expenses_channel"
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recurring Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when recurring expenses are processed"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recurring Expenses Logged")
            .setContentText("$count recurring expense(s) processed for today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    @SuppressLint("MissingPermission")
    private fun showUpcomingBillsNotification(upcomingRules: List<RecurringRule>) {
        val channelId = "upcoming_bills_channel"
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Upcoming Bills",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows upcoming recurring bills for the next 7 days"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val dateFormat = SimpleDateFormat("dd MMM", Locale.US)
        val lines = upcomingRules.take(5).map { rule ->
            val dateStr = dateFormat.format(Date(rule.nextDueDate))
            "$dateStr - ${rule.note} (\u20B9${rule.amount.toInt()})"
        }

        val bigText = lines.joinToString("\n")
        val summaryText = "${upcomingRules.size} bill(s) due in next 7 days"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming Bills")
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        notificationManager.notify(1002, notification)
    }
}
