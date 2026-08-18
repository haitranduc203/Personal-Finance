package com.fintrack.app.data

import com.fintrack.app.data.local.dao.TransactionDao
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.CategoryExpense
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.TransactionRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Fake TransactionDao for Unit Testing repository business logic.
 */
class FakeTransactionDao : TransactionDao {
    private val transactions = mutableListOf<TransactionEntity>()
    private val categories = mutableMapOf(
        1L to CategoryEntity(1L, "Ăn uống", "Fastfood", "#FFA000", CategoryType.EXPENSE, true),
        9L to CategoryEntity(9L, "Tiền lương", "Paid", "#2E7D32", CategoryType.INCOME, true)
    )

    override suspend fun insert(transaction: TransactionEntity): Long {
        val newId = (transactions.maxOfOrNull { it.id } ?: 0L) + 1L
        val item = transaction.copy(id = newId)
        transactions.add(item)
        return newId
    }

    override suspend fun update(transaction: TransactionEntity): Int {
        val index = transactions.indexOfFirst { it.id == transaction.id }
        return if (index != -1) {
            transactions[index] = transaction
            1
        } else 0
    }

    override suspend fun delete(transaction: TransactionEntity): Int {
        val initialSize = transactions.size
        transactions.removeAll { it.id == transaction.id }
        return initialSize - transactions.size
    }

    override suspend fun deleteById(id: Long): Int {
        val initialSize = transactions.size
        transactions.removeAll { it.id == id }
        return initialSize - transactions.size
    }

    override fun observeTransactions(): Flow<List<TransactionWithCategory>> {
        val list = transactions.map {
            TransactionWithCategory(
                transaction = it,
                category = categories[it.categoryId] ?: CategoryEntity(0L, "Unknown", "", "", CategoryType.BOTH)
            )
        }
        return flowOf(list)
    }

    override fun observeById(id: Long): Flow<TransactionWithCategory?> {
        val item = transactions.find { it.id == id }?.let {
            TransactionWithCategory(
                transaction = it,
                category = categories[it.categoryId] ?: CategoryEntity(0L, "Unknown", "", "", CategoryType.BOTH)
            )
        }
        return flowOf(item)
    }

    override suspend fun getById(id: Long): TransactionWithCategory? {
        return transactions.find { it.id == id }?.let {
            TransactionWithCategory(
                transaction = it,
                category = categories[it.categoryId] ?: CategoryEntity(0L, "Unknown", "", "", CategoryType.BOTH)
            )
        }
    }

    override fun observeTransactionsByPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>> {
        val list = transactions.filter { it.transactionDate in startDate..endDate }.map {
            TransactionWithCategory(
                transaction = it,
                category = categories[it.categoryId] ?: CategoryEntity(0L, "Unknown", "", "", CategoryType.BOTH)
            )
        }
        return flowOf(list)
    }

    override fun observeTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> {
        val list = transactions.filter { it.type == type }.map {
            TransactionWithCategory(
                transaction = it,
                category = categories[it.categoryId] ?: CategoryEntity(0L, "Unknown", "", "", CategoryType.BOTH)
            )
        }
        return flowOf(list)
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>> {
        val list = transactions.take(limit).map {
            TransactionWithCategory(
                transaction = it,
                category = categories[it.categoryId] ?: CategoryEntity(0L, "Unknown", "", "", CategoryType.BOTH)
            )
        }
        return flowOf(list)
    }

    override fun observeTotalIncome(): Flow<Long> {
        val total = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        return flowOf(total)
    }

    override fun observeTotalExpense(): Flow<Long> {
        val total = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return flowOf(total)
    }

    override fun observeIncomeByPeriod(startDate: Long, endDate: Long): Flow<Long> {
        val total = transactions.filter { it.type == TransactionType.INCOME && it.transactionDate in startDate..endDate }
            .sumOf { it.amount }
        return flowOf(total)
    }

    override fun observeExpenseByPeriod(startDate: Long, endDate: Long): Flow<Long> {
        val total = transactions.filter { it.type == TransactionType.EXPENSE && it.transactionDate in startDate..endDate }
            .sumOf { it.amount }
        return flowOf(total)
    }

    override fun observeCategoryExpensesByPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryExpense>> {
        return flowOf(emptyList())
    }

    override suspend fun count(): Int = transactions.size

    override suspend fun deleteAll(): Int {
        val count = transactions.size
        transactions.clear()
        return count
    }
}

class TransactionRepositoryTest {

    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeTransactionDao()
        repository = TransactionRepositoryImpl(fakeDao)
    }

    @Test
    fun testAddTransactionAndCalculateBalance() = runTest {
        // 1. Add Income: 20,000,000
        val incomeId = repository.addTransaction(
            TransactionEntity(
                amount = 20_000_000L,
                type = TransactionType.INCOME,
                categoryId = 9L,
                note = "Lương tháng",
                transactionDate = System.currentTimeMillis()
            )
        )
        assertEquals(1L, incomeId)

        // 2. Add Expense: 50,000
        val expenseId = repository.addTransaction(
            TransactionEntity(
                amount = 50_000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Ăn trưa",
                transactionDate = System.currentTimeMillis()
            )
        )
        assertEquals(2L, expenseId)

        // 3. Verify Income, Expense, and Balance calculation
        val totalIncome = repository.observeTotalIncome().first()
        val totalExpense = repository.observeTotalExpense().first()
        val balance = repository.observeBalance().first()

        assertEquals(20_000_000L, totalIncome)
        assertEquals(50_000L, totalExpense)
        assertEquals(19_950_000L, balance)
    }

    @Test
    fun testDeleteTransactionUpdatesBalance() = runTest {
        val id1 = repository.addTransaction(
            TransactionEntity(
                amount = 500_000L,
                type = TransactionType.INCOME,
                categoryId = 9L,
                transactionDate = System.currentTimeMillis()
            )
        )
        val id2 = repository.addTransaction(
            TransactionEntity(
                amount = 200_000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                transactionDate = System.currentTimeMillis()
            )
        )

        assertEquals(300_000L, repository.observeBalance().first())

        // Delete expense
        repository.deleteTransactionById(id2)
        assertEquals(500_000L, repository.observeBalance().first())
    }

    @Test
    fun testGetTransactionById() = runTest {
        val id = repository.addTransaction(
            TransactionEntity(
                amount = 120_000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Cà phê",
                transactionDate = System.currentTimeMillis()
            )
        )

        val retrieved = repository.getTransactionById(id)
        assertNotNull(retrieved)
        assertEquals(120_000L, retrieved?.transaction?.amount)
        assertEquals("Ăn uống", retrieved?.category?.name)
    }
}
