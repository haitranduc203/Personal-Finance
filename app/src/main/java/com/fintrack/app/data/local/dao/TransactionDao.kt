package com.fintrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.CategoryExpense
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [TransactionEntity].
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Delete
    suspend fun delete(transaction: TransactionEntity): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll(): Int

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC")
    fun observeTransactions(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionWithCategory?>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionWithCategory?

    @Transaction
    @Query("SELECT * FROM transactions WHERE transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC, id DESC")
    fun observeTransactionsByPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY transactionDate DESC, id DESC")
    fun observeTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int = 5): Flow<List<TransactionWithCategory>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun observeTotalIncome(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun observeTotalExpense(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND transactionDate BETWEEN :startDate AND :endDate")
    fun observeIncomeByPeriod(startDate: Long, endDate: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND transactionDate BETWEEN :startDate AND :endDate")
    fun observeExpenseByPeriod(startDate: Long, endDate: Long): Flow<Long>

    @Query(
        """
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconKey AS categoryIconKey,
            c.colorKey AS categoryColorKey,
            SUM(t.amount) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY c.id, c.name, c.iconKey, c.colorKey
        ORDER BY totalAmount DESC
        """
    )
    fun observeCategoryExpensesByPeriod(startDate: Long, endDate: Long): Flow<List<CategoryExpense>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}
