package com.fintrack.app.ui.screens.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed

data class CategoryItemUi(
    val id: Long,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * Stateful entry composable for Add/Edit Transaction Screen.
 */
@Composable
fun AddEditTransactionScreen(
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeIndex by remember { mutableIntStateOf(0) } // 0: Expense, 1: Income
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(1L) }
    var dateText by remember { mutableStateOf("15/08/2026") }
    var noteText by remember { mutableStateOf("") }

    val expenseCategories = listOf(
        CategoryItemUi(1L, "Ăn uống", Icons.Default.Fastfood, Color(0xFFFFA000)),
        CategoryItemUi(2L, "Mua sắm", Icons.Default.ShoppingCart, Color(0xFF7B1FA2)),
        CategoryItemUi(3L, "Đi lại", Icons.Default.DirectionsCar, Color(0xFF0288D1)),
        CategoryItemUi(4L, "Nhà cửa", Icons.Default.Home, Color(0xFF5D4037)),
        CategoryItemUi(5L, "Giải trí", Icons.Default.SportsEsports, Color(0xFFE91E63)),
        CategoryItemUi(6L, "Y tế", Icons.Default.LocalHospital, Color(0xFFD32F2F)),
        CategoryItemUi(7L, "Giáo dục", Icons.Default.School, Color(0xFF388E3C))
    )

    val incomeCategories = listOf(
        CategoryItemUi(8L, "Tiền lương", Icons.Default.Paid, SemanticGreen),
        CategoryItemUi(9L, "Thưởng", Icons.Default.Work, Color(0xFF00796B)),
        CategoryItemUi(10L, "Đầu tư", Icons.Default.Paid, Color(0xFF1976D2))
    )

    val activeCategories = if (selectedTypeIndex == 0) expenseCategories else incomeCategories

    AddEditTransactionScreenContent(
        isEditMode = transactionId != null,
        selectedTypeIndex = selectedTypeIndex,
        onTypeSelected = {
            selectedTypeIndex = it
            selectedCategoryId = if (it == 0) expenseCategories.first().id else incomeCategories.first().id
        },
        amountText = amountText,
        onAmountChange = { amountText = it },
        categories = activeCategories,
        selectedCategoryId = selectedCategoryId,
        onCategorySelected = { selectedCategoryId = it },
        dateText = dateText,
        onDateClick = { /* Date picker dialog in later milestone */ },
        noteText = noteText,
        onNoteChange = { noteText = it },
        onSaveClick = onNavigateBack,
        onCancelClick = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless pure UI component for Add/Edit Transaction Screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionScreenContent(
    isEditMode: Boolean,
    selectedTypeIndex: Int,
    onTypeSelected: (Int) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    categories: List<CategoryItemUi>,
    selectedCategoryId: Long,
    onCategorySelected: (Long) -> Unit,
    dateText: String,
    onDateClick: () -> Unit,
    noteText: String,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeLabels = listOf("Khoản chi", "Khoản thu")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isEditMode) "Chỉnh sửa giao dịch" else "Thêm giao dịch mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onCancelClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
            // 1. Transaction Type Toggle (Segmented Button)
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    typeLabels.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = typeLabels.size),
                            onClick = { onTypeSelected(index) },
                            selected = index == selectedTypeIndex,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = if (selectedTypeIndex == 0) SemanticRed.copy(alpha = 0.15f) else SemanticGreen.copy(alpha = 0.15f),
                                activeContentColor = if (selectedTypeIndex == 0) SemanticRed else SemanticGreen
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Large Amount Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Số tiền giao dịch (₫)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = onAmountChange,
                            placeholder = {
                                Text(
                                    "0",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (selectedTypeIndex == 0) SemanticRed else SemanticGreen
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Category Selector Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "Chọn danh mục",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { category ->
                                val isSelected = category.id == selectedCategoryId
                                CategoryGridChip(
                                    category = category,
                                    isSelected = isSelected,
                                    onClick = { onCategorySelected(category.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Date & Note Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Date Picker Field
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ngày giao dịch") },
                            trailingIcon = {
                                IconButton(onClick = onDateClick) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Chọn ngày",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onDateClick)
                        )

                        // Note Field
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = onNoteChange,
                            label = { Text("Ghi chú (tùy chọn)") },
                            placeholder = { Text("Ví dụ: Ăn trưa cùng đồng nghiệp...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 5. Action Buttons (Save & Cancel)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "Cập nhật giao dịch" else "Lưu giao dịch",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Hủy bỏ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Category Chip for Add/Edit Selection Grid.
 */
@Composable
fun CategoryGridChip(
    category: CategoryItemUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, category.color, RoundedCornerShape(20.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface
            )

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Đã chọn",
                    tint = category.color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// Compose Previews
// ----------------------------------------------------

@PreviewLightDark
@Composable
private fun AddEditTransactionExpensePreview() {
    FinTrackTheme {
        AddEditTransactionScreen(
            transactionId = null,
            onNavigateBack = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun AddEditTransactionEditModePreview() {
    FinTrackTheme {
        AddEditTransactionScreen(
            transactionId = 123L,
            onNavigateBack = {}
        )
    }
}
