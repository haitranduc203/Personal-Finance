package com.fintrack.app.ui.util

import com.fintrack.app.data.local.preferences.CurrencyConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun format_vnd_standard() {
        val result = CurrencyFormatter.format(50000L, CurrencyConfig.VND)
        assertEquals("50.000 ₫", result)
    }

    @Test
    fun format_vnd_withExpenseSign() {
        val result = CurrencyFormatter.format(
            amount = 120000L,
            currency = CurrencyConfig.VND,
            withSign = true,
            isExpense = true
        )
        assertEquals("-120.000 ₫", result)
    }

    @Test
    fun format_vnd_withIncomeSign() {
        val result = CurrencyFormatter.format(
            amount = 25000000L,
            currency = CurrencyConfig.VND,
            withSign = true,
            isIncome = true
        )
        assertEquals("+25.000.000 ₫", result)
    }

    @Test
    fun format_usd_standard() {
        val result = CurrencyFormatter.format(1500L, CurrencyConfig.USD)
        assertEquals("$1,500", result)
    }

    @Test
    fun format_usd_withExpenseSign() {
        val result = CurrencyFormatter.format(
            amount = 50L,
            currency = CurrencyConfig.USD,
            withSign = true,
            isExpense = true
        )
        assertEquals("-$50", result)
    }

    @Test
    fun format_eur_standard() {
        val result = CurrencyFormatter.format(2000L, CurrencyConfig.EUR)
        assertEquals("2.000 €", result)
    }

    @Test
    fun format_zeroAmount() {
        val result = CurrencyFormatter.format(0L, CurrencyConfig.VND)
        assertEquals("0 ₫", result)
    }
}
