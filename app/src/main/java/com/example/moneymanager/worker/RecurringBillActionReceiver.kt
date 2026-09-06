package com.example.moneymanager.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.moneymanager.util.LiveUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecurringBillActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ruleId = intent.getLongExtra(LiveUpdateHelper.EXTRA_RULE_ID, -1L)
        if (ruleId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = LiveUpdateHelper.buildDatabase(context)
                val dao = db.moneyDao()
                val rule = dao.getRecurringRuleById(ruleId)
                if (rule != null) {
                    when (intent.action) {
                        LiveUpdateHelper.ACTION_LOG_BILL -> {
                            dao.insertTransaction(rule.toTransaction(System.currentTimeMillis()))
                            dao.updateRecurringRule(rule.copy(nextDueDate = rule.advanceNextDue()))
                        }
                        LiveUpdateHelper.ACTION_SNOOZE_BILL -> {
                            val snoozed = rule.nextDueDate + LiveUpdateHelper.DAY_MILLIS
                            dao.updateRecurringRuleNextDue(ruleId, snoozed)
                        }
                    }
                }
                db.close()

                val now = System.currentTimeMillis()
                val futureDate = now + 7L * 24 * 60 * 60 * 1000
                val refreshDb = LiveUpdateHelper.buildDatabase(context)
                val upcoming = refreshDb.moneyDao().getUpcomingRecurringRules(now, futureDate)
                refreshDb.close()
                LiveUpdateHelper.postUpcomingBillsLiveUpdate(context, upcoming, now)
            } finally {
                pendingResult.finish()
            }
        }
    }
}