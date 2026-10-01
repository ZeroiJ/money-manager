package com.example.moneymanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.moneymanager.MainActivity
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.PendingImport
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.util.UpiSmsParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UpiSmsListenerService : NotificationListenerService() {

    @Inject
    lateinit var moneyDao: MoneyDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val channelId = "upi_sms_channel"

    private val watchedPackages = setOf(
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.google.android.apps.nbu.paisa.user"
    )

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "UPI SMS Suggestions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Suggests expense entries from UPI SMS"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName !in watchedPackages) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (body.isBlank()) return
        val sender = extras.getString(Notification.EXTRA_SUB_TEXT)
            ?.takeIf { it.isNotBlank() } ?: title.ifBlank { sbn.packageName }

        if (!UpiSmsParser.isTransactionalSms(body)) return
        val parsed = UpiSmsParser.parse(body) ?: return

        scope.launch {
            moneyDao.insertPendingImports(
                listOf(
                    PendingImport(
                        amount = parsed.amount,
                        type = parsed.type,
                        merchant = parsed.merchant,
                        date = System.currentTimeMillis(),
                        paymentMode = PaymentMode.UPI,
                        scope = TransactionScope.PERSONAL,
                        referenceNo = parsed.referenceNo,
                        sender = sender
                    )
                )
            )
            showReviewNotification(parsed.amount, parsed.merchant, parsed.referenceNo)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    private fun showReviewNotification(amount: Double, merchant: String, referenceNo: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, referenceNo.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("UPI detected: ₹${amount.toInt()} → $merchant")
            .setContentText("Added to review queue in Settings")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(referenceNo.hashCode(), notification)
    }
}
