package com.example.moneymanager.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FormatUtilsTest {

    @Test
    fun testFormatCurrency_IndianGrouping() {
        assertEquals("₹500", FormatUtils.formatCurrency(500.0, useIndianGrouping = true))
        assertEquals("₹1,500", FormatUtils.formatCurrency(1500.0, useIndianGrouping = true))
        assertEquals("₹1,50,000", FormatUtils.formatCurrency(150000.0, useIndianGrouping = true))
        assertEquals("₹10,00,000", FormatUtils.formatCurrency(1000000.0, useIndianGrouping = true))
        assertEquals("₹1,50,000.50", FormatUtils.formatCurrency(150000.50, useIndianGrouping = true))
    }

    @Test
    fun testFormatDate_TodayAndYesterday() {
        val now = System.currentTimeMillis()
        assertEquals("Today", FormatUtils.formatDate(now))

        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        assertEquals("Yesterday", FormatUtils.formatDate(cal.timeInMillis))
    }

    @Test
    fun testFormatMonth() {
        val result = FormatUtils.formatMonth("2026-08")
        assertTrue(result.contains("August") && result.contains("2026"))
    }

    @Test
    fun testGetMonthTimestampRange() {
        val (start, end) = FormatUtils.getMonthTimestampRange("2026-08")
        assertTrue(start < end)
        val cal = Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
    }
}
