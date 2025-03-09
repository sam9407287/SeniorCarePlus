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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.DarkCardBackground
import com.example.myapplication.ui.theme.LightCardBackground
import com.example.myapplication.ui.theme.ThemeManager
import com.example.myapplication.ui.theme.LanguageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 提醒类型
enum class ReminderType(val zhLabel: String, val enLabel: String, val color: Color, val darkColor: Color) {
    MEDICATION("服藥", "Medication", Color(0xFF1976D2), Color(0xFF5E92F3)),
    WATER("喝水", "Drink", Color(0xFF03A9F4), Color(0xFF64B5F6)),
    MEAL("用餐", "Meal", Color(0xFFF57C00), Color(0xFFFFB74D)),
    HEART_RATE("心率", "Heart Rate", Color(0xFFE91E63), Color(0xFFF48FB1)),
    TEMPERATURE("體溫", "Temperature", Color(0xFF7B1FA2), Color(0xFFAB47BC)),
    GENERAL("一般提醒", "General", Color(0xFF009688), Color(0xFF4DB6AC))
}

// 根據語言獲取標籤
val ReminderType.label: String
    get() = if (LanguageManager.isChineseLanguage) this.zhLabel else this.enLabel

// 提醒项目
data class ReminderItem(
    val id: Int,
    val title: String,
    val time: String,
    val days: List<String>,
    val type: ReminderType
)

@Composable
fun TimerScreen(navController: NavController) {
    // 判断是否为深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 提醒列表
    val reminders = remember {
        mutableStateListOf(
            ReminderItem(
                id = 1,
                title = "早晨服藥",
                time = "08:00",
                days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                type = ReminderType.MEDICATION
            ),
            ReminderItem(
                id = 2,
                title = "喝水提醒",
                time = "10:30",
                days = listOf("週一", "週二", "週三", "週四", "週五"),
                type = ReminderType.WATER
            ),
            ReminderItem(
                id = 3,
                title = "測量心率",
                time = "14:00",
                days = listOf("週一", "週三", "週五"),
                type = ReminderType.HEART_RATE
            ),
            ReminderItem(
                id = 4,
                title = "晚餐時間",
                time = "18:30",
                days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                type = ReminderType.MEAL
            ),
            ReminderItem(
                id = 5,
                title = "晚上服藥",
                time = "21:00",
                days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                type = ReminderType.MEDICATION
            )
        )
    }
    
    // 狀態
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var currentEditingReminder by remember { mutableStateOf<ReminderItem?>(null) }
    
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "定時提醒",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // 主題切換按鈕
                IconButton(
                    onClick = { ThemeManager.toggleTheme() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "切換主題",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 提醒列表
            if (reminders.isEmpty()) {
                // 空列表提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暫無提醒事項",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(reminders) { reminder ->
                        ReminderItemCard(
                            reminder = reminder,
                            isDarkTheme = isDarkTheme,
                            onEdit = {
                                currentEditingReminder = reminder
                                showAddReminderDialog = true
                            },
                            onDelete = {
                                reminders.remove(reminder)
                            },
                            onToggle = { isActive ->
                                // TODO: 實現啟用/禁用提醒的功能
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // 添加按钮
        FloatingActionButton(
            onClick = {
                currentEditingReminder = null
                showAddReminderDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加提醒",
                tint = Color.White
            )
        }
    }
    
    // 添加/编辑提醒对话框
    if (showAddReminderDialog) {
        AddEditReminderDialog(
            reminder = currentEditingReminder,
            isDarkTheme = isDarkTheme,
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { title, time, days, type ->
                if (currentEditingReminder != null) {
                    // 编辑现有提醒
                    val index = reminders.indexOfFirst { it.id == currentEditingReminder!!.id }
                    if (index != -1) {
                        reminders[index] = currentEditingReminder!!.copy(
                            title = title,
                            time = time,
                            days = days,
                            type = type
                        )
                    }
                } else {
                    // 添加新提醒
                    val newId = reminders.maxOfOrNull { it.id }?.plus(1) ?: 1
                    reminders.add(
                        ReminderItem(
                            id = newId,
                            title = title,
                            time = time,
                            days = days,
                            type = type
                        )
                    )
                }
                showAddReminderDialog = false
            }
        )
    }
}

@Composable
fun ReminderItemCard(
    reminder: ReminderItem,
    isDarkTheme: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DarkCardBackground else LightCardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                        modifier = Modifier.size(24.dp)
                    )
                    ReminderType.WATER -> Icon(
                        imageVector = Icons.Default.WaterDrop, 
                        contentDescription = null,
                        tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                    ReminderType.MEAL -> Icon(
                        imageVector = Icons.Default.Restaurant, 
                        contentDescription = null,
                        tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                    ReminderType.HEART_RATE -> Icon(
                        imageVector = Icons.Default.Favorite, 
                        contentDescription = null,
                        tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                    ReminderType.TEMPERATURE -> Icon(
                        imageVector = Icons.Default.MedicalServices, 
                        contentDescription = null,
                        tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                    ReminderType.GENERAL -> Icon(
                        imageVector = Icons.Default.Timer, 
                        contentDescription = null,
                        tint = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = reminder.time,
                    fontSize = 20.sp,
                    color = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 显示选中的天数
                Text(
                    text = reminder.days.joinToString(" · "),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // 按钮
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // 启用/禁用开关
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        isEnabled = it
                        onToggle(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isDarkTheme) reminder.type.darkColor else reminder.type.color,
                        checkedTrackColor = if (isDarkTheme) 
                            reminder.type.darkColor.copy(alpha = 0.5f) 
                        else 
                            reminder.type.color.copy(alpha = 0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    // 编辑按钮
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // 删除按钮
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    selectedColor: Color,
    isDarkTheme: Boolean,
    isChineseLanguage: Boolean,
    onCancel: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )
    
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isChineseLanguage) "選擇時間" else "Select Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        selectorColor = selectedColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorBorderColor = selectedColor,
                        periodSelectorSelectedContainerColor = selectedColor,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 確認和取消按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 取消按鈕
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 確認按鈕
                    Button(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "確認" else "Confirm",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayPickerDialog(
    initialSelectedDays: List<String>,
    daysOfWeek: List<String>,
    selectedColor: Color,
    isDarkTheme: Boolean,
    isChineseLanguage: Boolean,
    onCancel: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val selectedDays = remember { mutableStateListOf<String>().apply {
        addAll(initialSelectedDays)
    }}
    
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isChineseLanguage) "選擇重複日期" else "Select Repeat Days",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                // 周几选择
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // 快速選擇按鈕
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 全選按鈕
                        Button(
                            onClick = { 
                                selectedDays.clear()
                                selectedDays.addAll(daysOfWeek) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedColor.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Text(
                                text = if (isChineseLanguage) "全選" else "All",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        
                        // 工作日按鈕
                        Button(
                            onClick = { 
                                selectedDays.clear()
                                selectedDays.addAll(daysOfWeek.take(5)) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedColor.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = if (isChineseLanguage) "工作日" else "Weekdays",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        
                        // 週末按鈕
                        Button(
                            onClick = { 
                                selectedDays.clear()
                                selectedDays.addAll(daysOfWeek.takeLast(2)) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedColor.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (isChineseLanguage) "週末" else "Weekend",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                    
                    // 改進日期選擇按鈕
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // 使用更小的按鈕並減少間距
                        val dayTexts = if (isChineseLanguage) {
                            listOf("一", "二", "三", "四", "五", "六", "日")
                        } else {
                            listOf("M", "T", "W", "T", "F", "S", "S")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (i in 0 until 7) {
                                val day = daysOfWeek[i]
                                val isSelected = selectedDays.contains(day)
                                val buttonSize = 32.dp // 更小的按鈕尺寸
                                
                                Button(
                                    onClick = {
                                        if (isSelected) {
                                            selectedDays.remove(day)
                                        } else {
                                            selectedDays.add(day)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) 
                                            selectedColor 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier
                                        .size(buttonSize)
                                        .padding(horizontal = 0.dp) // 最小間距
                                ) {
                                    Text(
                                        text = dayTexts[i],
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp // 更小的字體
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 確認和取消按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 取消按鈕
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 確認按鈕
                    Button(
                        onClick = {
                            onConfirm(selectedDays.toList())
                        },
                        enabled = selectedDays.isNotEmpty(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedColor,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "確認" else "Confirm",
                            color = if (selectedDays.isNotEmpty()) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
    reminder: ReminderItem?,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>, ReminderType) -> Unit
) {
    // 檢查語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 状态
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var selectedType by remember { mutableStateOf(reminder?.type ?: ReminderType.GENERAL) }
    
    // 時間選擇
    var showTimePicker by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf(reminder?.time?.split(":")?.get(0)?.toIntOrNull() ?: 8) }
    var minute by remember { mutableStateOf(reminder?.time?.split(":")?.get(1)?.toIntOrNull() ?: 0) }
    var timeFormatted by remember { mutableStateOf(
        reminder?.time ?: "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    ) }
    
    // 日期选择
    var showDayPicker by remember { mutableStateOf(false) }
    val daysOfWeek = if (isChineseLanguage) {
        listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日")
    } else {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
    var selectedDays by remember { mutableStateOf(
        if (reminder != null) reminder.days else daysOfWeek.take(5)
    ) }
    
    // 顯示時間選擇器對話框
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            selectedColor = if (isDarkTheme) selectedType.darkColor else selectedType.color,
            isDarkTheme = isDarkTheme,
            isChineseLanguage = isChineseLanguage,
            onCancel = { showTimePicker = false },
            onConfirm = { selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                timeFormatted = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                showTimePicker = false
            }
        )
    }
    
    // 顯示日期選擇器對話框
    if (showDayPicker) {
        DayPickerDialog(
            initialSelectedDays = selectedDays,
            daysOfWeek = daysOfWeek,
            selectedColor = if (isDarkTheme) selectedType.darkColor else selectedType.color,
            isDarkTheme = isDarkTheme,
            isChineseLanguage = isChineseLanguage,
            onCancel = { showDayPicker = false },
            onConfirm = { days ->
                selectedDays = days
                showDayPicker = false
            }
        )
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题
                Text(
                    text = if (reminder != null) {
                        if (isChineseLanguage) "編輯提醒" else "Edit Reminder"
                    } else {
                        if (isChineseLanguage) "添加提醒" else "Add Reminder"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isChineseLanguage) "提醒標題" else "Reminder Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 类型选择
                Text(
                    text = if (isChineseLanguage) "提醒類型" else "Reminder Type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReminderType.values().forEach { type ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type }
                                .padding(4.dp)
                                .background(
                                    if (selectedType == type) 
                                        if (isDarkTheme) type.darkColor.copy(alpha = 0.2f) else type.color.copy(alpha = 0.1f)
                                    else 
                                        Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        ) {
                            // 图标
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDarkTheme) type.darkColor.copy(alpha = 0.2f) else type.color.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (type) {
                                    ReminderType.MEDICATION -> Icon(
                                        imageVector = Icons.Default.Medication, 
                                        contentDescription = null,
                                        tint = if (isDarkTheme) type.darkColor else type.color
                                    )
                                    ReminderType.WATER -> Icon(
                                        imageVector = Icons.Default.WaterDrop, 
                                        contentDescription = null,
                                        tint = if (isDarkTheme) type.darkColor else type.color
                                    )
                                    ReminderType.MEAL -> Icon(
                                        imageVector = Icons.Default.Restaurant, 
                                        contentDescription = null,
                                        tint = if (isDarkTheme) type.darkColor else type.color
                                    )
                                    ReminderType.HEART_RATE -> Icon(
                                        imageVector = Icons.Default.Favorite, 
                                        contentDescription = null,
                                        tint = if (isDarkTheme) type.darkColor else type.color
                                    )
                                    ReminderType.TEMPERATURE -> Icon(
                                        imageVector = Icons.Default.MedicalServices, 
                                        contentDescription = null,
                                        tint = if (isDarkTheme) type.darkColor else type.color
                                    )
                                    ReminderType.GENERAL -> Icon(
                                        imageVector = Icons.Default.Timer, 
                                        contentDescription = null,
                                        tint = if (isDarkTheme) type.darkColor else type.color
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 文字
                            Text(
                                text = type.label,
                                fontSize = 12.sp,
                                color = if (isDarkTheme) type.darkColor else type.color,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 时间选择
                Text(
                    text = if (isChineseLanguage) "提醒時間" else "Reminder Time",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 時間選擇按鈕
                Button(
                    onClick = { showTimePicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) selectedType.darkColor else selectedType.color
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 重复日期选择
                Text(
                    text = if (isChineseLanguage) "重複日期" else "Repeat Days",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 日期選擇按鈕
                Button(
                    onClick = { showDayPicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedDays.joinToString(" · "),
                        fontSize = 14.sp,
                        color = if (isDarkTheme) selectedType.darkColor else selectedType.color
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 底部按钮 - 改為更明顯的按鈕樣式
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 取消按鈕
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 確認按鈕
                    Button(
                        onClick = {
                            onConfirm(title, timeFormatted, selectedDays, selectedType)
                        },
                        enabled = title.isNotBlank() && selectedDays.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) selectedType.darkColor else selectedType.color,
                            disabledContainerColor = if (isDarkTheme) 
                                MaterialTheme.colorScheme.surfaceVariant 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "確認" else "Confirm",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
} 