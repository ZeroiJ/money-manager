package com.example.moneymanager.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

enum class ReportPeriod { THIS_MONTH, LAST_MONTH, ALL_TIME }

data class CategorySpendReport(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class PaymentModeSpendReport(
    val mode: PaymentMode,
    val amount: Double,
    val percentage: Float
)

data class DailySpendPoint(
    val dayNumber: Int,
    val amount: Double
)

data class CalendarDayHeatmap(
    val dayNumber: Int,
    val dayOfWeek: Int,
    val dateMillis: Long,
    val spendAmount: Double,
    val intensityLevel: Int // 0: None, 1: Low, 2: Medium, 3: High, 4: Max
)

data class MemberSettlement(
    val memberName: String,
    val totalPaid: Double,
    val fairShare: Double,
    val netBalance: Double // Positive: will receive, Negative: owes
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val moneyDao: MoneyDao
) : ViewModel() {

    val selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)

    val allCategories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val householdMembers: StateFlow<List<HouseholdMember>> = moneyDao.getAllHouseholdMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allTransactions = moneyDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(allTransactions, selectedPeriod) { txs, period ->
        val currentMonthRange = FormatUtils.getMonthTimestampRange(FormatUtils.getCurrentMonthKey())
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val lastMonthKey = String.format(java.util.Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val lastMonthRange = FormatUtils.getMonthTimestampRange(lastMonthKey)

        when (period) {
            ReportPeriod.THIS_MONTH -> txs.filter { it.date in currentMonthRange.first..currentMonthRange.second }
            ReportPeriod.LAST_MONTH -> txs.filter { it.date in lastMonthRange.first..lastMonthRange.second }
            ReportPeriod.ALL_TIME -> txs
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double> = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val personalExpense: StateFlow<Double> = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE && it.scope == TransactionScope.PERSONAL }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val householdExpense: StateFlow<Double> = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE && it.scope == TransactionScope.HOUSEHOLD }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryReports: StateFlow<List<CategorySpendReport>> = combine(
        filteredTransactions,
        allCategories,
        totalExpense
    ) { txs, cats, total ->
        if (total <= 0.0) return@combine emptyList<CategorySpendReport>()

        val expenses = txs.filter { it.type == TransactionType.EXPENSE }
        val spendMap = expenses.groupBy { it.categoryId }

        cats.mapNotNull { cat ->
            val catTxs = spendMap[cat.id] ?: return@mapNotNull null
            val sum = catTxs.sumOf { it.amount }
            if (sum > 0) {
                CategorySpendReport(
                    category = cat,
                    amount = sum,
                    percentage = (sum / total).toFloat(),
                    transactionCount = catTxs.size
                )
            } else null
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentModeReports: StateFlow<List<PaymentModeSpendReport>> = combine(
        filteredTransactions,
        totalExpense
    ) { txs, total ->
        if (total <= 0.0) return@combine emptyList<PaymentModeSpendReport>()
        val expenses = txs.filter { it.type == TransactionType.EXPENSE }

        PaymentMode.entries.map { mode ->
            val sum = expenses.filter { it.paymentMode == mode }.sumOf { it.amount }
            PaymentModeSpendReport(
                mode = mode,
                amount = sum,
                percentage = if (total > 0) (sum / total).toFloat() else 0f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySpendTrend: StateFlow<List<DailySpendPoint>> = filteredTransactions.map { txs ->
        val expenses = txs.filter { it.type == TransactionType.EXPENSE }
        val dayMap = mutableMapOf<Int, Double>()

        for (tx in expenses) {
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            dayMap[day] = (dayMap[day] ?: 0.0) + tx.amount
        }

        (1..31).map { day ->
            DailySpendPoint(dayNumber = day, amount = dayMap[day] ?: 0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyCalendarHeatmap: StateFlow<List<CalendarDayHeatmap>> = combine(
        filteredTransactions,
        selectedPeriod
    ) { txs, period ->
        val cal = Calendar.getInstance()
        if (period == ReportPeriod.LAST_MONTH) {
            cal.add(Calendar.MONTH, -1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        val expenses = txs.filter { it.type == TransactionType.EXPENSE }
        val daySpendMap = mutableMapOf<Int, Double>()
        for (tx in expenses) {
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            if (txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month) {
                val d = txCal.get(Calendar.DAY_OF_MONTH)
                daySpendMap[d] = (daySpendMap[d] ?: 0.0) + tx.amount
            }
        }

        val maxSpend = daySpendMap.values.maxOrNull() ?: 1.0

        (1..daysInMonth).map { d ->
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val spend = daySpendMap[d] ?: 0.0
            val intensity = when {
                spend <= 0.0 -> 0
                spend <= maxSpend * 0.25 -> 1
                spend <= maxSpend * 0.50 -> 2
                spend <= maxSpend * 0.75 -> 3
                else -> 4
            }

            CalendarDayHeatmap(
                dayNumber = d,
                dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK),
                dateMillis = dayCal.timeInMillis,
                spendAmount = spend,
                intensityLevel = intensity
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val householdSettlements: StateFlow<List<MemberSettlement>> = combine(
        filteredTransactions,
        householdMembers
    ) { txs, members ->
        if (members.isEmpty()) return@combine emptyList<MemberSettlement>()

        val householdTxs = txs.filter { it.type == TransactionType.EXPENSE && it.scope == TransactionScope.HOUSEHOLD }
        val totalHousehold = householdTxs.sumOf { it.amount }
        if (totalHousehold <= 0.0) return@combine emptyList<MemberSettlement>()

        val fairSharePerMember = totalHousehold / members.size
        val paidMap = householdTxs.groupBy { it.paidBy ?: "Me" }

        members.map { member ->
            val paid = paidMap[member.name]?.sumOf { it.amount } ?: 0.0
            val net = paid - fairSharePerMember
            MemberSettlement(
                memberName = member.name,
                totalPaid = paid,
                fairShare = fairSharePerMember,
                netBalance = net
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
