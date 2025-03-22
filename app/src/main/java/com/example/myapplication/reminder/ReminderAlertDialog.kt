package com.example.myapplication.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.myapplication.ui.screens.ReminderItem
import com.example.myapplication.ui.screens.ReminderType
import com.example.myapplication.ui.theme.DarkCardBackground
import com.example.myapplication.ui.theme.LightCardBackground
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReminderAlertDialog(
    reminder: ReminderItem,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) DarkCardBackground else LightCardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 標題列
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChineseLanguage) "提醒時間到" else "Reminder Alert",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isChineseLanguage) "關閉" else "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 提醒圖標
                Box(
                    modifier = Modifier
                        .size(72.dp)
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
                            modifier = Modifier.size(40.dp)
                        )
                        ReminderType.WATER -> Icon(
                            imageVector = Icons.Default.WaterDrop, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(40.dp)
                        )
                        ReminderType.MEAL -> Icon(
                            imageVector = Icons.Default.Restaurant, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(40.dp)
                        )
                        ReminderType.HEART_RATE -> Icon(
                            imageVector = Icons.Default.Favorite, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(40.dp)
                        )
                        ReminderType.TEMPERATURE -> Icon(
                            imageVector = Icons.Default.Thermostat, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(40.dp)
                        )
                        ReminderType.GENERAL -> Icon(
                            imageVector = Icons.Default.Timer, 
                            contentDescription = null,
                            tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 提醒標題
                Text(
                    text = reminder.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 當前時間
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                Text(
                    text = currentTime,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onSnooze,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "稍後提醒" else "Snooze",
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.size(16.dp))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "確定" else "OK",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
