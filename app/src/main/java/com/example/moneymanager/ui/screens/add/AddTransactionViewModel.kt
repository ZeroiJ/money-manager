package com.example.moneymanager.ui.screens.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.dao.MoneyDao
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.HouseholdMember
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.util.ReceiptStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val moneyDao: MoneyDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val editingTransactionId = MutableStateFlow<Long?>(null)

    val categories: StateFlow<List<Category>> = moneyDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val householdMembers: StateFlow<List<HouseholdMember>> = moneyDao.getAllHouseholdMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val amountInput = MutableStateFlow("0")
    val transactionType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedCategoryId = MutableStateFlow<Long?>(null)
    val noteInput = MutableStateFlow("")
    val paymentMode = MutableStateFlow(PaymentMode.UPI)
    val transactionScope = MutableStateFlow(TransactionScope.PERSONAL)
    val selectedPaidBy = MutableStateFlow<String?>("Me")
    val selectedDate = MutableStateFlow(System.currentTimeMillis())
    val receiptUri = MutableStateFlow<String?>(null)

    init {
        // Automatically select the first category once available
        viewModelScope.launch {
            categories.collect { list ->
                if (selectedCategoryId.value == null && list.isNotEmpty()) {
                    selectedCategoryId.value = list.first().id
                }
            }
        }
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val tx = moneyDao.getTransactionById(id) ?: return@launch
            editingTransactionId.value = tx.id
            amountInput.value = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString()
            transactionType.value = tx.type
            selectedCategoryId.value = tx.categoryId
            noteInput.value = tx.note
            paymentMode.value = tx.paymentMode
            transactionScope.value = tx.scope
            selectedPaidBy.value = tx.paidBy ?: "Me"
            selectedDate.value = tx.date
            receiptUri.value = tx.receiptUri
        }
    }

    fun setReceipt(uri: String) {
        receiptUri.value = uri
    }

    fun removeReceipt() {
        receiptUri.value?.let { ReceiptStorage.deleteReceipt(appContext, it) }
        receiptUri.value = null
    }

    fun applyPreset(categoryName: String, paymentModeName: String?) {
        viewModelScope.launch {
            val cats = categories.value
            val match = cats.find { it.name.equals(categoryName, ignoreCase = true) }
            if (match != null) {
                selectedCategoryId.value = match.id
            }
            if (paymentModeName != null) {
                try {
                    paymentMode.value = PaymentMode.valueOf(paymentModeName.uppercase())
                } catch (_: Exception) { }
            }
        }
    }

    fun onNumpadClick(key: String) {
        val current = amountInput.value
        when (key) {
            "C" -> {
                amountInput.value = "0"
            }
            "DEL" -> {
                if (current.length > 1) {
                    amountInput.value = current.dropLast(1)
                } else {
                    amountInput.value = "0"
                }
            }
            "." -> {
                val lastNumber = current.split("+", "-").lastOrNull() ?: ""
                if (!lastNumber.contains(".")) {
                    amountInput.value = "$current."
                }
            }
            "+", "-" -> {
                // Evaluate current expression if any, then append operator
                val evaluated = evaluateExpression(current)
                if (evaluated != null) {
                    amountInput.value = "$evaluated$key"
                } else if (!current.endsWith("+") && !current.endsWith("-")) {
                    amountInput.value = "$current$key"
                }
            }
            "=" -> {
                val evaluated = evaluateExpression(current)
                if (evaluated != null) {
                    amountInput.value = evaluated
                }
            }
            "+50" -> addQuickAmount(50.0)
            "+100" -> addQuickAmount(100.0)
            "+500" -> addQuickAmount(500.0)
            "+2000" -> addQuickAmount(2000.0)
            else -> { // Digit
                if (current == "0") {
                    amountInput.value = key
                } else {
                    // Limit max 2 decimal places in active operand
                    val lastNumber = current.split("+", "-").lastOrNull() ?: ""
                    val parts = lastNumber.split(".")
                    if (parts.size == 2 && parts[1].length >= 2) return
                    if (current.length >= 12) return
                    amountInput.value = current + key
                }
            }
        }
    }

    private fun evaluateExpression(expr: String): String? {
        val trimmed = expr.trimEnd('+', '-')
        if (trimmed.isEmpty()) return null

        val plusParts = trimmed.split("+")
        var sum = 0.0
        try {
            for (part in plusParts) {
                if (part.contains("-")) {
                    val minusParts = part.split("-")
                    var sub = minusParts.firstOrNull()?.toDoubleOrNull() ?: return null
                    for (i in 1 until minusParts.size) {
                        val v = minusParts[i].toDoubleOrNull() ?: return null
                        sub -= v
                    }
                    sum += sub
                } else {
                    val v = part.toDoubleOrNull() ?: return null
                    sum += v
                }
            }
            return if (sum % 1.0 == 0.0) {
                sum.toLong().toString()
            } else {
                String.format(java.util.Locale.US, "%.2f", sum)
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun addQuickAmount(addValue: Double) {
        val currentEvaluated = evaluateExpression(amountInput.value)?.toDoubleOrNull() ?: 0.0
        val newVal = currentEvaluated + addValue
        amountInput.value = if (newVal % 1.0 == 0.0) {
            newVal.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", newVal)
        }
    }

    fun setDateToToday() {
        selectedDate.value = System.currentTimeMillis()
    }

    fun setDateToYesterday() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        selectedDate.value = cal.timeInMillis
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val evaluatedString = evaluateExpression(amountInput.value) ?: amountInput.value
        val amount = evaluatedString.toDoubleOrNull() ?: return
        if (amount <= 0.0) return
        val categoryId = selectedCategoryId.value ?: return

        viewModelScope.launch {
            val existingId = editingTransactionId.value
            val transaction = Transaction(
                id = existingId ?: 0,
                amount = amount,
                type = transactionType.value,
                categoryId = categoryId,
                note = noteInput.value.trim(),
                date = selectedDate.value,
                paymentMode = paymentMode.value,
                scope = transactionScope.value,
                paidBy = if (transactionScope.value == TransactionScope.HOUSEHOLD) selectedPaidBy.value else null,
                receiptUri = receiptUri.value
            )
            if (existingId != null && existingId > 0) {
                moneyDao.updateTransaction(transaction)
            } else {
                moneyDao.insertTransaction(transaction)
            }
            onSuccess()
        }
    }
}
