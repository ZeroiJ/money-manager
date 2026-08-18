package com.example.moneymanager.ui.screens.home

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
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeMoneyDao
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeMoneyDao()
        viewModel = HomeViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testHomeMetrics_TodayAndMonthSpend() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.todaySpend.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.monthSpend.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.monthIncome.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.personalSpendMonth.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.householdSpendMonth.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.recentTransactions.collect() }

        val now = System.currentTimeMillis()
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }

        fakeDao.insertTransactions(
            listOf(
                Transaction(
                    id = 1,
                    amount = 200.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    note = "Tea Today",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.PERSONAL,
                    paidBy = null
                ),
                Transaction(
                    id = 2,
                    amount = 800.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 2,
                    note = "Groceries 2 days ago",
                    date = calYesterday.timeInMillis,
                    paymentMode = PaymentMode.CARD,
                    scope = TransactionScope.HOUSEHOLD,
                    paidBy = "Me"
                ),
                Transaction(
                    id = 3,
                    amount = 40000.0,
                    type = TransactionType.INCOME,
                    categoryId = 1,
                    note = "Income",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.PERSONAL,
                    paidBy = null
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(200.0, viewModel.todaySpend.value, 0.001)
        assertEquals(1000.0, viewModel.monthSpend.value, 0.001)
        assertEquals(40000.0, viewModel.monthIncome.value, 0.001)
        assertEquals(200.0, viewModel.personalSpendMonth.value, 0.001)
        assertEquals(800.0, viewModel.householdSpendMonth.value, 0.001)
        assertEquals(3, viewModel.recentTransactions.value.size)
    }

    @Test
    fun testScopeFiltering() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.recentTransactions.collect() }

        val now = System.currentTimeMillis()
        fakeDao.insertTransactions(
            listOf(
                Transaction(
                    id = 1,
                    amount = 100.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    note = "Personal item",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.PERSONAL,
                    paidBy = null
                ),
                Transaction(
                    id = 2,
                    amount = 500.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    note = "Household item",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.HOUSEHOLD,
                    paidBy = "Me"
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedScopeFilter.value = HomeScopeFilter.PERSONAL
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.recentTransactions.value.size)
        assertEquals("Personal item", viewModel.recentTransactions.value.first().note)

        viewModel.selectedScopeFilter.value = HomeScopeFilter.HOUSEHOLD
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.recentTransactions.value.size)
        assertEquals("Household item", viewModel.recentTransactions.value.first().note)

        viewModel.selectedScopeFilter.value = HomeScopeFilter.ALL
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.recentTransactions.value.size)
    }
}
