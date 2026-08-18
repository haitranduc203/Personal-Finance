package com.fintrack.app.ui.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class ThousandsSeparatorVisualTransformationTest {

    private val transformation = ThousandsSeparatorVisualTransformation('.')

    @Test
    fun emptyString_returnsIdentity() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
    }

    @Test
    fun shortDigits_noSeparatorAdded() {
        val result = transformation.filter(AnnotatedString("500"))
        assertEquals("500", result.text.text)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(3, result.offsetMapping.originalToTransformed(3))
        assertEquals(3, result.offsetMapping.transformedToOriginal(3))
    }

    @Test
    fun fourDigits_addsOneSeparator() {
        val result = transformation.filter(AnnotatedString("5000"))
        assertEquals("5.000", result.text.text)
        assertEquals(0, result.offsetMapping.originalToTransformed(0)) // before 5 -> before 5
        assertEquals(2, result.offsetMapping.originalToTransformed(1)) // after 5 -> after .
        assertEquals(5, result.offsetMapping.originalToTransformed(4)) // end -> end
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
        assertEquals(1, result.offsetMapping.transformedToOriginal(1)) // after 5, before .
        assertEquals(1, result.offsetMapping.transformedToOriginal(2)) // after .
        assertEquals(4, result.offsetMapping.transformedToOriginal(5)) // end
    }

    @Test
    fun sevenDigits_formatsMillionsCorrectly() {
        val result = transformation.filter(AnnotatedString("5000000"))
        assertEquals("5.000.000", result.text.text)
        assertEquals(9, result.text.length)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(2, result.offsetMapping.originalToTransformed(1))
        assertEquals(9, result.offsetMapping.originalToTransformed(7))

        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
        assertEquals(7, result.offsetMapping.transformedToOriginal(9))
    }
}
