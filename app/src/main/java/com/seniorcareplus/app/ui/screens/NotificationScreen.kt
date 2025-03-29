package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seniorcareplus.app.R
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// 通知項目數據類
data class NotificationItem(
    val titleZh: String,
    val titleEn: String,
    val notificationMethodsZh: String,
    val notificationMethodsEn: String,
    val type: NotificationType
) {
    fun getTitle(isChinese: Boolean): String = if (isChinese) titleZh else titleEn
    fun getNotificationMethods(isChinese: Boolean): String = if (isChinese) notificationMethodsZh else notificationMethodsEn
}

enum class NotificationType {
    NORMAL,
    ABNORMAL
}

enum class FilterType {
    ALL,
    NORMAL,
    ABNORMAL
}

@Composable
fun NotificationScreen(navController: NavController) {
    // 检查深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 不再使用SeniorCareTopBar，直接顯示內容
    NotificationContent(navController)
}

@Composable
fun NotificationContent(navController: NavController) {
    // 檢查深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    // 檢查語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 篩選狀態
    var currentFilter by remember { mutableStateOf(FilterType.ALL) }
    
    // 編輯對話框狀態
    var showEditDialog by remember { mutableStateOf(false) }
    var currentEditingNotification by remember { mutableStateOf<NotificationItem?>(null) }
    
    // 模擬通知數據
    val notifications = remember {
        listOf(
            NotificationItem(
                titleZh = "火災警報", 
                titleEn = "Fire Alarm",
                notificationMethodsZh = "推送通知, 電子郵件", 
                notificationMethodsEn = "Push notification, Email", 
                type = NotificationType.ABNORMAL
            ),
            NotificationItem(
                titleZh = "溫度警報", 
                titleEn = "Temperature Alarm",
                notificationMethodsZh = "推送通知, 電子郵件", 
                notificationMethodsEn = "Push notification, Email", 
                type = NotificationType.ABNORMAL
            ),
            NotificationItem(
                titleZh = "入侵警報", 
                titleEn = "Burglary Alarm",
                notificationMethodsZh = "推送通知, 電子郵件", 
                notificationMethodsEn = "Push notification, Email", 
                type = NotificationType.ABNORMAL
            ),
            NotificationItem(
                titleZh = "燃氣警報", 
                titleEn = "Gas Alarm",
                notificationMethodsZh = "推送通知, 電子郵件", 
                notificationMethodsEn = "Push notification, Email", 
                type = NotificationType.ABNORMAL
            ),
            NotificationItem(
                titleZh = "每日報告", 
                titleEn = "Daily Report",
                notificationMethodsZh = "電子郵件", 
                notificationMethodsEn = "Email", 
                type = NotificationType.NORMAL
            ),
            NotificationItem(
                titleZh = "服藥提醒", 
                titleEn = "Medication Reminder",
                notificationMethodsZh = "推送通知", 
                notificationMethodsEn = "Push notification", 
                type = NotificationType.NORMAL
            ),
            NotificationItem(
                titleZh = "系統更新", 
                titleEn = "System Update",
                notificationMethodsZh = "推送通知", 
                notificationMethodsEn = "Push notification", 
                type = NotificationType.NORMAL
            )
        )
    }
    
    // 根據篩選條件過濾通知
    val filteredNotifications = when (currentFilter) {
        FilterType.ALL -> notifications
        FilterType.NORMAL -> notifications.filter { it.type == NotificationType.NORMAL }
        FilterType.ABNORMAL -> notifications.filter { it.type == NotificationType.ABNORMAL }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 標題欄和返回按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 移除返回按钮
                
                Text(
                    text = if (isChineseLanguage) "通知" else "Notifications",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                

            }
            
            // 篩選按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = if (isChineseLanguage) "全部" else "All",
                    isSelected = currentFilter == FilterType.ALL,
                    onClick = { currentFilter = FilterType.ALL }
                )
                FilterButton(
                    text = if (isChineseLanguage) "異常" else "Abnormal",
                    isSelected = currentFilter == FilterType.ABNORMAL,
                    onClick = { currentFilter = FilterType.ABNORMAL }
                )
                FilterButton(
                    text = if (isChineseLanguage) "正常" else "Normal",
                    isSelected = currentFilter == FilterType.NORMAL,
                    onClick = { currentFilter = FilterType.NORMAL }
                )
            }
            
            // 通知列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredNotifications) { notification ->
                    NotificationItemCard(
                        notification = notification,
                        onEditClick = {
                            currentEditingNotification = notification
                            showEditDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // 顯示編輯對話框
    if (showEditDialog && currentEditingNotification != null) {
        NotificationEditDialog(
            notification = currentEditingNotification!!,
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationItem,
    onEditClick: () -> Unit
) {
    // 獲取語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = notification.getTitle(isChineseLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = notification.getNotificationMethods(isChineseLanguage),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isChineseLanguage) "編輯" else "Edit",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun NotificationEditDialog(
    notification: NotificationItem,
    onDismiss: () -> Unit
) {
    // 狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    var pushEnabled by remember { mutableStateOf(notification.getNotificationMethods(isChineseLanguage).contains(if (isChineseLanguage) "推送" else "Push")) }
    var emailEnabled by remember { mutableStateOf(notification.getNotificationMethods(isChineseLanguage).contains(if (isChineseLanguage) "電子" else "Email")) }
    var priority by remember { mutableStateOf(if (notification.type == NotificationType.ABNORMAL) if (isChineseLanguage) "高" else "High" else if (isChineseLanguage) "普通" else "Normal") }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        color = Color(0x88000000)
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clickable { /* 防止點擊穿透 */ },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isChineseLanguage) "編輯通知設定" else "Edit Notification Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = notification.getTitle(isChineseLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isChineseLanguage) "通知方式" else "Notification Methods",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .toggleable(
                            value = pushEnabled,
                            onValueChange = { pushEnabled = it }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = pushEnabled,
                        onCheckedChange = { pushEnabled = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = if (isChineseLanguage) "推送通知" else "Push Notification",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .toggleable(
                            value = emailEnabled,
                            onValueChange = { emailEnabled = it }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = emailEnabled,
                        onCheckedChange = { emailEnabled = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = if (isChineseLanguage) "電子郵件" else "Email",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isChineseLanguage) "優先級" else "Priority",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = priority == (if (isChineseLanguage) "高" else "High"),
                            onClick = { priority = if (isChineseLanguage) "高" else "High" }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = priority == (if (isChineseLanguage) "高" else "High"),
                        onClick = { priority = if (isChineseLanguage) "高" else "High" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = if (isChineseLanguage) "高優先級" else "High Priority",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = priority == (if (isChineseLanguage) "普通" else "Normal"),
                            onClick = { priority = if (isChineseLanguage) "普通" else "Normal" }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = priority == (if (isChineseLanguage) "普通" else "Normal"),
                        onClick = { priority = if (isChineseLanguage) "普通" else "Normal" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = if (isChineseLanguage) "普通優先級" else "Normal Priority",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isChineseLanguage) "啟用通知" else "Enable Notifications",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChineseLanguage) "接收此類型通知" else "Receive this notification type",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = true,
                        onCheckedChange = { },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isChineseLanguage) "保存" else "Save",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
} 