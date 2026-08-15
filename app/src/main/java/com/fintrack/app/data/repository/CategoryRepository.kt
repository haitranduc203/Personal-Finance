package com.fintrack.app.data.repository

import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.model.CategoryType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing category data operations.
 */
interface CategoryRepository {

    fun observeCategories(): Flow<List<CategoryEntity>>

    fun observeCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>>

    suspend fun getCategoryById(id: Long): CategoryEntity?

    suspend fun addCategory(category: CategoryEntity): Long

    suspend fun updateCategory(category: CategoryEntity)

    suspend fun deleteCategory(category: CategoryEntity)

    suspend fun seedDefaultCategoriesIfEmpty()
}
