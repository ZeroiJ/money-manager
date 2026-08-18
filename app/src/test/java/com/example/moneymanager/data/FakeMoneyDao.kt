package com.example.moneymanager.data

import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMoneyDao : MoneyDao {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val categories = MutableStateFlow<List<Category>>(emptyList())
    val budgets = MutableStateFlow<List<Budget>>(emptyList())
    val recurringRules = MutableStateFlow<List<RecurringRule>>(emptyList())
    val householdMembers = MutableStateFlow<List<HouseholdMember>>(emptyList())

    private var nextTxId = 1L
    private var nextCatId = 1L
    private var nextBudgetId = 1L
    private var nextRuleId = 1L
    private var nextMemberId = 1L

    override suspend fun insertTransaction(transaction: Transaction): Long {
        val id = if (transaction.id > 0) transaction.id else nextTxId++
        val newTx = transaction.copy(id = id)
        transactions.value = transactions.value.filterNot { it.id == id } + newTx
        return id
    }

    override suspend fun insertTransactions(transactionsList: List<Transaction>): List<Long> {
        return transactionsList.map { insertTransaction(it) }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactions.value = transactions.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactions.value = transactions.value.filterNot { it.id == transaction.id }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactions.value.find { it.id == id }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = transactions

    override suspend fun getAllTransactionsList(): List<Transaction> = transactions.value

    override fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactions.map { list ->
            list.filter { it.date in startDate..endDate }
        }
    }

    override suspend fun deleteAllTransactions() {
        transactions.value = emptyList()
    }

    override suspend fun insertCategory(category: Category): Long {
        val id = if (category.id > 0) category.id else nextCatId++
        val newCat = category.copy(id = id)
        categories.value = categories.value.filterNot { it.id == id } + newCat
        return id
    }

    override suspend fun insertCategories(categoriesList: List<Category>): List<Long> {
        return categoriesList.map { insertCategory(it) }
    }

    override suspend fun updateCategory(category: Category) {
        categories.value = categories.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(category: Category) {
        categories.value = categories.value.filterNot { it.id == category.id }
    }

    override fun getAllCategories(): Flow<List<Category>> = categories

    override suspend fun getAllCategoriesList(): List<Category> = categories.value

    override suspend fun getCategoryById(id: Long): Category? {
        return categories.value.find { it.id == id }
    }

    override suspend fun insertBudget(budget: Budget): Long {
        val id = if (budget.id > 0) budget.id else nextBudgetId++
        val newBudget = budget.copy(id = id)
        budgets.value = budgets.value.filterNot { it.id == id } + newBudget
        return id
    }

    override suspend fun updateBudget(budget: Budget) {
        budgets.value = budgets.value.map { if (it.id == budget.id) budget else it }
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgets.value = budgets.value.filterNot { it.id == budget.id }
    }

    override fun getBudgetsForMonth(month: String): Flow<List<Budget>> {
        return budgets.map { list -> list.filter { it.month == month } }
    }

    override suspend fun getAllBudgetsList(): List<Budget> = budgets.value

    override suspend fun getBudgetForCategoryAndMonth(categoryId: Long, month: String): Budget? {
        return budgets.value.find { it.categoryId == categoryId && it.month == month }
    }

    override suspend fun insertRecurringRule(rule: RecurringRule): Long {
        val id = if (rule.id > 0) rule.id else nextRuleId++
        val newRule = rule.copy(id = id)
        recurringRules.value = recurringRules.value.filterNot { it.id == id } + newRule
        return id
    }

    override suspend fun updateRecurringRule(rule: RecurringRule) {
        recurringRules.value = recurringRules.value.map { if (it.id == rule.id) rule else it }
    }

    override suspend fun deleteRecurringRule(rule: RecurringRule) {
        recurringRules.value = recurringRules.value.filterNot { it.id == rule.id }
    }

    override fun getAllRecurringRules(): Flow<List<RecurringRule>> = recurringRules

    override suspend fun getDueRecurringRules(now: Long): List<RecurringRule> {
        return recurringRules.value.filter { it.nextDueDate <= now }
    }

    override suspend fun getAllRecurringRulesList(): List<RecurringRule> = recurringRules.value

    override suspend fun insertHouseholdMember(member: HouseholdMember): Long {
        val id = if (member.id > 0) member.id else nextMemberId++
        val newMember = member.copy(id = id)
        householdMembers.value = householdMembers.value.filterNot { it.id == id } + newMember
        return id
    }

    override suspend fun updateHouseholdMember(member: HouseholdMember) {
        householdMembers.value = householdMembers.value.map { if (it.id == member.id) member else it }
    }

    override suspend fun deleteHouseholdMember(member: HouseholdMember) {
        householdMembers.value = householdMembers.value.filterNot { it.id == member.id }
    }

    override fun getAllHouseholdMembers(): Flow<List<HouseholdMember>> = householdMembers

    override suspend fun getAllHouseholdMembersList(): List<HouseholdMember> = householdMembers.value
}
