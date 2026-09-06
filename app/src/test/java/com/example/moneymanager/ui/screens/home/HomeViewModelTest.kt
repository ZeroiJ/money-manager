package com.example.moneymanager.ui.screens.home

import com.example.moneymanager.data.FakeMoneyDao
import com.example.moneymanager.data.model.*
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
import org.junit.Assert.assertNull
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

    private fun tx(
        id: Long,
        amount: Double,
        note: String,
        date: Long,
        scope: TransactionScope = TransactionScope.PERSONAL
    ) = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.EXPENSE,
        categoryId = 1,
        note = note,
        date = date,
        paymentMode = PaymentMode.UPI,
        scope = scope,
        paidBy = if (scope == TransactionScope.HOUSEHOLD) "Me" else null
    )

    @Test
    fun testRecentTransactions_ReturnsAllWhenAtMostTen() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.recentTransactions.collect() }

        val now = System.currentTimeMillis()
        fakeDao.insertTransactions(
            listOf(
                tx(1, 200.0, "Tea Today", now),
                tx(2, 800.0, "Groceries", now, TransactionScope.HOUSEHOLD),
                tx(3, 40000.0, "Income-ish", now)
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.recentTransactions.value.size)
        assertEquals("Tea Today", viewModel.recentTransactions.value.first().note)
    }

    @Test
    fun testRecentTransactions_CapsAtTen() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.recentTransactions.collect() }

        val now = System.currentTimeMillis()
        val twelve = (1L..12L).map { id -> tx(id, id * 10.0, "Tx $id", now) }
        fakeDao.insertTransactions(twelve)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(10, viewModel.recentTransactions.value.size)
    }

    @Test
    fun testCategoriesMap_MapsById() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.categoriesMap.collect() }

        fakeDao.insertCategories(
            listOf(
                Category(id = 1, name = "Groceries", icon = "shopping_basket", color = 0),
                Category(id = 2, name = "Dining Out", icon = "restaurant", color = 0)
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Groceries", viewModel.categoriesMap.value[1L]?.name)
        assertEquals("Dining Out", viewModel.categoriesMap.value[2L]?.name)
        assertNull(viewModel.categoriesMap.value[99L])
    }
}