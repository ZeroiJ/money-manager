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
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.data.prefs.UserPreferences
import com.example.moneymanager.util.BackupUtils
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

    val categories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringRules: StateFlow<List<RecurringRule>> = moneyDao.getAllRecurringRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val householdMembers: StateFlow<List<HouseholdMember>> = moneyDao.getAllHouseholdMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            val category = Category(
                name = name.trim(),
                icon = icon,
                color = color,
                isDefault = false
            )
            moneyDao.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            moneyDao.deleteCategory(category)
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
        }
    }

    fun deleteRecurringRule(rule: RecurringRule) {
        viewModelScope.launch {
            moneyDao.deleteRecurringRule(rule)
        }
    }

    fun addHouseholdMember(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                moneyDao.insertHouseholdMember(HouseholdMember(name = name.trim()))
            }
        }
    }

    fun deleteHouseholdMember(member: HouseholdMember) {
        viewModelScope.launch {
            moneyDao.deleteHouseholdMember(member)
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
}
