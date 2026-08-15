package com.fintrack.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility helper mapping database icon strings and hex color strings to Compose representations.
 */
object CategoryIconHelper {
    fun getIconByName(iconName: String): ImageVector {
        return when (iconName.lowercase().trim()) {
            "fastfood", "restaurant", "food", "an_uong" -> Icons.Default.Fastfood
            "shoppingcart", "shopping_cart", "mua_sam" -> Icons.Default.ShoppingCart
            "directionscar", "directions_car", "commute", "di_lai" -> Icons.Default.DirectionsCar
            "home", "house", "nha_cua" -> Icons.Default.Home
            "sportsesports", "sports_esports", "giai_tri" -> Icons.Default.SportsEsports
            "localhospital", "local_hospital", "medical", "y_te" -> Icons.Default.LocalHospital
            "school", "education", "giao_duc" -> Icons.Default.School
            "fitnesscenter", "fitness_center", "the_thao" -> Icons.Default.FitnessCenter
            "paid", "salary", "tien_luong" -> Icons.Default.Paid
            "work", "bonus", "thuong" -> Icons.Default.Work
            "trendingup", "trending_up", "dau_tu" -> Icons.AutoMirrored.Filled.TrendingUp
            "redeem", "gift", "qua_tang" -> Icons.Default.Redeem
            else -> Icons.Default.Category
        }
    }

    fun parseColor(hex: String): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorLong)
            } else {
                Color(colorLong)
            }
        } catch (e: Exception) {
            Color(0xFF1A237E)
        }
    }
}
