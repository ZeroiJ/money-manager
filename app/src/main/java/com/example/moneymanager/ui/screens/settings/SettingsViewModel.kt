package com.example.moneymanager.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Frequency
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.PendingImport
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.data.prefs.UserPreferences
import com.example.moneymanager.util.BackupUtils
import com.example.moneymanager.util.ErrorBus
import com.example.moneymanager.util.ErrorKind
import com.example.moneymanager.util.SmsInboxReader
import com.example.moneymanager.util.XlsxImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val moneyDao: MoneyDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val useIndianGrouping: StateFlow<Boolean> = userPreferences.useIndianGrouping

    fun setUseIndianGrouping(enabled: Boolean) {
        userPreferences.setUseIndianGrouping(enabled)
    }

    val biometricEnabled: StateFlow<Boolean> = userPreferences.biometricEnabled

    fun toggleBiometric() {
        userPreferences.setBiometricEnabled(!userPreferences.biometricEnabled.value)
    }

    val ambientEnabled: StateFlow<Boolean> = userPreferences.ambientEnabled

    fun setAmbientEnabled(enabled: Boolean) {
        userPreferences.setAmbientEnabled(enabled)
    }

    val hideAmounts: StateFlow<Boolean> = userPreferences.hideAmounts

    fun setHideAmounts(hidden: Boolean) {
        userPreferences.setHideAmounts(hidden)
    }

    val categories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringRules: StateFlow<List<RecurringRule>> = moneyDao.getAllRecurringRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val householdMembers: StateFlow<List<HouseholdMember>> = moneyDao.getAllHouseholdMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            try {
                val category = Category(
                    name = name.trim(),
                    icon = icon,
                    color = color,
                    isDefault = false
                )
                moneyDao.insertCategory(category)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "Settings → Categories", "Couldn't add the category", e)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                moneyDao.deleteCategory(category)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "Settings → Categories", "Couldn't delete the category", e)
            }
        }
    }

    fun addRecurringRule(
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        paymentMode: PaymentMode,
        scope: TransactionScope,
        frequency: Frequency,
        nextDueDate: Long
    ) {
        viewModelScope.launch {
            try {
                val rule = RecurringRule(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    note = note.trim(),
                    paymentMode = paymentMode,
                    scope = scope,
                    frequency = frequency,
                    nextDueDate = nextDueDate
                )
                moneyDao.insertRecurringRule(rule)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "Settings → Recurring", "Couldn't save the rule", e)
            }
        }
    }

    fun deleteRecurringRule(rule: RecurringRule) {
        viewModelScope.launch {
            try {
                moneyDao.deleteRecurringRule(rule)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "Settings → Recurring", "Couldn't delete the rule", e)
            }
        }
    }

    fun addHouseholdMember(name: String) {
        viewModelScope.launch {
            try {
                if (name.isNotBlank()) {
                    moneyDao.insertHouseholdMember(HouseholdMember(name = name.trim()))
                }
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "Settings → Household", "Couldn't add the member", e)
            }
        }
    }

    fun deleteHouseholdMember(member: HouseholdMember) {
        viewModelScope.launch {
            try {
                moneyDao.deleteHouseholdMember(member)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "Settings → Household", "Couldn't delete the member", e)
            }
        }
    }

    suspend fun exportJsonBackup(): String {
        val txs = moneyDao.getAllTransactionsList()
        val cats = moneyDao.getAllCategoriesList()
        val budgets = moneyDao.getAllBudgetsList()
        val recurring = moneyDao.getAllRecurringRulesList()
        val members = moneyDao.getAllHouseholdMembersList()
        return BackupUtils.exportToJson(txs, cats, budgets, recurring, members)
    }

    suspend fun exportCsvBackup(): String {
        val txs = moneyDao.getAllTransactionsList()
        val cats = moneyDao.getAllCategoriesList().associateBy { it.id }
        return BackupUtils.exportToCsv(txs, cats)
    }

    suspend fun importJsonBackup(jsonString: String): Boolean {
        return try {
            val data = BackupUtils.importFromJson(jsonString)
            if (data.categories.isNotEmpty()) {
                moneyDao.insertCategories(data.categories)
            }
            if (data.transactions.isNotEmpty()) {
                moneyDao.insertTransactions(data.transactions)
            }
            for (b in data.budgets) {
                moneyDao.insertBudget(b)
            }
            for (r in data.recurringRules) {
                moneyDao.insertRecurringRule(r)
            }
            for (m in data.householdMembers) {
                moneyDao.insertHouseholdMember(m)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    data class XlsxPreview(
        val rows: List<XlsxImporter.ParsedTransaction>,
        val totalRows: Int,
        val skippedRows: Int
    )

    private val _xlsxPreview = MutableStateFlow<XlsxPreview?>(null)
    val xlsxPreview: StateFlow<XlsxPreview?> = _xlsxPreview.asStateFlow()

    private val _xlsxBusy = MutableStateFlow(false)
    val xlsxBusy: StateFlow<Boolean> = _xlsxBusy.asStateFlow()

    private val _xlsxError = MutableStateFlow<String?>(null)
    val xlsxError: StateFlow<String?> = _xlsxError.asStateFlow()

    fun parseXlsx(context: Context, uri: Uri) {
        viewModelScope.launch {
            _xlsxBusy.value = true
            _xlsxError.value = null
            try {
                val result = withContext(Dispatchers.IO) {
                    XlsxImporter.importFromUri(context, uri)
                }
                _xlsxPreview.value = XlsxPreview(result.rows, result.totalRows, result.skippedRows)
            } catch (e: Throwable) {
                _xlsxError.value = e.message ?: "Unknown parse error"
            } finally {
                _xlsxBusy.value = false
            }
        }
    }

    fun dismissXlsxPreview() {
        _xlsxPreview.value = null
        _xlsxError.value = null
    }

    suspend fun commitImport(): Int {
        val preview = _xlsxPreview.value ?: return 0
        val rows = preview.rows
        if (rows.isEmpty()) return 0

        val existing = moneyDao.getAllCategoriesList()
        val byLowerName = existing.associateBy { it.name.trim().lowercase() }
        val hintToId = mutableMapOf<String, Long>()

        val transactions = rows.map { parsed ->
            val categoryId = if (parsed.categoryHint.isBlank()) {
                0L
            } else {
                val key = parsed.categoryHint.trim().lowercase()
                hintToId.getOrPut(key) {
                    byLowerName[key]?.id ?: moneyDao.insertCategory(
                        Category(
                            name = parsed.categoryHint.trim(),
                            icon = "category",
                            color = 0xFF2563EB,
                            isDefault = false
                        )
                    )
                }
            }
            Transaction(
                amount = parsed.amount,
                type = parsed.type,
                categoryId = categoryId,
                note = parsed.note,
                date = parsed.date,
                paymentMode = parsed.paymentMode,
                scope = parsed.scope
            )
        }

        moneyDao.insertTransactions(transactions)
        _xlsxPreview.value = null
        return transactions.size
    }

    val pendingImports: StateFlow<List<PendingImport>> = moneyDao.getPendingImports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** One-shot row counts per table for Settings → Diagnostics. */
    suspend fun dbCounts(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        listOf(
            "Transactions" to moneyDao.getAllTransactionsList().size,
            "Categories" to moneyDao.getAllCategoriesList().size,
            "Budgets" to moneyDao.getAllBudgetsList().size,
            "Recurring rules" to moneyDao.getAllRecurringRulesList().size,
            "Household members" to moneyDao.getAllHouseholdMembersList().size
        )
    }

    suspend fun scanSmsInbox(context: Context): Int {
        val candidates = withContext(Dispatchers.IO) {
            SmsInboxReader.readRecent(context)
        }
        if (candidates.isEmpty()) return 0
        val rows = candidates.map { c ->
            PendingImport(
                amount = c.amount,
                type = c.type,
                merchant = c.merchant,
                date = c.date,
                paymentMode = PaymentMode.UPI,
                scope = TransactionScope.PERSONAL,
                referenceNo = c.referenceNo,
                sender = c.sender
            )
        }
        return moneyDao.insertPendingImports(rows).size
    }

    fun approvePending(item: PendingImport) {
        viewModelScope.launch {
            try {
                moneyDao.insertTransaction(
                    Transaction(
                        amount = item.amount,
                        type = item.type,
                        categoryId = 0L,
                        note = "${item.merchant} via UPI",
                        date = item.date,
                        paymentMode = item.paymentMode,
                        scope = item.scope
                    )
                )
                moneyDao.deletePendingImport(item)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "SMS review queue", "Couldn't log the payment", e)
            }
        }
    }

    fun discardPending(item: PendingImport) {
        viewModelScope.launch {
            try {
                moneyDao.deletePendingImport(item)
            } catch (e: Exception) {
                ErrorBus.report(ErrorKind.DB_WRITE, "SMS review queue", "Couldn't discard the payment", e)
            }
        }
    }
}
