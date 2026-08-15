package com.fintrack.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fintrack.app.data.local.model.CategoryType

/**
 * Room Entity representing a transaction category.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconKey: String,
    val colorKey: String,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
