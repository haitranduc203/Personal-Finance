package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.CategoryExpense
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.CategoryRepository
import com.fintrack.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeCategoryRepository(
    initialCategories: List<CategoryEntity> = listOf(
        CategoryEntity(1L, "Ăn uống", "fastfood", "#FFA000", CategoryType.EXPENSE, true),
        CategoryEntity(2L, "Mua sắm", "shopping_cart", "#7B1FA2", CategoryType.EXPENSE, true),
        CategoryEntity(8L, "Tiền lương", "paid", "#2E7D32", CategoryType.INCOME, true)
    )
) : CategoryRepository {

    private val _categories = MutableStateFlow(initialCategories)

    override fun observeCategories(): Flow<List<CategoryEntity>> = _categories.asStateFlow()

    override fun observeCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>> =
        _categories.map { list -> list.filter { it.type == type } }

    override suspend fun getCategoryById(id: Long): CategoryEntity? =
        _categories.value.find { it.id == id }

    override suspend fun addCategory(category: CategoryEntity): Long {
        val newId = (_categories.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val item = category.copy(id = newId)
        _categories.value = _categories.value + item
        return newId
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        _categories.value = _categories.value.filterNot { it.id == category.id }
    }

    override suspend fun seedDefaultCategoriesIfEmpty() {
        // No-op for fake
    }
}

class FakeTransactionRepository : TransactionRepository {
    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val categoriesMap = mutableMapOf(
        1L to CategoryEntity(1L, "Ăn uống", "fastfood", "#FFA000", CategoryType.EXPENSE, true),
        2L to CategoryEntity(2L, "Mua sắm", "shopping_cart", "#7B1FA2", CategoryType.EXPENSE, true),
        8L to CategoryEntity(8L, "Tiền lương", "paid", "#2E7D32", CategoryType.INCOME, true)
    )

    override fun observeTransactions(): Flow<List<TransactionWithCategory>> {
        return _transactions.map { list ->
            list.map { tx ->
                TransactionWithCategory(
                    transaction = tx,
                    category = categoriesMap[tx.categoryId]
                        ?: CategoryEntity(tx.categoryId, "Khác", "category", "#9E9E9E", CategoryType.EXPENSE, false)
                )
            }
        }
    }

    override fun observeTransactionById(id: Long): Flow<TransactionWithCategory?> {
        return _transactions.map { list ->
            val tx = list.find { it.id == id } ?: return@map null
            val cat = categoriesMap[tx.categoryId]
                ?: CategoryEntity(tx.categoryId, "Khác", "category", "#9E9E9E", CategoryType.EXPENSE, false)
            TransactionWithCategory(tx, cat)
        }
    }

    override suspend fun getTransactionById(id: Long): TransactionWithCategory? {
        val tx = _transactions.value.find { it.id == id } ?: return null
        val cat = categoriesMap[tx.categoryId]
            ?: CategoryEntity(tx.categoryId, "Khác", "category", "#9E9E9E", CategoryType.EXPENSE, false)
        return TransactionWithCategory(tx, cat)
    }

    override fun observeTransactionsByPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>> {
        return _transactions.map { list ->
            list.filter { it.transactionDate in startDate..endDate }.map { tx ->
                TransactionWithCategory(
                    tx,
                    categoriesMap[tx.categoryId]
                        ?: CategoryEntity(tx.categoryId, "Khác", "category", "#9E9E9E", CategoryType.EXPENSE, false)
                )
            }
        }
    }

    override fun observeTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> {
        return _transactions.map { list ->
            list.filter { it.type == type }.map { tx ->
                TransactionWithCategory(
                    tx,
                    categoriesMap[tx.categoryId]
                        ?: CategoryEntity(tx.categoryId, "Khác", "category", "#9E9E9E", CategoryType.EXPENSE, false)
                )
            }
        }
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>> {
        return _transactions.map { list ->
            list.take(limit).map { tx ->
                TransactionWithCategory(
                    tx,
                    categoriesMap[tx.categoryId]
                        ?: CategoryEntity(tx.categoryId, "Khác", "category", "#9E9E9E", CategoryType.EXPENSE, false)
                )
            }
        }
    }

    override fun observeTotalIncome(): Flow<Long> {
        return _transactions.map { list ->
            list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        }
    }

    override fun observeTotalExpense(): Flow<Long> {
        return _transactions.map { list ->
            list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        }
    }

    override fun observeBalance(): Flow<Long> {
        return _transactions.map { list ->
            val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            income - expense
        }
    }

    override fun observeIncomeByPeriod(startDate: Long, endDate: Long): Flow<Long> {
        return _transactions.map { list ->
            list.filter { it.type == TransactionType.INCOME && it.transactionDate in startDate..endDate }.sumOf { it.amount }
        }
    }

    override fun observeExpenseByPeriod(startDate: Long, endDate: Long): Flow<Long> {
        return _transactions.map { list ->
            list.filter { it.type == TransactionType.EXPENSE && it.transactionDate in startDate..endDate }.sumOf { it.amount }
        }
    }

    override fun observeCategoryExpensesByPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryExpense>> {
        return _transactions.map { emptyList() }
    }

    override suspend fun addTransaction(transaction: TransactionEntity): Long {
        val newId = (_transactions.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val item = transaction.copy(id = newId)
        _transactions.value = _transactions.value + item
        return newId
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        _transactions.value = _transactions.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        _transactions.value = _transactions.value.filterNot { it.id == transaction.id }
    }

    override suspend fun deleteTransactionById(id: Long) {
        _transactions.value = _transactions.value.filterNot { it.id == id }
    }
}
