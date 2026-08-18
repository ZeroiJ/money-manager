package com.example.moneymanager.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategoryBudgetProgress(
    val category: Category,
    val budget: Budget?,
    val spent: Double
) {
    val limit: Double get() = budget?.amountLimit ?: 0.0
    val progress: Float get() = if (limit > 0) (spent / limit).toFloat() else 0f
    val isOverBudget: Boolean get() = limit > 0 && spent > limit
    val remaining: Double get() = limit - spent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val moneyDao: MoneyDao
) : ViewModel() {

    val selectedMonth = MutableStateFlow(FormatUtils.getCurrentMonthKey())

    val categories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentMonthBudgets: Flow<List<Budget>> = selectedMonth.flatMapLatest { month ->
        moneyDao.getBudgetsForMonth(month)
    }

    private val currentMonthTransactions: Flow<List<Transaction>> = selectedMonth.flatMapLatest { month ->
        val range = FormatUtils.getMonthTimestampRange(month)
        moneyDao.getTransactionsBetween(range.first, range.second)
    }

    val budgetProgressList: StateFlow<List<CategoryBudgetProgress>> = combine(
        categories,
        currentMonthBudgets,
        currentMonthTransactions
    ) { cats, budgets, txs ->
        val budgetMap = budgets.associateBy { it.categoryId }
        val spendMap = txs.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        cats.map { cat ->
            val b = budgetMap[cat.id]
            val s = spendMap[cat.id] ?: 0.0
            CategoryBudgetProgress(category = cat, budget = b, spent = s)
        }.sortedWith(compareByDescending<CategoryBudgetProgress> { it.budget != null }
            .thenByDescending { it.spent })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBudget: StateFlow<Double> = budgetProgressList.map { list ->
        list.mapNotNull { it.budget }.sumOf { it.amountLimit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSpent: StateFlow<Double> = budgetProgressList.map { list ->
        list.filter { it.budget != null }.sumOf { it.spent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun navigateMonth(delta: Int) {
        val current = selectedMonth.value
        val parts = current.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: 8) - 1

        val cal = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, delta)
        }
        selectedMonth.value = String.format(java.util.Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun setBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val month = selectedMonth.value
            val existing = moneyDao.getBudgetForCategoryAndMonth(categoryId, month)
            val budget = existing?.copy(amountLimit = amount) ?: Budget(categoryId = categoryId, month = month, amountLimit = amount)
            moneyDao.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            moneyDao.deleteBudget(budget)
        }
    }
}
