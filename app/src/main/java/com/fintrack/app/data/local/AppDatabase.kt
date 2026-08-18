package com.fintrack.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fintrack.app.data.local.converter.AppTypeConverters
import com.fintrack.app.data.local.dao.CategoryDao
import com.fintrack.app.data.local.dao.TransactionDao
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.entity.TransactionEntity

/**
 * Main Room Database for FinTrack application.
 * Manages local persistence for transactions and categories.
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DATABASE_NAME = "fintrack_database.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
