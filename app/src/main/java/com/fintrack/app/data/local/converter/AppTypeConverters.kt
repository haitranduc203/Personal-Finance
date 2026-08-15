package com.fintrack.app.data.local.converter

import androidx.room.TypeConverter
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.model.TransactionType

/**
 * Room Type Converters for Enums used across database entities.
 */
class AppTypeConverters {

    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { enumValueOf<TransactionType>(it) }
    }

    @TypeConverter
    fun fromCategoryType(type: CategoryType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toCategoryType(value: String?): CategoryType? {
        return value?.let { enumValueOf<CategoryType>(it) }
    }
}
