package com.fintrack.app.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed
import com.fintrack.app.ui.util.CategoryIconHelper
import com.fintrack.app.ui.viewmodel.AppViewModelProvider

/**
 * Stateful entry composable for Settings Screen.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SettingsScreenContent(
            isDarkTheme = uiState.isDarkTheme,
            onToggleTheme = { viewModel.toggleDarkTheme(it) },
            selectedCurrency = uiState.currencyDisplayName,
            onCurrencyClick = { viewModel.openCurrencyDialog() },
            isDailyReminderEnabled = uiState.isDailyReminderEnabled,
            onToggleDailyReminder = { viewModel.toggleDailyReminder(it) },
            reminderTime = uiState.reminderTimeFormatted,
            categoryCountText = if (uiState.categories.isNotEmpty()) "${uiState.categories.size} danh mục" else "12 danh mục",
            onReminderTimeClick = {
                val currentHour = uiState.userPreferences.reminderHour
                val currentMinute = uiState.userPreferences.reminderMinute
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        viewModel.setReminderTime(hourOfDay, minute)
                    },
                    currentHour,
                    currentMinute,
                    true
                ).show()
            },
            onCategoryManagementClick = { viewModel.openCategoryDialog() },
            onResetOnboardingClick = { viewModel.openResetOnboardingDialog() },
            onClearDataClick = { viewModel.openClearDataDialog() },
            onTestNotificationClick = { viewModel.triggerTestNotification() }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }

    // Category Management Dialog
    if (uiState.showCategoryDialog) {
        CategoryManagementDialog(
            categories = uiState.categories,
            onDismiss = { viewModel.dismissCategoryDialog() }
        )
    }

    // Currency Selection Dialog
    if (uiState.showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = uiState.userPreferences.currency,
            onCurrencySelected = { viewModel.selectCurrency(it) },
            onDismiss = { viewModel.dismissCurrencyDialog() }
        )
    }

    // Reset Onboarding Dialog
    if (uiState.showResetOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetOnboardingDialog() },
            title = { Text("Đặt lại Onboarding?") },
            text = { Text("Màn hình hướng dẫn giới thiệu sẽ xuất hiện lại khi mở ứng dụng.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmResetOnboarding() }) {
                    Text("Đồng ý")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetOnboardingDialog() }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Clear All Data Dialog
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearDataDialog() },
            title = { Text("Xóa toàn bộ dữ liệu?", color = MaterialTheme.colorScheme.error) },
            text = { Text("Toàn bộ lịch sử giao dịch và cài đặt sẽ được đặt lại về mặc định. Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmClearData() }) {
                    Text("Xóa hết", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearDataDialog() }) {
                    Text("Hủy")
                }
            }
        )
    }
}

/**
 * Category Management Dialog showing categorized list of all seeded categories.
 */
@Composable
fun CategoryManagementDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit
) {
    val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }
    val incomeCategories = categories.filter { it.type == CategoryType.INCOME }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Danh mục hệ thống (${categories.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "CHI TIÊU (${expenseCategories.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SemanticRed,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(expenseCategories) { cat ->
                    CategoryItemRow(cat)
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "THU NHẬP (${incomeCategories.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SemanticGreen,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                items(incomeCategories) { cat ->
                    CategoryItemRow(cat)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

/**
 * Single Category Item Row inside Category Management Dialog.
 */
@Composable
private fun CategoryItemRow(category: CategoryEntity) {
    val icon = CategoryIconHelper.getIconByName(category.iconKey)
    val color = CategoryIconHelper.parseColor(category.colorKey)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.name,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (category.type == CategoryType.EXPENSE) "Chi" else "Thu",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Currency Selection Dialog with Radio Buttons.
 */
@Composable
fun CurrencySelectionDialog(
    selectedCurrency: CurrencyConfig,
    onCurrencySelected: (CurrencyConfig) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Chọn đơn vị tiền tệ", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                CurrencyConfig.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (currency == selectedCurrency),
                                onClick = { onCurrencySelected(currency) }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currency == selectedCurrency),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = currency.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currency == selectedCurrency) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

/**
 * Stateless pure UI component for Settings Screen.
 */
@Composable
fun SettingsScreenContent(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    selectedCurrency: String,
    onCurrencyClick: () -> Unit,
    isDailyReminderEnabled: Boolean,
    onToggleDailyReminder: (Boolean) -> Unit,
    reminderTime: String,
    categoryCountText: String,
    onReminderTimeClick: () -> Unit,
    onCategoryManagementClick: () -> Unit,
    onResetOnboardingClick: () -> Unit,
    onClearDataClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Tùy chỉnh hệ thống",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Cài đặt & Bảo mật",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Bảo mật",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Bảo mật",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onTestNotificationClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Thử thông báo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Local Vault Banner
            item {
                LocalVaultBanner()
            }

            // GIAO DIỆN & HIỂN THỊ
            item {
                SettingsSection(title = "GIAO DIỆN & HIỂN THỊ") {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Giao diện tối (Dark Theme)",
                        subtitle = "Chuyển đổi tông màu tối để bảo vệ mắt",
                        trailingContent = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsItem(
                        icon = Icons.Default.Paid,
                        title = "Đơn vị tiền tệ",
                        subtitle = null,
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = selectedCurrency,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Chọn đơn vị",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = onCurrencyClick
                    )
                }
            }

            // THÔNG BÁO
            item {
                SettingsSection(title = "THÔNG BÁO") {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Nhắc nhở ghi sổ hàng ngày",
                        subtitle = "Nhận thông báo nhắc ghi chép chi tiêu",
                        trailingContent = {
                            Switch(
                                checked = isDailyReminderEnabled,
                                onCheckedChange = onToggleDailyReminder,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    )
                    if (isDailyReminderEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        SettingsItem(
                            icon = Icons.Default.AccessTime,
                            title = "Thời gian nhắc nhở",
                            subtitle = null,
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = reminderTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Chọn giờ",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = onReminderTimeClick
                        )
                    }
                }
            }

            // QUẢN LÝ DỮ LIỆU
            item {
                SettingsSection(title = "QUẢN LÝ DỮ LIỆU") {
                    SettingsItem(
                        icon = Icons.Default.Category,
                        title = "Quản lý danh mục",
                        subtitle = null,
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = categoryCountText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Xem danh mục",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = onCategoryManagementClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsItem(
                        icon = Icons.Default.RestartAlt,
                        title = "Đặt lại Onboarding",
                        subtitle = null,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Đặt lại Onboarding",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = onResetOnboardingClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "Xóa toàn bộ dữ liệu",
                        titleColor = MaterialTheme.colorScheme.error,
                        subtitle = null,
                        trailingContent = null,
                        onClick = onClearDataClick
                    )
                }
            }

            // THÔNG TIN ỨNG DỤNG
            item {
                SettingsSection(title = "THÔNG TIN ỨNG DỤNG") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Phiên bản",
                        subtitle = null,
                        trailingContent = {
                            Text(
                                text = "1.0.0 (Build 2026)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Local-First Vault security highlight banner.
 */
@Composable
fun LocalVaultBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Bảo mật",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Local-First Vault",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dữ liệu của bạn được lưu trữ an toàn 100% cục bộ trên thiết bị qua Room Database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Reusable Section Card container.
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * Generic row item in settings.
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailingContent?.invoke()
    }
}

// ----------------------------------------------------
// Compose Previews
// ----------------------------------------------------

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    FinTrackTheme {
        SettingsScreenContent(
            isDarkTheme = false,
            onToggleTheme = {},
            selectedCurrency = "VND (₫)",
            onCurrencyClick = {},
            isDailyReminderEnabled = true,
            onToggleDailyReminder = {},
            reminderTime = "20:00",
            categoryCountText = "12 danh mục",
            onReminderTimeClick = {},
            onCategoryManagementClick = {},
            onResetOnboardingClick = {},
            onClearDataClick = {},
            onTestNotificationClick = {}
        )
    }
}
