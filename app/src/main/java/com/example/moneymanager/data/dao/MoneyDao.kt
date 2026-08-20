package com.example.moneymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.RecurringRule
import com.example.moneymanager.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyDao {
    // --- Transactions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsList(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end")
    suspend fun getTodaySpend(start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end")
    suspend fun getMonthSpend(start: Long, end: Long): Double?

    @Query("SELECT SUM(amountLimit) FROM budgets WHERE month = :month")
    suspend fun getTotalBudgetForMonth(month: String): Double?

    // --- Categories ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>): List<Long>

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategoriesList(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    // --- Budgets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsList(): List<Budget>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month LIMIT 1")
    suspend fun getBudgetForCategoryAndMonth(categoryId: Long, month: String): Budget?

    // --- Recurring Rules ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRule(rule: RecurringRule): Long

    @Update
    suspend fun updateRecurringRule(rule: RecurringRule)

    @Delete
    suspend fun deleteRecurringRule(rule: RecurringRule)

    @Query("SELECT * FROM recurring_rules ORDER BY nextDueDate ASC")
    fun getAllRecurringRules(): Flow<List<RecurringRule>>

    @Query("SELECT * FROM recurring_rules WHERE nextDueDate <= :now")
    suspend fun getDueRecurringRules(now: Long): List<RecurringRule>

    @Query("SELECT * FROM recurring_rules WHERE nextDueDate > :now AND nextDueDate <= :futureDate ORDER BY nextDueDate ASC")
    suspend fun getUpcomingRecurringRules(now: Long, futureDate: Long): List<RecurringRule>

    @Query("SELECT * FROM recurring_rules")
    suspend fun getAllRecurringRulesList(): List<RecurringRule>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getRecurringRuleById(id: Long): RecurringRule?

    @Query("UPDATE recurring_rules SET nextDueDate = :nextDate WHERE id = :id")
    suspend fun updateRecurringRuleNextDue(id: Long, nextDate: Long)

    // --- Household Members ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholdMember(member: HouseholdMember): Long

    @Update
    suspend fun updateHouseholdMember(member: HouseholdMember)

    @Delete
    suspend fun deleteHouseholdMember(member: HouseholdMember)

    @Query("SELECT * FROM household_members ORDER BY name ASC")
    fun getAllHouseholdMembers(): Flow<List<HouseholdMember>>

    @Query("SELECT * FROM household_members ORDER BY name ASC")
    suspend fun getAllHouseholdMembersList(): List<HouseholdMember>
}
