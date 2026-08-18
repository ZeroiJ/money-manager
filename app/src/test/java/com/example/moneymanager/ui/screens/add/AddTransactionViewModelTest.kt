package com.example.moneymanager.ui.screens.add

import com.example.moneymanager.data.FakeMoneyDao
import com.example.moneymanager.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeMoneyDao
    private lateinit var viewModel: AddTransactionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeMoneyDao()
        viewModel = AddTransactionViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testNumpadInput_DigitsAndDel() {
        assertEquals("0", viewModel.amountInput.value)

        viewModel.onNumpadClick("5")
        assertEquals("5", viewModel.amountInput.value)

        viewModel.onNumpadClick("0")
        assertEquals("50", viewModel.amountInput.value)

        viewModel.onNumpadClick("DEL")
        assertEquals("5", viewModel.amountInput.value)

        viewModel.onNumpadClick("DEL")
        assertEquals("0", viewModel.amountInput.value)
    }

    @Test
    fun testNumpadInput_MathExpressionEvaluation() {
        viewModel.onNumpadClick("1")
        viewModel.onNumpadClick("2")
        viewModel.onNumpadClick("0")
        assertEquals("120", viewModel.amountInput.value)

        viewModel.onNumpadClick("+")
        assertEquals("120+", viewModel.amountInput.value)

        viewModel.onNumpadClick("8")
        viewModel.onNumpadClick("0")
        assertEquals("120+80", viewModel.amountInput.value)

        viewModel.onNumpadClick("=")
        assertEquals("200", viewModel.amountInput.value)
    }

    @Test
    fun testQuickAmountIncrements() {
        viewModel.onNumpadClick("+50")
        assertEquals("50", viewModel.amountInput.value)

        viewModel.onNumpadClick("+100")
        assertEquals("150", viewModel.amountInput.value)

        viewModel.onNumpadClick("+500")
        assertEquals("650", viewModel.amountInput.value)
    }

    @Test
    fun testSaveTransaction_InsertsIntoDao() = runTest {
        fakeDao.insertCategory(Category(id = 1, name = "Chai", icon = "local_cafe", color = 0, isDefault = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.amountInput.value = "150"
        viewModel.selectedCategoryId.value = 1
        viewModel.noteInput.value = "Tea & Samosa"
        viewModel.paymentMode.value = PaymentMode.UPI
        viewModel.transactionScope.value = TransactionScope.PERSONAL

        var saved = false
        viewModel.saveTransaction { saved = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        assertEquals(1, fakeDao.transactions.value.size)
        val tx = fakeDao.transactions.value.first()
        assertEquals(150.0, tx.amount, 0.001)
        assertEquals("Tea & Samosa", tx.note)
        assertEquals(PaymentMode.UPI, tx.paymentMode)
        assertEquals(TransactionScope.PERSONAL, tx.scope)
    }

    @Test
    fun testEditTransaction_UpdatesDao() = runTest {
        val existingTxId = fakeDao.insertTransaction(
            Transaction(
                id = 10,
                amount = 500.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                note = "Groceries",
                date = 1000L,
                paymentMode = PaymentMode.CASH,
                scope = TransactionScope.HOUSEHOLD,
                paidBy = "Me"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadTransaction(existingTxId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("500", viewModel.amountInput.value)
        assertEquals("Groceries", viewModel.noteInput.value)
        assertEquals(TransactionScope.HOUSEHOLD, viewModel.transactionScope.value)

        viewModel.amountInput.value = "750"
        viewModel.noteInput.value = "Groceries & Milk"

        var updated = false
        viewModel.saveTransaction { updated = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(updated)
        assertEquals(1, fakeDao.transactions.value.size)
        val updatedTx = fakeDao.transactions.value.first()
        assertEquals(750.0, updatedTx.amount, 0.001)
        assertEquals("Groceries & Milk", updatedTx.note)
    }
}
