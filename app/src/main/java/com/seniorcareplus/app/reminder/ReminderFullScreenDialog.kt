package com.seniorcareplus.app.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seniorcareplus.app.ui.screens.ReminderItem
import com.seniorcareplus.app.ui.screens.ReminderType
import com.seniorcareplus.app.ui.theme.DarkCardBackground
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.LightCardBackground
import com.seniorcareplus.app.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReminderFullScreenDialog(
    reminder: ReminderItem,
    onDismiss: () -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 使用 BackHandler 來處理系統返回按鈕
    BackHandler(enabled = true) {
        onDismiss()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 提醒圖標
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkTheme) 
                                reminder.type.darkColor.copy(alpha = 0.2f) 
                            else 
                                reminder.type.color.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (reminder.type) {
                        ReminderType.MEDICATION -> Icon(
                            imageVector = Icons.Default.Medication, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(72.dp)
                        )
                        ReminderType.WATER -> Icon(
                            imageVector = Icons.Default.WaterDrop, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(72.dp)
                        )
                        ReminderType.MEAL -> Icon(
                            imageVector = Icons.Default.Restaurant, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(72.dp)
                        )
                        ReminderType.HEART_RATE -> Icon(
                            imageVector = Icons.Default.Favorite, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(72.dp)
                        )
                        ReminderType.TEMPERATURE -> Icon(
                            imageVector = Icons.Default.Thermostat, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(72.dp)
                        )
                        ReminderType.GENERAL -> Icon(
                            imageVector = Icons.Default.Timer, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 提醒標題
                Text(
                    text = if (isChineseLanguage) "提醒時間到" else "Reminder Alert",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 提醒內容
                Text(
                    text = reminder.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 當前時間
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                Text(
                    text = currentTime,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // 確認按鈕
                Button(
                    onClick = {
                        // 確保點擊事件只關閉對話框，不會導致頁面導航遷移
                        // 直接調用 onDismiss 只關閉對話框不跳轉
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        text = if (isChineseLanguage) "確定" else "OK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
