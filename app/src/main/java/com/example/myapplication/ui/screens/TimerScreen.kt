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
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 提醒項目數據類
data class ReminderItem(
    val id: Int,
    val title: String,
    val time: String,
    val days: List<String>,
    val type: ReminderType,
    var isEnabled: Boolean = true
)

// 提醒類型
enum class ReminderType(val icon: ImageVector, val color: Color) {
    MEDICATION(Icons.Default.MedicalServices, Color(0xFF4CAF50)),
    WATER(Icons.Default.WaterDrop, Color(0xFF2196F3)),
    HEART_RATE(Icons.Default.Favorite, Color(0xFFE91E63)),
    MEAL(Icons.Default.Restaurant, Color(0xFFFF9800))
}

@Composable
fun TimerScreen(navController: NavController) {
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
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 標題
            Text(
                text = "定時提醒",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
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
                        text = "尚未設置任何提醒\n點擊下方 + 按鈕添加",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 顯示提醒列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { isEnabled ->
                                val index = reminders.indexOfFirst { it.id == reminder.id }
                                if (index >= 0) {
                                    reminders[index] = reminders[index].copy(isEnabled = isEnabled)
                                }
                            },
                            onEdit = {
                                currentEditingReminder = reminder
                                showAddReminderDialog = true
                            },
                            onDelete = {
                                reminders.removeAll { it.id == reminder.id }
                            }
                        )
                    }
                }
            }
        }
        
        // 添加按鈕
        FloatingActionButton(
            onClick = {
                currentEditingReminder = null
                showAddReminderDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF4169E1),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加提醒")
        }
        
        // 添加/編輯提醒對話框
        if (showAddReminderDialog) {
            AddEditReminderDialog(
                reminder = currentEditingReminder,
                onDismiss = { showAddReminderDialog = false },
                onSave = { title, time, days, type ->
                    if (currentEditingReminder != null) {
                        // 編輯現有提醒
                        val index = reminders.indexOfFirst { it.id == currentEditingReminder!!.id }
                        if (index >= 0) {
                            reminders[index] = reminders[index].copy(
                                title = title,
                                time = time,
                                days = days,
                                type = type
                            )
                        }
                    } else {
                        // 添加新提醒
                        val newId = if (reminders.isEmpty()) 1 else reminders.maxOf { it.id } + 1
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
}

@Composable
fun ReminderCard(
    reminder: ReminderItem,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled) Color.White else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 類型圖標
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (reminder.isEnabled) reminder.type.color.copy(alpha = 0.2f)
                        else Color.Gray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reminder.type.icon,
                    contentDescription = null,
                    tint = if (reminder.isEnabled) reminder.type.color else Color.Gray,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 提醒內容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isEnabled) Color.Black else Color.Gray
                )
                
                Text(
                    text = reminder.time,
                    fontSize = 16.sp,
                    color = if (reminder.isEnabled) Color(0xFF4169E1) else Color.Gray
                )
                
                Text(
                    text = reminder.days.joinToString(" · "),
                    fontSize = 14.sp,
                    color = if (reminder.isEnabled) Color.Gray else Color.Gray.copy(alpha = 0.7f)
                )
            }
            
            // 操作按鈕
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4169E1),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "編輯",
                            tint = if (reminder.isEnabled) Color(0xFF4169E1) else Color.Gray
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "刪除",
                            tint = if (reminder.isEnabled) Color.Red else Color.Gray
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
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>, ReminderType) -> Unit
) {
    // 狀態
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var selectedTime by remember { 
        mutableStateOf(reminder?.time ?: 
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    var selectedType by remember { mutableStateOf(reminder?.type ?: ReminderType.MEDICATION) }
    
    // 星期選擇
    val allDays = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日")
    val selectedDays = remember { 
        mutableStateListOf<String>().apply {
            if (reminder != null) {
                addAll(reminder.days)
            } else {
                addAll(allDays) // 預設選擇所有日期
            }
        }
    }
    
    // 時間選擇器狀態
    val initialHour = if (reminder != null) {
        reminder.time.split(":")[0].toInt()
    } else {
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
    
    val initialMinute = if (reminder != null) {
        reminder.time.split(":")[1].toInt()
    } else {
        Calendar.getInstance().get(Calendar.MINUTE)
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )
    
    var showTimePicker by remember { mutableStateOf(false) }
    var showTypeSelector by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 標題
                Text(
                    text = if (reminder == null) "添加提醒" else "編輯提醒",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 提醒標題輸入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("提醒標題") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                // 提醒類型選擇
                Box {
                    OutlinedTextField(
                        value = when (selectedType) {
                            ReminderType.MEDICATION -> "服藥提醒"
                            ReminderType.WATER -> "喝水提醒"
                            ReminderType.HEART_RATE -> "測量心率提醒"
                            ReminderType.MEAL -> "進餐提醒"
                        },
                        onValueChange = { },
                        label = { Text("提醒類型") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { showTypeSelector = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = selectedType.icon,
                                contentDescription = null,
                                tint = selectedType.color
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showTypeSelector,
                        onDismissRequest = { showTypeSelector = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("服藥提醒") },
                            onClick = {
                                selectedType = ReminderType.MEDICATION
                                showTypeSelector = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = ReminderType.MEDICATION.icon,
                                    contentDescription = null,
                                    tint = ReminderType.MEDICATION.color
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("喝水提醒") },
                            onClick = {
                                selectedType = ReminderType.WATER
                                showTypeSelector = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = ReminderType.WATER.icon,
                                    contentDescription = null,
                                    tint = ReminderType.WATER.color
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("測量心率提醒") },
                            onClick = {
                                selectedType = ReminderType.HEART_RATE
                                showTypeSelector = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = ReminderType.HEART_RATE.icon,
                                    contentDescription = null,
                                    tint = ReminderType.HEART_RATE.color
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("進餐提醒") },
                            onClick = {
                                selectedType = ReminderType.MEAL
                                showTypeSelector = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = ReminderType.MEAL.icon,
                                    contentDescription = null,
                                    tint = ReminderType.MEAL.color
                                )
                            }
                        )
                    }
                }
                
                // 提醒時間選擇
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { },
                    label = { Text("提醒時間") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { showTimePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                    }
                )
                
                // 星期選擇
                Text(
                    text = "重複",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    allDays.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF4169E1) else Color(0xFFEEEEEE)
                                )
                                .clickable {
                                    if (isSelected) {
                                        selectedDays.remove(day)
                                    } else {
                                        selectedDays.add(day)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.substring(1, 2), // 例如：週一 -> 一
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                // 按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && selectedDays.isNotEmpty()) {
                                onSave(
                                    title,
                                    selectedTime,
                                    selectedDays.toList(),
                                    selectedType
                                )
                            }
                        },
                        enabled = title.isNotEmpty() && selectedDays.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4169E1)
                        )
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
    
    // 時間選擇器對話框
    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "選擇時間",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    TimePicker(
                        state = timePickerState
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showTimePicker = false }
                        ) {
                            Text("取消")
                        }
                        
                        TextButton(
                            onClick = {
                                val hour = timePickerState.hour.toString().padStart(2, '0')
                                val minute = timePickerState.minute.toString().padStart(2, '0')
                                selectedTime = "$hour:$minute"
                                showTimePicker = false
                            }
                        ) {
                            Text("確定")
                        }
                    }
                }
            }
        }
    }
} 