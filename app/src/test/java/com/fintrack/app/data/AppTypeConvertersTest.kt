package com.fintrack.app.data

import com.fintrack.app.data.local.converter.AppTypeConverters
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppTypeConvertersTest {

    private val converters = AppTypeConverters()

    @Test
    fun testTransactionTypeConversion() {
        assertEquals("INCOME", converters.fromTransactionType(TransactionType.INCOME))
        assertEquals("EXPENSE", converters.fromTransactionType(TransactionType.EXPENSE))
        assertNull(converters.fromTransactionType(null))

        assertEquals(TransactionType.INCOME, converters.toTransactionType("INCOME"))
        assertEquals(TransactionType.EXPENSE, converters.toTransactionType("EXPENSE"))
        assertNull(converters.toTransactionType(null))
    }

    @Test
    fun testCategoryTypeConversion() {
        assertEquals("INCOME", converters.fromCategoryType(CategoryType.INCOME))
        assertEquals("EXPENSE", converters.fromCategoryType(CategoryType.EXPENSE))
        assertEquals("BOTH", converters.fromCategoryType(CategoryType.BOTH))
        assertNull(converters.fromCategoryType(null))

        assertEquals(CategoryType.INCOME, converters.toCategoryType("INCOME"))
        assertEquals(CategoryType.EXPENSE, converters.toCategoryType("EXPENSE"))
        assertEquals(CategoryType.BOTH, converters.toCategoryType("BOTH"))
        assertNull(converters.toCategoryType(null))
    }
}
