package com.example.moneymanager.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.moneymanager.data.model.TransactionType

data class SmsCandidate(
    val amount: Double,
    val merchant: String,
    val type: TransactionType,
    val date: Long,
    val referenceNo: String?,
    val sender: String
)

object SmsInboxReader {

    fun readRecent(context: Context, daysBack: Int = 30, limit: Int = 200): List<SmsCandidate> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        val since = System.currentTimeMillis() - daysBack * 24L * 60L * 60L * 1000L
        val out = mutableListOf<SmsCandidate>()
        try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                "${Telephony.Sms.DATE} > ?",
                arrayOf(since.toString()),
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                while (cursor.moveToNext() && out.size < limit) {
                    val sender = cursor.getString(addressIdx).orEmpty()
                    val body = cursor.getString(bodyIdx).orEmpty()
                    if (!UpiSmsParser.isTransactionalSms(body)) continue
                    val parsed = UpiSmsParser.parse(body) ?: continue
                    out.add(
                        SmsCandidate(
                            amount = parsed.amount,
                            merchant = parsed.merchant,
                            type = parsed.type,
                            date = cursor.getLong(dateIdx),
                            referenceNo = parsed.referenceNo,
                            sender = sender
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        }
        return out
    }
}
