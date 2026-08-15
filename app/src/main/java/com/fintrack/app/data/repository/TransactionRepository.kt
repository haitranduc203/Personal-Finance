package com.fintrack.app.data.repository

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.CategoryExpense
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing transaction operations.
 * Acts as the boundary between Data Layer and Domain / Presentation Layers.
 */
interface TransactionRepository {

    fun observeTransactions(): Flow<List<TransactionWithCategory>>

    fun observeTransactionById(id: Long): Flow<TransactionWithCategory?>

    suspend fun getTransactionById(id: Long): TransactionWithCategory?

    fun observeTransactionsByPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    fun observeTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>>

    fun observeRecentTransactions(limit: Int = 5): Flow<List<TransactionWithCategory>>

    fun observeTotalIncome(): Flow<Long>

    fun observeTotalExpense(): Flow<Long>

    fun observeBalance(): Flow<Long>

    fun observeIncomeByPeriod(startDate: Long, endDate: Long): Flow<Long>

    fun observeExpenseByPeriod(startDate: Long, endDate: Long): Flow<Long>

    fun observeCategoryExpensesByPeriod(startDate: Long, endDate: Long): Flow<List<CategoryExpense>>

    suspend fun addTransaction(transaction: TransactionEntity): Long

    suspend fun updateTransaction(transaction: TransactionEntity)

    suspend fun deleteTransaction(transaction: TransactionEntity)

    suspend fun deleteTransactionById(id: Long)
}
