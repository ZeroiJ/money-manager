package com.example.moneymanager.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.util.ErrorBus
import com.example.moneymanager.util.ErrorKind
import com.example.moneymanager.util.ReceiptStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
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

    // Swipe-right delete on the Home ledger rows (v1.21). Room's invalidation
    // re-emits every derived flow (history, month spend) after the row vanishes.
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transaction.receiptUri?.let { ReceiptStorage.deleteReceipt(appContext, it) }
                moneyDao.deleteTransaction(transaction)
            } catch (e: Exception) {
                ErrorBus.report(
                    ErrorKind.DB_WRITE, "Home",
                    "Couldn't delete the transaction", e
                )
            }
        }
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

    // Full month-grouped history for the Home ledger (uncapped, date-DESC).
    val allTransactions: StateFlow<List<Transaction>> = transactionsSource
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