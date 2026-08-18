package com.example.moneymanager.util

import com.example.moneymanager.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupUtilsTest {

    @Test
    fun testExportAndImportJson_Roundtrip() {
        val transactions = listOf(
            Transaction(
                id = 1,
                amount = 250.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                note = "Chai & Samosa",
                date = 1723800000000L,
                paymentMode = PaymentMode.UPI,
                scope = TransactionScope.PERSONAL,
                paidBy = null
            )
        )
        val categories = listOf(
            Category(
                id = 1,
                name = "Chai & Snacks",
                icon = "local_cafe",
                color = 4293498930,
                isDefault = true
            )
        )
        val budgets = listOf(
            Budget(
                id = 1,
                categoryId = 1,
                month = "2026-08",
                amountLimit = 1500.0
            )
        )
        val recurring = listOf(
            RecurringRule(
                id = 1,
                amount = 999.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                note = "WiFi Bill",
                paymentMode = PaymentMode.UPI,
                scope = TransactionScope.HOUSEHOLD,
                frequency = Frequency.MONTHLY,
                nextDueDate = 1723800000000L
            )
        )
        val members = listOf(
            HouseholdMember(id = 1, name = "Me"),
            HouseholdMember(id = 2, name = "Roommate")
        )

        val json = BackupUtils.exportToJson(transactions, categories, budgets, recurring, members)
        assertTrue(json.contains("Chai & Samosa"))
        assertTrue(json.contains("WiFi Bill"))

        val imported = BackupUtils.importFromJson(json)
        assertEquals(1, imported.transactions.size)
        assertEquals("Chai & Samosa", imported.transactions.first().note)
        assertEquals(250.0, imported.transactions.first().amount, 0.001)
        assertEquals(1, imported.categories.size)
        assertEquals("Chai & Snacks", imported.categories.first().name)
        assertEquals(1, imported.budgets.size)
        assertEquals(1500.0, imported.budgets.first().amountLimit, 0.001)
        assertEquals(1, imported.recurringRules.size)
        assertEquals(2, imported.householdMembers.size)
    }

    @Test
    fun testExportCsv() {
        val transactions = listOf(
            Transaction(
                id = 1,
                amount = 450.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                note = "Lunch with friends",
                date = 1723800000000L,
                paymentMode = PaymentMode.CARD,
                scope = TransactionScope.HOUSEHOLD,
                paidBy = "Me"
            )
        )
        val categories = mapOf(
            1L to Category(id = 1, name = "Food Delivery", icon = "restaurant", color = 0, isDefault = true)
        )

        val csv = BackupUtils.exportToCsv(transactions, categories)
        assertTrue(csv.contains("Lunch with friends"))
        assertTrue(csv.contains("Food Delivery"))
        assertTrue(csv.contains("CARD"))
        assertTrue(csv.contains("HOUSEHOLD"))
    }
}
