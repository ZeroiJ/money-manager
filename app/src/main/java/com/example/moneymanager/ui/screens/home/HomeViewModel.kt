package com.example.moneymanager.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

enum class HomeScopeFilter { ALL, PERSONAL, HOUSEHOLD }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moneyDao: MoneyDao
) : ViewModel() {

    private val currentMonthKey = FormatUtils.getCurrentMonthKey()
    private val monthRange = FormatUtils.getMonthTimestampRange(currentMonthKey)

    val selectedScopeFilter = MutableStateFlow(HomeScopeFilter.ALL)

    val allCategories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<Long, Category>> = allCategories.map { list ->
        list.associateBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Month transactions
    val monthTransactions: StateFlow<List<Transaction>> = moneyDao.getTransactionsBetween(monthRange.first, monthRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All transactions for recent display
    val allTransactions: StateFlow<List<Transaction>> = moneyDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered recent transactions
    val recentTransactions: StateFlow<List<Transaction>> = combine(allTransactions, selectedScopeFilter) { list, filter ->
        val filtered = when (filter) {
            HomeScopeFilter.ALL -> list
            HomeScopeFilter.PERSONAL -> list.filter { it.scope == TransactionScope.PERSONAL }
            HomeScopeFilter.HOUSEHOLD -> list.filter { it.scope == TransactionScope.HOUSEHOLD }
        }
        filtered.take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's spend calculation
    val todaySpend: StateFlow<Double> = monthTransactions.map { list ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        list.filter { it.date >= todayStart && it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Month spend calculation
    val monthSpend: StateFlow<Double> = monthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Month income calculation
    val monthIncome: StateFlow<Double> = monthTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Personal vs Household month spend
    val personalSpendMonth: StateFlow<Double> = monthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE && it.scope == TransactionScope.PERSONAL }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val householdSpendMonth: StateFlow<Double> = monthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE && it.scope == TransactionScope.HOUSEHOLD }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Budgets for month
    val monthBudgets: StateFlow<List<Budget>> = moneyDao.getBudgetsForMonth(currentMonthKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBudgetLimit: StateFlow<Double> = monthBudgets.map { list ->
        list.sumOf { it.amountLimit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
