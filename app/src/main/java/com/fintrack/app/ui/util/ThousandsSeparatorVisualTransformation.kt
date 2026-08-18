package com.fintrack.app.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that formats digit strings with a thousands separator (default '.').
 * Keeps underlying state as raw digits while presenting formatted currency amounts.
 */
class ThousandsSeparatorVisualTransformation(
    private val separator: Char = '.'
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = buildString {
            val reversed = raw.reversed()
            for (i in reversed.indices) {
                if (i > 0 && i % 3 == 0) {
                    append(separator)
                }
                append(reversed[i])
            }
        }.reversed()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceIn(0, raw.length)
                var separatorsCount = 0
                for (k in 0 until safeOffset) {
                    val distFromRight = raw.length - 1 - k
                    if (distFromRight > 0 && distFromRight % 3 == 0) {
                        separatorsCount++
                    }
                }
                return (safeOffset + separatorsCount).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceIn(0, formatted.length)
                var separatorsCount = 0
                for (i in 0 until safeOffset) {
                    if (formatted[i] == separator) {
                        separatorsCount++
                    }
                }
                return (safeOffset - separatorsCount).coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
