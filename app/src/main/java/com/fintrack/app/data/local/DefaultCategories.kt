package com.fintrack.app.data.local

import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.model.CategoryType

/**
 * Standard default categories for initial database seeding.
 */
object DefaultCategories {

    val list: List<CategoryEntity> = listOf(
        // Expense Categories
        CategoryEntity(
            id = 1L,
            name = "Ăn uống",
            iconKey = "Fastfood",
            colorKey = "#FFA000",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 2L,
            name = "Mua sắm",
            iconKey = "ShoppingCart",
            colorKey = "#7B1FA2",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 3L,
            name = "Đi lại",
            iconKey = "DirectionsCar",
            colorKey = "#0288D1",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 4L,
            name = "Nhà cửa",
            iconKey = "Home",
            colorKey = "#5D4037",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 5L,
            name = "Giải trí",
            iconKey = "SportsEsports",
            colorKey = "#E91E63",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 6L,
            name = "Y tế",
            iconKey = "LocalHospital",
            colorKey = "#D32F2F",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 7L,
            name = "Giáo dục",
            iconKey = "School",
            colorKey = "#388E3C",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),
        CategoryEntity(
            id = 8L,
            name = "Hóa đơn",
            iconKey = "Receipt",
            colorKey = "#F57C00",
            type = CategoryType.EXPENSE,
            isDefault = true
        ),

        // Income Categories
        CategoryEntity(
            id = 9L,
            name = "Tiền lương",
            iconKey = "Paid",
            colorKey = "#2E7D32",
            type = CategoryType.INCOME,
            isDefault = true
        ),
        CategoryEntity(
            id = 10L,
            name = "Thưởng",
            iconKey = "Work",
            colorKey = "#00796B",
            type = CategoryType.INCOME,
            isDefault = true
        ),
        CategoryEntity(
            id = 11L,
            name = "Đầu tư",
            iconKey = "TrendingUp",
            colorKey = "#1976D2",
            type = CategoryType.INCOME,
            isDefault = true
        ),
        CategoryEntity(
            id = 12L,
            name = "Khác",
            iconKey = "AccountBalanceWallet",
            colorKey = "#546E7A",
            type = CategoryType.BOTH,
            isDefault = true
        )
    )
}
