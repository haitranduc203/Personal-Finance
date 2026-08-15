package com.fintrack.app.data.repository

import com.fintrack.app.data.local.DefaultCategories
import com.fintrack.app.data.local.dao.CategoryDao
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.model.CategoryType
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [CategoryRepository] backed by [CategoryDao].
 */
class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.observeCategories()
    }

    override fun observeCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>> {
        return categoryDao.observeCategoriesByType(type)
    }

    override suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getById(id)
    }

    override suspend fun addCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    override suspend fun seedDefaultCategoriesIfEmpty() {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(DefaultCategories.list)
        }
    }
}
