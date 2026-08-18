package com.example.moneymanager.ui.screens.reports

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeMoneyDao
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeMoneyDao()
        viewModel = ReportsViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testReportsAggregations_TotalsAndPercentages() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalExpense.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.totalIncome.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.personalExpense.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.householdExpense.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.categoryReports.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.allCategories.collect() }

        val now = System.currentTimeMillis()
        val cat1 = Category(id = 1, name = "Food", icon = "restaurant", color = 0, isDefault = true)
        val cat2 = Category(id = 2, name = "Rent", icon = "home", color = 0, isDefault = true)
        fakeDao.insertCategories(listOf(cat1, cat2))

        fakeDao.insertTransactions(
            listOf(
                Transaction(
                    id = 1,
                    amount = 1000.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    note = "Lunch",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.PERSONAL,
                    paidBy = null
                ),
                Transaction(
                    id = 2,
                    amount = 3000.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 2,
                    note = "House Rent",
                    date = now,
                    paymentMode = PaymentMode.CARD,
                    scope = TransactionScope.HOUSEHOLD,
                    paidBy = "Me"
                ),
                Transaction(
                    id = 3,
                    amount = 50000.0,
                    type = TransactionType.INCOME,
                    categoryId = 1,
                    note = "Salary",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.PERSONAL,
                    paidBy = null
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(4000.0, viewModel.totalExpense.value, 0.001)
        assertEquals(50000.0, viewModel.totalIncome.value, 0.001)
        assertEquals(1000.0, viewModel.personalExpense.value, 0.001)
        assertEquals(3000.0, viewModel.householdExpense.value, 0.001)

        val catReports = viewModel.categoryReports.value
        assertEquals(2, catReports.size)
        assertEquals("Rent", catReports.first().category.name)
        assertEquals(3000.0, catReports.first().amount, 0.001)
        assertEquals(0.75f, catReports.first().percentage, 0.01f)
    }

    @Test
    fun testHouseholdSettlement_FairShareBalances() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.householdSettlements.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.householdMembers.collect() }

        val now = System.currentTimeMillis()
        fakeDao.insertHouseholdMember(HouseholdMember(id = 1, name = "Me"))
        fakeDao.insertHouseholdMember(HouseholdMember(id = 2, name = "Roommate"))

        fakeDao.insertTransactions(
            listOf(
                Transaction(
                    id = 1,
                    amount = 8000.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    note = "Groceries",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.HOUSEHOLD,
                    paidBy = "Me"
                ),
                Transaction(
                    id = 2,
                    amount = 2000.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    note = "Electricity",
                    date = now,
                    paymentMode = PaymentMode.UPI,
                    scope = TransactionScope.HOUSEHOLD,
                    paidBy = "Roommate"
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val settlements = viewModel.householdSettlements.value
        assertEquals(2, settlements.size)

        val meSettlement = settlements.find { it.memberName == "Me" }!!
        val roommateSettlement = settlements.find { it.memberName == "Roommate" }!!

        // Total = 10,000. Fair share = 5,000 each.
        // Me paid 8,000 -> net balance = +3,000 (gets back)
        // Roommate paid 2,000 -> net balance = -3,000 (owes)
        assertEquals(8000.0, meSettlement.totalPaid, 0.001)
        assertEquals(5000.0, meSettlement.fairShare, 0.001)
        assertEquals(3000.0, meSettlement.netBalance, 0.001)

        assertEquals(2000.0, roommateSettlement.totalPaid, 0.001)
        assertEquals(5000.0, roommateSettlement.fairShare, 0.001)
        assertEquals(-3000.0, roommateSettlement.netBalance, 0.001)
    }

    @Test
    fun testMonthlyCalendarHeatmap_GeneratesDays() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.monthlyCalendarHeatmap.collect() }
        testDispatcher.scheduler.advanceUntilIdle()
        val heatmap = viewModel.monthlyCalendarHeatmap.value
        assertTrue(heatmap.size in 28..31)
    }
}
