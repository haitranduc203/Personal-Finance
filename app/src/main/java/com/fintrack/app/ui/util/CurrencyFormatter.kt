package com.fintrack.app.ui.util

import com.fintrack.app.data.local.preferences.CurrencyConfig
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Thread-safe utility for currency formatting across all UI and presentation layers.
 */
object CurrencyFormatter {

    fun format(
        amount: Long,
        currency: CurrencyConfig = CurrencyConfig.VND,
        withSign: Boolean = false,
        isIncome: Boolean = false,
        isExpense: Boolean = false
    ): String {
        val locale = when (currency) {
            CurrencyConfig.VND -> Locale("vi", "VN")
            CurrencyConfig.USD -> Locale.US
            CurrencyConfig.EUR -> Locale.GERMANY
        }
        val nf = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 0
        }
        val formatted = nf.format(abs(amount))
        val symbol = currency.symbol

        val baseFormatted = if (currency == CurrencyConfig.USD) {
            "$symbol$formatted"
        } else {
            "$formatted $symbol"
        }

        return when {
            withSign && isIncome -> "+$baseFormatted"
            withSign && isExpense -> "-$baseFormatted"
            withSign -> {
                val prefix = if (amount < 0) "-" else if (amount > 0) "+" else ""
                "$prefix$baseFormatted"
            }
            else -> baseFormatted
        }
    }

    fun formatSigned(
        amount: Long,
        currency: CurrencyConfig = CurrencyConfig.VND,
        isExpense: Boolean
    ): String {
        val prefix = if (isExpense) "-" else "+"
        return "$prefix${format(abs(amount), currency)}"
    }
}
