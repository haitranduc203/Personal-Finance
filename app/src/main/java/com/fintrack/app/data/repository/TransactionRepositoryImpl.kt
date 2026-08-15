package com.fintrack.app.data.repository

import com.fintrack.app.data.local.dao.TransactionDao
import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.CategoryExpense
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Default implementation of [TransactionRepository] backed by [TransactionDao].
 */
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<TransactionWithCategory>> {
        return transactionDao.observeTransactions()
    }

    override fun observeTransactionById(id: Long): Flow<TransactionWithCategory?> {
        return transactionDao.observeById(id)
    }

    override suspend fun getTransactionById(id: Long): TransactionWithCategory? {
        return transactionDao.getById(id)
    }

    override fun observeTransactionsByPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithCategory>> {
        return transactionDao.observeTransactionsByPeriod(startDate, endDate)
    }

    override fun observeTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> {
        return transactionDao.observeTransactionsByType(type)
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>> {
        return transactionDao.observeRecentTransactions(limit)
    }

    override fun observeTotalIncome(): Flow<Long> {
        return transactionDao.observeTotalIncome()
    }

    override fun observeTotalExpense(): Flow<Long> {
        return transactionDao.observeTotalExpense()
    }

    override fun observeBalance(): Flow<Long> {
        return combine(
            observeTotalIncome(),
            observeTotalExpense()
        ) { totalIncome, totalExpense ->
            totalIncome - totalExpense
        }
    }

    override fun observeIncomeByPeriod(startDate: Long, endDate: Long): Flow<Long> {
        return transactionDao.observeIncomeByPeriod(startDate, endDate)
    }

    override fun observeExpenseByPeriod(startDate: Long, endDate: Long): Flow<Long> {
        return transactionDao.observeExpenseByPeriod(startDate, endDate)
    }

    override fun observeCategoryExpensesByPeriod(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryExpense>> {
        return transactionDao.observeCategoryExpensesByPeriod(startDate, endDate)
    }

    override suspend fun addTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    override suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteById(id)
    }
}
