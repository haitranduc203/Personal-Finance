package com.fintrack.app.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.fintrack.app.ui.components.EmptyStateView
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed

data class CategoryStatItem(
    val name: String,
    val totalAmountFormatted: String,
    val percentage: Float, // 0.0 to 1.0
    val color: Color
)

/**
 * Stateful entry composable for Statistics Screen.
 */
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier
) {
    var selectedPeriodIndex by remember { mutableIntStateOf(1) } // 0: Tuần, 1: Tháng, 2: Năm

    val sampleCategoryStats = listOf(
        CategoryStatItem("Mua sắm", "420.000 ₫", 0.78f, Color(0xFF7B1FA2)),
        CategoryStatItem("Ăn uống", "70.000 ₫", 0.13f, Color(0xFFFFA000)),
        CategoryStatItem("Đi lại", "50.000 ₫", 0.09f, Color(0xFF0288D1))
    )

    StatisticsScreenContent(
        selectedPeriodIndex = selectedPeriodIndex,
        onPeriodSelected = { selectedPeriodIndex = it },
        totalIncome = "+20.500.000 ₫",
        totalExpense = "-540.000 ₫",
        dailyAverage = "-18.000 ₫",
        categoryStats = sampleCategoryStats,
        modifier = modifier
    )
}

/**
 * Stateless pure UI component for Statistics Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    selectedPeriodIndex: Int,
    onPeriodSelected: (Int) -> Unit,
    totalIncome: String,
    totalExpense: String,
    dailyAverage: String,
    categoryStats: List<CategoryStatItem>,
    modifier: Modifier = Modifier
) {
    val periods = listOf("Tuần", "Tháng", "Năm")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Thống kê & Báo cáo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Period Selector (Segmented Button)
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    periods.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                            onClick = { onPeriodSelected(index) },
                            selected = index == selectedPeriodIndex
                        ) {
                            Text(text = label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // 2. Summary KPI Cards (3 columns/cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Expense Card
                    KpiCard(
                        title = "Tổng chi",
                        amount = totalExpense,
                        color = SemanticRed,
                        modifier = Modifier.weight(1f)
                    )
                    // Total Income Card
                    KpiCard(
                        title = "Tổng thu",
                        amount = totalIncome,
                        color = SemanticGreen,
                        modifier = Modifier.weight(1f)
                    )
                    // Daily Average Card
                    KpiCard(
                        title = "Trung bình/ngày",
                        amount = dailyAverage,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Category Breakdown Card (Donut Chart Preview)
            item {
                CategoryBreakdownCard(categoryStats = categoryStats)
            }

            // 4. Monthly Trend Bar Chart Preview
            item {
                MonthlyTrendBarChartCard()
            }
        }
    }
}

/**
 * KPI Summary Card component.
 */
@Composable
fun KpiCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Donut Chart & Category Breakdown Card.
 */
@Composable
fun CategoryBreakdownCard(
    categoryStats: List<CategoryStatItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cơ cấu chi tiêu theo danh mục",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (categoryStats.isEmpty()) {
                EmptyStateView(
                    title = "Chưa có dữ liệu thống kê",
                    description = "Dữ liệu biểu đồ sẽ hiển thị sau khi bạn ghi nhận giao dịch chi tiêu."
                )
            } else {
                // Donut Chart Graphic Skeleton & Percentages
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Canvas Donut Preview
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(90.dp)) {
                            var startAngle = -90f
                            categoryStats.forEach { stat ->
                                val sweep = stat.percentage * 360f
                                drawArc(
                                    color = stat.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                        }
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Progress indicators list
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categoryStats.forEach { stat ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(stat.color)
                                        )
                                        Text(
                                            text = stat.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${(stat.percentage * 100).toInt()}% (${stat.totalAmountFormatted})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { stat.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = stat.color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Monthly trend bar chart skeleton preview card.
 */
@Composable
fun MonthlyTrendBarChartCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "So sánh Thu - Chi theo tuần",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Bar Chart Visual Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val weeks = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4")
                val heights = listOf(0.4f, 0.7f, 0.3f, 0.9f)
                val incomeHeights = listOf(0.9f, 0.6f, 0.8f, 1.0f)

                weeks.forEachIndexed { i, week ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(100.dp)
                        ) {
                            // Income bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxSize(fraction = incomeHeights[i])
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(SemanticGreen)
                            )
                            // Expense bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxSize(fraction = heights[i])
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(SemanticRed)
                            )
                        }
                        Text(
                            text = week,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SemanticGreen)
                    )
                    Text("Tiền thu", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SemanticRed)
                    )
                    Text("Tiền chi", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Compose Previews
// ----------------------------------------------------

@PreviewLightDark
@Composable
private fun StatisticsScreenPopulatedPreview() {
    FinTrackTheme {
        StatisticsScreen()
    }
}

@PreviewLightDark
@Composable
private fun StatisticsScreenEmptyPreview() {
    FinTrackTheme {
        StatisticsScreenContent(
            selectedPeriodIndex = 0,
            onPeriodSelected = {},
            totalIncome = "0 ₫",
            totalExpense = "0 ₫",
            dailyAverage = "0 ₫",
            categoryStats = emptyList()
        )
    }
}
