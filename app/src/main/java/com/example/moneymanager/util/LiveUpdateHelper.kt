package com.example.moneymanager.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.example.moneymanager.R
import com.example.moneymanager.data.db.AppDatabase
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.worker.RecurringBillActionReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LiveUpdateHelper {

    const val CHANNEL_RECURRING = "recurring_expenses_channel"
    const val CHANNEL_UPCOMING = "upcoming_bills_channel"
    const val NOTIFICATION_ID_RECURRING = 1001
    const val NOTIFICATION_ID_UPCOMING = 1002

    const val ACTION_LOG_BILL = "com.example.moneymanager.action.LOG_BILL"
    const val ACTION_SNOOZE_BILL = "com.example.moneymanager.action.SNOOZE_BILL"
    const val EXTRA_RULE_ID = "com.example.moneymanager.extra.RULE_ID"

    private const val MAX_SEGMENTS = 10
    private const val MAX_POINTS = 4

    const val DAY_MILLIS = 24L * 60 * 60 * 1000

    fun buildDatabase(context: Context): AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "money_manager.db"
    ).build()

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_RECURRING,
                "Recurring Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifies when recurring expenses are processed" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_UPCOMING,
                "Upcoming Bills",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows upcoming recurring bills for the next 7 days" }
        )
    }

    @SuppressLint("MissingPermission")
    fun showProcessedNotification(context: Context, count: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("Recurring Expenses Logged")
            .setContentText("$count recurring expense(s) processed for today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_RECURRING, notification)
    }

    @SuppressLint("MissingPermission")
    fun postUpcomingBillsLiveUpdate(
        context: Context,
        upcomingRules: List<RecurringRule>,
        now: Long
    ) {
        val notifier = NotificationManagerCompat.from(context)
        if (upcomingRules.isEmpty()) {
            notifier.cancel(NOTIFICATION_ID_UPCOMING)
            return
        }

        val imminent = upcomingRules.count { it.nextDueDate <= now + DAY_MILLIS }
        val nextBill = upcomingRules.first()
        val logPi = billActionIntent(context, ACTION_LOG_BILL, nextBill.id)
        val snoozePi = billActionIntent(context, ACTION_SNOOZE_BILL, nextBill.id)

        val title = "${upcomingRules.size} bill(s) due in next 7 days"
        val dateStr = SimpleDateFormat("dd MMM", Locale.US).format(Date(nextBill.nextDueDate))
        val text = "Next: ${nextBill.note} ${FormatUtils.formatCurrency(nextBill.amount)} on $dateStr"
        val actions = listOf(
            NotificationCompat.Action(
                R.drawable.ic_check,
                "LOG ${FormatUtils.formatCurrency(nextBill.amount)}",
                logPi
            ),
            NotificationCompat.Action(
                R.drawable.ic_snooze,
                "SNOOZE 1D",
                snoozePi
            )
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            buildProgressStyle(context, upcomingRules, imminent, title, text, actions)
        } else {
            buildCompatStyle(context, upcomingRules, imminent, title, text, actions)
        }
        notifier.notify(NOTIFICATION_ID_UPCOMING, notification)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun buildProgressStyle(
        context: Context,
        rules: List<RecurringRule>,
        progress: Int,
        title: String,
        text: String,
        actions: List<NotificationCompat.Action>
    ): Notification {
        val segmentCount = rules.size.coerceAtMost(MAX_SEGMENTS)
        val style = Notification.ProgressStyle()
            .setProgress(progress.coerceIn(0, segmentCount))
            .setStyledByProgress(true)
            .setProgressStartIcon(Icon.createWithResource(context, R.drawable.ic_bill))
            .setProgressEndIcon(Icon.createWithResource(context, R.drawable.ic_check))
        repeat(segmentCount) {
            style.addProgressSegment(Notification.ProgressStyle.Segment(1))
        }
        repeat(segmentCount.coerceAtMost(MAX_POINTS)) {
            style.addProgressPoint(Notification.ProgressStyle.Point(it + 1))
        }

        val builder = Notification.Builder(context, CHANNEL_UPCOMING)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(style)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_STATUS)
        for (action in actions) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, action.icon),
                    action.title,
                    action.actionIntent
                ).build()
            )
        }
        return builder.build()
    }

    private fun buildCompatStyle(
        context: Context,
        rules: List<RecurringRule>,
        progress: Int,
        title: String,
        text: String,
        actions: List<NotificationCompat.Action>
    ): Notification {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.US)
        val lines = rules.take(5).joinToString("\n") { rule ->
            "${dateFormat.format(Date(rule.nextDueDate))} - ${rule.note} (${FormatUtils.formatCurrency(rule.amount)})"
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_UPCOMING)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines))
            .setProgress(rules.size.coerceAtMost(MAX_SEGMENTS), progress.coerceIn(0, rules.size), false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
        for (action in actions) {
            builder.addAction(action)
        }
        return builder.build()
    }

    private fun billActionIntent(context: Context, action: String, ruleId: Long): PendingIntent {
        val intent = Intent(context, RecurringBillActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_RULE_ID, ruleId)
        return PendingIntent.getBroadcast(
            context,
            ruleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}