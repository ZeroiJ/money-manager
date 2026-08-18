package com.example.moneymanager.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class DateFilter { ALL, THIS_MONTH, LAST_MONTH }

data class DayGroupedTransactions(
    val dateLabel: String,
    val dayTimestamp: Long,
    val dayTotalSpend: Double,
    val transactions: List<Transaction>
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val moneyDao: MoneyDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val scopeFilter = MutableStateFlow<TransactionScope?>(null)
    val paymentModeFilter = MutableStateFlow<PaymentMode?>(null)
    val typeFilter = MutableStateFlow<TransactionType?>(null)
    val dateFilter = MutableStateFlow(DateFilter.ALL)

    val categories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<Long, Category>> = categories.map { list ->
        list.associateBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val allTransactions = moneyDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("UNCHECKED_CAST")
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        categoriesMap,
        searchQuery,
        scopeFilter,
        paymentModeFilter,
        typeFilter,
        dateFilter
    ) { params ->
        val txs = params[0] as List<Transaction>
        val catMap = params[1] as Map<Long, Category>
        val query = (params[2] as String).trim()
        val scope = params[3] as TransactionScope?
        val mode = params[4] as PaymentMode?
        val type = params[5] as TransactionType?
        val dateFilt = params[6] as DateFilter

        val currentMonthRange = FormatUtils.getMonthTimestampRange(FormatUtils.getCurrentMonthKey())
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val lastMonthKey = String.format(java.util.Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val lastMonthRange = FormatUtils.getMonthTimestampRange(lastMonthKey)

        txs.filter { tx ->
            // Search filter
            val matchesQuery = if (query.isBlank()) true else {
                val catName = catMap[tx.categoryId]?.name ?: ""
                tx.note.contains(query, ignoreCase = true) ||
                        catName.contains(query, ignoreCase = true) ||
                        tx.amount.toString().contains(query) ||
                        tx.paymentMode.name.contains(query, ignoreCase = true) ||
                        (tx.paidBy != null && tx.paidBy.contains(query, ignoreCase = true))
            }

            // Scope filter
            val matchesScope = scope == null || tx.scope == scope

            // Payment mode filter
            val matchesMode = mode == null || tx.paymentMode == mode

            // Type filter
            val matchesType = type == null || tx.type == type

            // Date filter
            val matchesDate = when (dateFilt) {
                DateFilter.ALL -> true
                DateFilter.THIS_MONTH -> tx.date in currentMonthRange.first..currentMonthRange.second
                DateFilter.LAST_MONTH -> tx.date in lastMonthRange.first..lastMonthRange.second
            }

            matchesQuery && matchesScope && matchesMode && matchesType && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedTransactions: StateFlow<List<DayGroupedTransactions>> = filteredTransactions.map { txList ->
        val groups = txList.groupBy { tx ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = tx.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }

        groups.map { (dayTimestamp, list) ->
            val daySpend = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            DayGroupedTransactions(
                dateLabel = FormatUtils.formatDate(dayTimestamp),
                dayTimestamp = dayTimestamp,
                dayTotalSpend = daySpend,
                transactions = list
            )
        }.sortedByDescending { it.dayTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFilteredSpend: StateFlow<Double> = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            moneyDao.deleteTransaction(transaction)
        }
    }
}
