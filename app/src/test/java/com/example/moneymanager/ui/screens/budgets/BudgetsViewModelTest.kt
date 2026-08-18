package com.example.moneymanager.ui.screens.budgets

import com.example.moneymanager.data.FakeMoneyDao
import com.example.moneymanager.data.model.*
import com.example.moneymanager.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeMoneyDao
    private lateinit var viewModel: BudgetsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeMoneyDao()
        viewModel = BudgetsViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testBudgetProgressCalculation() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.budgetProgressList.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalBudget.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalSpent.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.categories.collect() }

        val currentMonth = FormatUtils.getCurrentMonthKey()
        val range = FormatUtils.getMonthTimestampRange(currentMonth)
        val midMonthDate = (range.first + range.second) / 2

        val cat1 = Category(id = 1, name = "Groceries", icon = "shopping_basket", color = 0, isDefault = true)
        val cat2 = Category(id = 2, name = "Dining Out", icon = "restaurant", color = 0, isDefault = true)
        fakeDao.insertCategories(listOf(cat1, cat2))

        fakeDao.insertBudget(Budget(id = 1, categoryId = 1, month = currentMonth, amountLimit = 5000.0))

        fakeDao.insertTransaction(
            Transaction(
                id = 1,
                amount = 2500.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                note = "Weekly Veg",
                date = midMonthDate,
                paymentMode = PaymentMode.UPI,
                scope = TransactionScope.HOUSEHOLD,
                paidBy = "Me"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5000.0, viewModel.totalBudget.value, 0.001)
        assertEquals(2500.0, viewModel.totalSpent.value, 0.001)

        val progressList = viewModel.budgetProgressList.value
        assertEquals(2, progressList.size)

        val groceryProgress = progressList.find { it.category.id == 1L }
        assertNotNull(groceryProgress)
        assertEquals(5000.0, groceryProgress!!.limit, 0.001)
        assertEquals(2500.0, groceryProgress.spent, 0.001)
        assertEquals(0.5f, groceryProgress.progress, 0.01f)
        assertEquals(2500.0, groceryProgress.remaining, 0.001)
    }

    @Test
    fun testSetAndNavigateBudget() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.budgetProgressList.collect() }

        viewModel.setBudget(categoryId = 2, amount = 3500.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentMonth = FormatUtils.getCurrentMonthKey()
        val budgetInDao = fakeDao.getBudgetForCategoryAndMonth(2, currentMonth)
        assertNotNull(budgetInDao)
        assertEquals(3500.0, budgetInDao!!.amountLimit, 0.001)

        viewModel.navigateMonth(1)
        testDispatcher.scheduler.advanceUntilIdle()
        // New month selected
        assertEquals(false, viewModel.selectedMonth.value == currentMonth)
    }
}
