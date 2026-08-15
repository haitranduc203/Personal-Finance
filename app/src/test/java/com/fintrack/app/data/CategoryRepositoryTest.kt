package com.fintrack.app.data

import com.fintrack.app.data.local.dao.CategoryDao
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.repository.CategoryRepository
import com.fintrack.app.data.repository.CategoryRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FakeCategoryDao : CategoryDao {
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun observeCategories(): Flow<List<CategoryEntity>> = _categories

    override fun observeCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>> =
        _categories.map { list -> list.filter { it.type == type } }

    override suspend fun getById(id: Long): CategoryEntity? =
        _categories.value.find { it.id == id }

    override suspend fun getByName(name: String): CategoryEntity? =
        _categories.value.find { it.name == name }

    override suspend fun insert(category: CategoryEntity): Long {
        val newId = (_categories.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val item = category.copy(id = newId)
        _categories.value = _categories.value + item
        return newId
    }

    override suspend fun insertAll(categories: List<CategoryEntity>): List<Long> {
        val ids = mutableListOf<Long>()
        var curId = (_categories.value.maxOfOrNull { it.id } ?: 0L)
        val newItems = categories.map {
            curId += 1
            ids.add(curId)
            it.copy(id = curId)
        }
        _categories.value = _categories.value + newItems
        return ids
    }

    override suspend fun update(category: CategoryEntity): Int {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
        return 1
    }

    override suspend fun delete(category: CategoryEntity): Int {
        _categories.value = _categories.value.filterNot { it.id == category.id }
        return 1
    }

    override suspend fun count(): Int = _categories.value.size
}

class CategoryRepositoryTest {

    private lateinit var fakeDao: FakeCategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        fakeDao = FakeCategoryDao()
        repository = CategoryRepositoryImpl(fakeDao)
    }

    @Test
    fun seedDefaultCategoriesIfEmpty_seedsWhenCountIsZero() = runTest {
        assertEquals(0, fakeDao.count())
        repository.seedDefaultCategoriesIfEmpty()
        val seeded = repository.observeCategories().first()
        assertEquals(12, seeded.size)

        // Calling again should not duplicate
        repository.seedDefaultCategoriesIfEmpty()
        assertEquals(12, repository.observeCategories().first().size)
    }

    @Test
    fun observeCategoriesByType_filtersProperly() = runTest {
        repository.seedDefaultCategoriesIfEmpty()
        val expenseCats = repository.observeCategoriesByType(CategoryType.EXPENSE).first()
        val incomeCats = repository.observeCategoriesByType(CategoryType.INCOME).first()

        assertEquals(8, expenseCats.size)
        assertEquals(3, incomeCats.size)
    }

    @Test
    fun addAndGetCategory_worksCorrectly() = runTest {
        val cat = CategoryEntity(
            id = 0L,
            name = "Đầu tư Crypto",
            iconKey = "trending_up",
            colorKey = "#FF5722",
            type = CategoryType.INCOME,
            isDefault = false
        )
        val id = repository.addCategory(cat)
        val fetched = repository.getCategoryById(id)
        assertNotNull(fetched)
        assertEquals("Đầu tư Crypto", fetched?.name)
    }

    @Test
    fun deleteCategory_removesFromList() = runTest {
        val cat = CategoryEntity(
            id = 100L,
            name = "Tạm",
            iconKey = "delete",
            colorKey = "#9E9E9E",
            type = CategoryType.EXPENSE,
            isDefault = false
        )
        val id = repository.addCategory(cat)
        val created = repository.getCategoryById(id)
        assertNotNull(created)

        repository.deleteCategory(created!!)
        val deleted = repository.getCategoryById(id)
        assertNull(deleted)
    }
}
