package com.fintrack.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.entity.TransactionEntity

/**
 * Composite model binding a [TransactionEntity] with its referenced [CategoryEntity].
 */
data class TransactionWithCategory(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity
)
