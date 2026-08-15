package com.fintrack.app.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticRed

/**
 * Stateful entry composable for Settings Screen.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("VND (₫)") }
    var isDailyReminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("20:00") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    SettingsScreenContent(
        isDarkTheme = isDarkTheme,
        onToggleTheme = { isDarkTheme = it },
        selectedCurrency = selectedCurrency,
        onCurrencyClick = { /* Currency dialog in later milestone */ },
        isDailyReminderEnabled = isDailyReminderEnabled,
        onToggleDailyReminder = { isDailyReminderEnabled = it },
        reminderTime = reminderTime,
        onReminderTimeClick = { /* Time picker in later milestone */ },
        onCategoryManagementClick = { /* Navigate to category management */ },
        onResetOnboardingClick = { showResetDialog = true },
        onClearDataClick = { showClearDataDialog = true },
        modifier = modifier
    )

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Đặt lại Onboarding?") },
            text = { Text("Màn hình hướng dẫn sẽ xuất hiện lại trong lần mở ứng dụng kế tiếp.") },
            confirmButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Đồng ý")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Xóa toàn bộ dữ liệu?", color = MaterialTheme.colorScheme.error) },
            text = { Text("Toàn bộ giao dịch và cài đặt sẽ bị xóa vĩnh viễn khỏi thiết bị. Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Xóa dữ liệu", color = SemanticRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

/**
 * Stateless pure UI component for Settings Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    selectedCurrency: String,
    onCurrencyClick: () -> Unit,
    isDailyReminderEnabled: Boolean,
    onToggleDailyReminder: (Boolean) -> Unit,
    reminderTime: String,
    onReminderTimeClick: () -> Unit,
    onCategoryManagementClick: () -> Unit,
    onResetOnboardingClick: () -> Unit,
    onClearDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Cài đặt & Bảo mật",
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
            // 1. Local Vault Card (Stitch Design)
            item {
                LocalVaultCard()
            }

            // 2. Section: Giao diện
            item {
                SettingsSection(title = "GIAO DIỆN & HIỂN THỊ") {
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Giao diện tối (Dark Theme)",
                        subtitle = "Chuyển đổi tông màu tối để bảo vệ mắt",
                        checked = isDarkTheme,
                        onCheckedChange = onToggleTheme
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsNavigationItem(
                        icon = Icons.Default.Paid,
                        title = "Đơn vị tiền tệ",
                        value = selectedCurrency,
                        onClick = onCurrencyClick
                    )
                }
            }

            // 3. Section: Nhắc nhở & Thông báo
            item {
                SettingsSection(title = "THÔNG BÁO") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Nhắc nhở ghi sổ hàng ngày",
                        subtitle = "Nhận thông báo nhắc ghi chép chi tiêu",
                        checked = isDailyReminderEnabled,
                        onCheckedChange = onToggleDailyReminder
                    )
                    if (isDailyReminderEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavigationItem(
                            icon = Icons.Default.AccessTime,
                            title = "Thời gian nhắc nhở",
                            value = reminderTime,
                            onClick = onReminderTimeClick
                        )
                    }
                }
            }

            // 4. Section: Dữ liệu & Danh mục
            item {
                SettingsSection(title = "QUẢN LÝ DỮ LIỆU") {
                    SettingsNavigationItem(
                        icon = Icons.Default.Category,
                        title = "Quản lý danh mục",
                        value = "12 danh mục",
                        onClick = onCategoryManagementClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsNavigationItem(
                        icon = Icons.Default.RestartAlt,
                        title = "Đặt lại Onboarding",
                        value = null,
                        onClick = onResetOnboardingClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsNavigationItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Xóa toàn bộ dữ liệu",
                        value = null,
                        titleColor = SemanticRed,
                        iconTint = SemanticRed,
                        onClick = onClearDataClick
                    )
                }
            }

            // 5. Section: Giới thiệu
            item {
                SettingsSection(title = "THÔNG TIN ỨNG DỤNG") {
                    SettingsNavigationItem(
                        icon = Icons.Default.Info,
                        title = "Phiên bản",
                        value = "v1.0.0 (Build 1)",
                        onClick = {}
                    )
                }
            }
        }
    }
}

/**
 * Local Vault Status Card showing local-first SQLite/Room storage.
 */
@Composable
fun LocalVaultCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Settings Navigation Row Item.
 */
@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Settings Switch Row Item.
 */
@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

// ----------------------------------------------------
// Compose Previews
// ----------------------------------------------------

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    FinTrackTheme {
        SettingsScreen()
    }
}
