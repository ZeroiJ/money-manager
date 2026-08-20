package com.example.moneymanager.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.example.moneymanager.MainActivity
import com.example.moneymanager.data.db.AppDatabase
import com.example.moneymanager.theme.ChromaBlack
import com.example.moneymanager.theme.ChromaOrange
import com.example.moneymanager.theme.ChromaRed
import com.example.moneymanager.theme.ChromaStone200
import com.example.moneymanager.theme.ChromaStone50
import com.example.moneymanager.theme.ChromaStone600
import com.example.moneymanager.theme.ChromaWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MoneyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = withContext(Dispatchers.IO) {
            Room.databaseBuilder(context, AppDatabase::class.java, "money_manager.db")
                .fallbackToDestructiveMigration()
                .build()
        }
        val dao = db.moneyDao()

        val todaySpend = withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val todayEnd = cal.timeInMillis
            dao.getTodaySpend(todayStart, todayEnd) ?: 0.0
        }

        val monthData = withContext(Dispatchers.IO) {
            val calMonth = Calendar.getInstance()
            calMonth.set(Calendar.DAY_OF_MONTH, 1)
            calMonth.set(Calendar.HOUR_OF_DAY, 0); calMonth.set(Calendar.MINUTE, 0)
            calMonth.set(Calendar.SECOND, 0); calMonth.set(Calendar.MILLISECOND, 0)
            val monthStart = calMonth.timeInMillis
            calMonth.add(Calendar.MONTH, 1)
            val monthEnd = calMonth.timeInMillis
            val monthSpend = dao.getMonthSpend(monthStart, monthEnd) ?: 0.0
            val monthStr = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            val totalBudget = dao.getTotalBudgetForMonth(monthStr) ?: 0.0
            Pair(monthSpend, totalBudget)
        }

        val monthSpend = monthData.first
        val totalBudget = monthData.second
        val budgetPercent = if (totalBudget > 0) ((monthSpend / totalBudget) * 100).toInt().coerceIn(0, 999) else 0

        provideContent {
            GlanceTheme {
                WidgetContent(todaySpend, monthSpend, totalBudget, budgetPercent)
            }
        }
    }

    @Composable
    private fun WidgetContent(
        todaySpend: Double,
        monthSpend: Double,
        totalBudget: Double,
        budgetPercent: Int
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ChromaStone50)
                .padding(12.dp)
        ) {
            // Header row: title + add button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MONEY_MGR",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ColorProvider(ChromaBlack)
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .height(28.dp)
                        .width(28.dp)
                        .background(ChromaBlack)
                        .padding(0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            color = ColorProvider(ChromaWhite),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(0.dp)
                    )
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // Today spend
            Text(
                text = "TODAY",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ColorProvider(ChromaStone600)
                )
            )
            Text(
                text = "\u20B9${formatWidgetAmount(todaySpend)}",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ColorProvider(ChromaBlack)
                )
            )

            Spacer(GlanceModifier.height(6.dp))

            // Budget bar
            if (totalBudget > 0) {
                Text(
                    text = "BUDGET: $budgetPercent%",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(ChromaBlack)
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Box(
                    modifier = GlanceModifier
                        .height(6.dp)
                        .fillMaxWidth()
                        .background(ChromaStone200)
                ) {}
                Spacer(GlanceModifier.height(2.dp))
                val barWidth = (budgetPercent.coerceAtMost(100) * 2).dp
                val barColor = if (budgetPercent > 100) ChromaRed else ChromaOrange
                Box(
                    modifier = GlanceModifier
                        .height(6.dp)
                        .width(barWidth)
                        .background(barColor)
                ) {}
                Text(
                    text = "\u20B9${formatWidgetAmount(monthSpend)} of \u20B9${formatWidgetAmount(totalBudget)}",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ColorProvider(ChromaStone600)
                    )
                )
            }
        }
    }

    private fun formatWidgetAmount(amount: Double): String {
        if (amount == 0.0) return "0"
        val parts = amount.toString().split(".")
        val intPart = parts[0].toLong()
        val decPart = if (parts.size > 1 && parts[1].isNotEmpty()) ".${parts[1].take(2)}" else ""
        val str = intPart.toString()
        val last3 = str.takeLast(3)
        val remaining = str.dropLast(3)
        val formatted = if (remaining.isNotEmpty()) {
            remaining.chunked(2).joinToString(",") + "," + last3
        } else last3
        return formatted + decPart
    }
}
