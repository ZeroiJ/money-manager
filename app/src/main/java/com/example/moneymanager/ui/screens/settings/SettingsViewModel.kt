package com.example.moneymanager.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Frequency
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.data.prefs.UserPreferences
import com.example.moneymanager.util.BackupUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
}
