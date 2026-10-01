package com.example.moneymanager.util

import com.example.moneymanager.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiSmsParserTest {

    private val debitSms =
        "Rs.240.00 debited from A/c XX4521 on 06-Oct-26 10:24 by UPI " +
            "to CHAI POINT Ref 628484920111. Avl Bal Rs.12,410.50 - HDFC"

    private val creditSms =
        "Rs.5,000.00 credited to A/c XX4521 on 06-Oct-26 by UPI " +
            "from RAMESH K Ref 628484920112. Avl Bal Rs.17,410.50 - HDFC"

    private val otpSms =
        "Your OTP is 482913 for Rs.240.00 txn. Do not share with anyone. " +
            "Valid for 10 mins - HDFC"

    private val balanceOnlySms =
        "Available balance in your A/c XX4521 is Rs.15,000.00 as on 06-Oct-26 - SBI"

    @Test
    fun parse_debitedSms_returnsExpenseWithAmount() {
        val parsed = UpiSmsParser.parse(debitSms)
        assertNotNull(parsed)
        assertEquals(240.0, parsed!!.amount, 0.001)
        assertEquals(TransactionType.EXPENSE, parsed.type)
    }

    @Test
    fun parse_creditedSms_returnsIncomeWithAmount() {
        val parsed = UpiSmsParser.parse(creditSms)
        assertNotNull(parsed)
        assertEquals(5000.0, parsed!!.amount, 0.001)
        assertEquals(TransactionType.INCOME, parsed.type)
    }

    @Test
    fun isTransactionalSms_debitAndCredit_returnsTrue() {
        assertTrue(UpiSmsParser.isTransactionalSms(debitSms))
        assertTrue(UpiSmsParser.isTransactionalSms(creditSms))
    }

    @Test
    fun isTransactionalSms_otp_returnsFalse() {
        assertFalse(UpiSmsParser.isTransactionalSms(otpSms))
    }

    @Test
    fun isTransactionalSms_balanceOnlyUpdate_returnsFalse() {
        assertFalse(UpiSmsParser.isTransactionalSms(balanceOnlySms))
    }

    @Test
    fun isUpiSms_realDebitSms_stillTrue() {
        assertTrue(UpiSmsParser.isUpiSms("VM-HDFC", debitSms))
    }
}
