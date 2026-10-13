package com.example.moneymanager.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.util.ErrorBus
import com.example.moneymanager.util.ErrorKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moneyDao: MoneyDao
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<Long, Category>> = allCategories.map { list ->
        list.associateBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Feed health: a failed DB read surfaces as loadError (card + retry)
    // instead of a forever-empty feed. Retry re-subscribes to the DAO.
    private val refreshTick = MutableStateFlow(0)
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    fun retryLoad() {
        _loadError.value = null
        refreshTick.value++
    }

    private val transactionsSource: Flow<List<Transaction>> =
        refreshTick.flatMapLatest {
            moneyDao.getAllTransactions()
                .catch { e ->
                    ErrorBus.report(ErrorKind.DB_READ, "Home feed", "Couldn't load transactions", e)
                    _loadError.value = e.message ?: "Couldn't load transactions"
                    emit(emptyList())
                }
        }

    val recentTransactions: StateFlow<List<Transaction>> = transactionsSource
        .map { list -> list.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Additive glanceable totals for the iOS Home card. Sourced from the UNCAPPED
    // feed so backdated writes and edits beyond the top-10 still retrigger them.
    val todaySpend: StateFlow<Double> = transactionsSource
        .mapLatest {
            val (start, end) = dayRange(System.currentTimeMillis())
            try {
                moneyDao.getTodaySpend(start, end) ?: 0.0
            } catch (_: Exception) {
                0.0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthSpend: StateFlow<Double> = transactionsSource
        .mapLatest {
            val (start, end) = monthRange(System.currentTimeMillis())
            try {
                moneyDao.getMonthSpend(start, end) ?: 0.0
            } catch (_: Exception) {
                0.0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** PRD §4.1: total allocated budget for the current month (0 if none set). */
    val monthBudget: StateFlow<Double> = moneyDao.getBudgetsForMonth(currentMonthKey())
        .map { list -> list.sumOf { it.amountLimit } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * PRD §4.1 overrun module: categories where month spend exceeds their
     * budget cap. Empty list renders no banner (quiet when healthy).
     */
    val overruns: StateFlow<List<Overrun>> = combine(
        moneyDao.getBudgetsForMonth(currentMonthKey()),
        transactionsSource,
        allCategories
    ) { budgets, txs, cats ->
        if (budgets.isEmpty()) return@combine emptyList()
        val (start, end) = monthRange(System.currentTimeMillis())
        val spendByCat = txs.asSequence()
            .filter { it.type == com.example.moneymanager.data.model.TransactionType.EXPENSE && it.date in start..end }
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val nameById = cats.associateBy({ it.id }, { it.name })
        budgets.mapNotNull { b ->
            val spent = spendByCat[b.categoryId] ?: 0.0
            if (b.amountLimit > 0 && spent > b.amountLimit) {
                Overrun(
                    categoryId = b.categoryId,
                    name = nameById[b.categoryId] ?: "Category",
                    overspent = spent - b.amountLimit
                )
            } else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun currentMonthKey(): String {
        val cal = Calendar.getInstance()
        return String.format(
            java.util.Locale.US, "%04d-%02d",
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1
        )
    }

    data class Overrun(val categoryId: Long, val name: String, val overspent: Double)

    companion object {
        fun dayRange(now: Long): Pair<Long, Long> {
            val start = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            return start to end
        }

        fun monthRange(now: Long): Pair<Long, Long> {
            val start = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            return start to end
        }
    }
}