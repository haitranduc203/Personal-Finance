package com.fintrack.app.data.local.model

/**
 * Aggregated category expense data for statistics and chart reporting.
 */
data class CategoryExpense(
    val categoryId: Long,
    val categoryName: String,
    val categoryIconKey: String,
    val categoryColorKey: String,
    val totalAmount: Long
)
