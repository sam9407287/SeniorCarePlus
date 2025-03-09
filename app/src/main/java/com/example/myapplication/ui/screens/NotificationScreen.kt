package com.example.myapplication.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.ThemeManager

// 通知項目數據類
data class NotificationItem(
    val title: String,
    val notificationMethods: String,
    val type: NotificationType
)

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
    // 篩選狀態
    var currentFilter by remember { mutableStateOf(FilterType.ALL) }
    
    // 編輯對話框狀態
    var showEditDialog by remember { mutableStateOf(false) }
    var currentEditingNotification by remember { mutableStateOf<NotificationItem?>(null) }
    
    // 模擬通知數據
    val notifications = remember {
        listOf(
            NotificationItem("Fire Alarm", "Push notification, Email", NotificationType.ABNORMAL),
            NotificationItem("Temperature Alarm", "Push notification, Email", NotificationType.ABNORMAL),
            NotificationItem("Burglary Alarm", "Push notification, Email", NotificationType.ABNORMAL),
            NotificationItem("Gas Alarm", "Push notification, Email", NotificationType.ABNORMAL),
            NotificationItem("Daily Report", "Email", NotificationType.NORMAL),
            NotificationItem("Medication Reminder", "Push notification", NotificationType.NORMAL),
            NotificationItem("System Update", "Push notification", NotificationType.NORMAL)
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
            // 標題
            Text(
                text = "通知",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 篩選按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = "All",
                    isSelected = currentFilter == FilterType.ALL,
                    onClick = { currentFilter = FilterType.ALL }
                )
                FilterButton(
                    text = "Abnormal",
                    isSelected = currentFilter == FilterType.ABNORMAL,
                    onClick = { currentFilter = FilterType.ABNORMAL }
                )
                FilterButton(
                    text = "Normal",
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
                    text = notification.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = notification.notificationMethods,
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
                    text = "Edit",
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
    var pushEnabled by remember { mutableStateOf(notification.notificationMethods.contains("Push")) }
    var emailEnabled by remember { mutableStateOf(notification.notificationMethods.contains("Email")) }
    var priority by remember { mutableStateOf(if (notification.type == NotificationType.ABNORMAL) "High" else "Normal") }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .clickable(onClick = onDismiss),
        color = Color.Transparent
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clickable { /* 防止點擊穿透 */ },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "編輯通知設定",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = notification.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "通知方式",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
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
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4169E1))
                    )
                    Text(
                        text = "推送通知",
                        modifier = Modifier.padding(start = 8.dp)
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
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4169E1))
                    )
                    Text(
                        text = "電子郵件",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "優先級",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = priority == "High",
                            onClick = { priority = "High" }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = priority == "High",
                        onClick = { priority = "High" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4169E1))
                    )
                    Text(
                        text = "高優先級",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = priority == "Normal",
                            onClick = { priority = "Normal" }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = priority == "Normal",
                        onClick = { priority = "Normal" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4169E1))
                    )
                    Text(
                        text = "普通優先級",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "啟用通知",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "接收此類型通知")
                    Switch(
                        checked = true,
                        onCheckedChange = { },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4169E1)
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
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "取消",
                            color = Color.Black
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4169E1)
                        )
                    ) {
                        Text(
                            text = "保存",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
} 