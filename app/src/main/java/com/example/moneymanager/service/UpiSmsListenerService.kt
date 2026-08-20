package com.example.moneymanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.moneymanager.MainActivity
import com.example.moneymanager.util.UpiSmsParser

class UpiSmsListenerService : NotificationListenerService() {

    private val CHANNEL_ID = "upi_sms_channel"
    private val NOTIFICATION_ID = 2001

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.packageName != "com.android.mms" && sbn.packageName != "com.google.android.apps.messaging") return

        val extras = sbn.notification?.extras ?: return
        val sender = extras.getString(Notification.EXTRA_SUB_TEXT, "") ?: extras.getString(Notification.EXTRA_TITLE, "")
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        if (!UpiSmsParser.isUpiSms(sender, body)) return

        val parsed = UpiSmsParser.parse(body) ?: return
        showSuggestionNotification(parsed.amount, parsed.merchant)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    private fun showSuggestionNotification(amount: Double, merchant: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("preset_category", "Food Delivery")
            putExtra("preset_payment_mode", "UPI")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountStr = "₹${amount.toInt()}"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("UPI detected: $amountStr → $merchant")
            .setContentText("Tap to log this expense")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "UPI SMS Suggestions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Suggests expense entries from UPI SMS"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
