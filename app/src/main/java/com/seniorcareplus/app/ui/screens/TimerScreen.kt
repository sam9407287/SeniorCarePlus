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
import androidx.compose.material.icons.filled.Thermostat
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.seniorcareplus.app.ui.theme.DarkCardBackground
import com.seniorcareplus.app.ui.theme.LightCardBackground
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager
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
    GENERAL("一般", "Norm", Color(0xFF009688), Color(0xFF4DB6AC))
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
    val type: ReminderType,
    val isEnabled: Boolean = true // 添加啟用/禁用狀態，默認為啟用
)

@Composable
fun TimerScreen(navController: NavController) {
    // 判断是否为深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    // 檢查語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 使用ViewModel來管理提醒列表（帶有緩存功能）
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<ReminderViewModel>()
    val reminders = viewModel.reminders
    
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
            // 標題 - 根據語言設置顯示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "定時提醒" else "Timer Reminders",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                

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
                        text = if (isChineseLanguage) "暫無提醒事項" else "No reminders",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp) // 添加底部間距，避免被FAB遮擋
                ) {
                    items(reminders) { reminder ->
                        ReminderItemCard(
                            reminder = reminder,
                            isDarkTheme = isDarkTheme,
                            isChineseLanguage = isChineseLanguage,
                            onEdit = {
                                currentEditingReminder = reminder
                                showAddReminderDialog = true
                            },
                            onDelete = {
                                viewModel.deleteReminder(reminder.id)
                            },
                            onToggle = { isActive ->
                                // 切換提醒的啟用/禁用狀態
                                viewModel.toggleReminder(reminder.id, isActive)
                                
                                // 如果啟用並且需要測試，可以顯示提醒對話框
                                if (isActive) {
                                    viewModel.showReminderAlert(reminder.id)
                                }
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
                contentDescription = if (isChineseLanguage) "添加提醒" else "Add Reminder",
                tint = Color.White
            )
        }
    }
    
    // 顯示提醒對話框（當從通知打開時）
    if (viewModel.showReminderAlert) {
        viewModel.currentReminder?.let { reminder ->
            // 這裡不需要添加對話框，因為已經在 MainActivity 中添加了
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
                    val updatedReminder = currentEditingReminder!!.copy(
                        title = title,
                        time = time,
                        days = days,
                        type = type,
                        isEnabled = currentEditingReminder!!.isEnabled // 保留原始的啟用狀態
                    )
                    viewModel.updateReminder(updatedReminder)
                } else {
                    // 添加新提醒
                    val newId = viewModel.getNextId()
                    val newReminder = ReminderItem(
                        id = newId,
                        title = title,
                        time = time,
                        days = days,
                        type = type,
                        isEnabled = true // 默認啟用新提醒
                    )
                    viewModel.addReminder(newReminder)
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
    isChineseLanguage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    // 使用 reminder 的 isEnabled 屬性作為初始值
    var isEnabled by remember { mutableStateOf(reminder.isEnabled) }
    
    // 獲取提醒類型對應的顏色
    val typeColor = if (isDarkTheme) reminder.type.darkColor else reminder.type.color
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧：图标和标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 圖標
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (reminder.type) {
                            ReminderType.MEDICATION -> Icon(
                                imageVector = Icons.Default.Medication, 
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            ReminderType.WATER -> Icon(
                                imageVector = Icons.Default.WaterDrop, 
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            ReminderType.MEAL -> Icon(
                                imageVector = Icons.Default.Restaurant, 
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            ReminderType.HEART_RATE -> Icon(
                                imageVector = Icons.Default.Favorite, 
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            ReminderType.TEMPERATURE -> Icon(
                                imageVector = Icons.Default.Thermostat, 
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            ReminderType.GENERAL -> Icon(
                                imageVector = Icons.Default.Timer, 
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 标题
                    Column {
                        Text(
                            text = reminder.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 时间
                        Text(
                            text = reminder.time,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 右侧：开关
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newValue ->
                        isEnabled = newValue  // 更新本地状态
                        onToggle(newValue)    // 通知外部状态更改
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = typeColor,
                        checkedBorderColor = typeColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 显示重复日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.days.joinToString(" · "),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 编辑和删除按钮
                Row {
                    // 编辑按钮
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (isChineseLanguage) "編輯" else "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // 删除按钮
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = if (isChineseLanguage) "刪除" else "Delete",
                            tint = MaterialTheme.colorScheme.error,
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
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                        horizontalArrangement = Arrangement.SpaceEvenly
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
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(0.7f)  // 更小的權重
                                .padding(end = 6.dp)
                                .height(40.dp)
                        ) {
                            Text(
                                text = if (isChineseLanguage) "全選" else "All",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        
                        // 平日按鈕
                        Button(
                            onClick = { 
                                selectedDays.clear()
                                selectedDays.addAll(daysOfWeek.take(5)) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedColor.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.3f)  // 更大的權重
                                .padding(horizontal = 4.dp)
                                .height(40.dp)
                        ) {
                            Text(
                                text = if (isChineseLanguage) "平日" else "Weekday",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        
                        // 假日按鈕
                        Button(
                            onClick = { 
                                selectedDays.clear()
                                selectedDays.addAll(daysOfWeek.takeLast(2)) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedColor.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.3f)  // 更大的權重
                                .padding(start = 6.dp)
                                .height(40.dp)
                        ) {
                            Text(
                                text = if (isChineseLanguage) "假日" else "Weekend",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                    
                    // 改進日期選擇按鈕佈局 - 確保均勻分佈
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        val dayTexts = if (isChineseLanguage) {
                            listOf("一", "二", "三", "四", "五", "六", "日")
                        } else {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        }
                        
                        // 使用固定寬度佈局保證對稱性
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 7) {
                                val day = daysOfWeek[i]
                                val isSelected = selectedDays.contains(day)
                                // 統一並縮小按鈕大小
                                val buttonSize = 36.dp
                                
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
                                ) {
                                    Text(
                                        text = dayTexts[i],
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isChineseLanguage) 13.sp else 10.sp,  // 調整字體大小
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
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
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    
                    // 確認按鈕
                    Button(
                        onClick = {
                            onConfirm(selectedDays.toList())
                        },
                        enabled = selectedDays.isNotEmpty(),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedColor,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "確認" else "Confirm",
                            color = if (selectedDays.isNotEmpty()) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
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
    
    // 安全地解析時間
    var hour by remember { 
        mutableStateOf(
            try {
                reminder?.time?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
            } catch (e: Exception) {
                8 // 預設值
            }
        )
    }
    
    var minute by remember { 
        mutableStateOf(
            try {
                reminder?.time?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
            } catch (e: Exception) {
                0 // 預設值
            }
        )
    }
    
    var timeFormatted by remember { 
        mutableStateOf(
            reminder?.time ?: "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        )
    }
    
    // 日期选择 - 根據當前語言設置
    var showDayPicker by remember { mutableStateOf(false) }
    val daysOfWeek = if (isChineseLanguage) {
        listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日")
    } else {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
    
    // 確保選擇的日期也跟隨當前語言設置
    var selectedDays by remember { mutableStateOf<List<String>>(
        if (reminder != null) {
            // 如果已有選擇的日期，轉換為當前語言
            val existingDays = reminder.days
            if (isChineseLanguage) {
                existingDays.map { day ->
                    when (day) {
                        "Mon" -> "週一"
                        "Tue" -> "週二"
                        "Wed" -> "週三"
                        "Thu" -> "週四"
                        "Fri" -> "週五"
                        "Sat" -> "週六"
                        "Sun" -> "週日"
                        else -> day
                    }
                }
            } else {
                existingDays.map { day ->
                    when (day) {
                        "週一" -> "Mon"
                        "週二" -> "Tue"
                        "週三" -> "Wed"
                        "週四" -> "Thu"
                        "週五" -> "Fri"
                        "週六" -> "Sat"
                        "週日" -> "Sun"
                        else -> day
                    }
                }
            }
        } else {
            // 默認選擇工作日
            daysOfWeek.take(5)
        }
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
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 标题
                Text(
                    text = if (reminder != null) {
                        if (isChineseLanguage) "編輯提醒" else "Edit Reminder"
                    } else {
                        if (isChineseLanguage) "添加提醒" else "Add Reminder"
                    },
                    fontSize = 22.sp,
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
                
                // 提醒類型選擇器 - 修正英文標籤問題
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 計算每個類型項目的比例和顯示大小
                        ReminderType.values().forEach { type ->
                            val isSelected = selectedType == type
                            
                            // 修正英文標籤顯示
                            val labelText = if (isChineseLanguage) {
                                type.zhLabel  // 中文標籤
                            } else {
                                // 完整英文標籤
                                when (type) {
                                    ReminderType.MEDICATION -> "Med"
                                    ReminderType.WATER -> "Water"
                                    ReminderType.MEAL -> "Meal"
                                    ReminderType.HEART_RATE -> "Heart"
                                    ReminderType.TEMPERATURE -> "Temp"
                                    ReminderType.GENERAL -> "Norm"
                                }
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedType = type }
                                    .background(
                                        if (isSelected) 
                                            if (isDarkTheme) type.darkColor.copy(alpha = 0.2f) else type.color.copy(alpha = 0.1f)
                                        else 
                                            Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 6.dp, horizontal = 1.dp)
                            ) {
                                // 圖標
                                Box(
                                    modifier = Modifier
                                        .size(35.dp)
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
                                            tint = if (isDarkTheme) type.darkColor else type.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        ReminderType.WATER -> Icon(
                                            imageVector = Icons.Default.WaterDrop, 
                                            contentDescription = null,
                                            tint = if (isDarkTheme) type.darkColor else type.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        ReminderType.MEAL -> Icon(
                                            imageVector = Icons.Default.Restaurant, 
                                            contentDescription = null,
                                            tint = if (isDarkTheme) type.darkColor else type.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        ReminderType.HEART_RATE -> Icon(
                                            imageVector = Icons.Default.Favorite, 
                                            contentDescription = null,
                                            tint = if (isDarkTheme) type.darkColor else type.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        ReminderType.TEMPERATURE -> Icon(
                                            imageVector = Icons.Default.Thermostat, 
                                            contentDescription = null,
                                            tint = if (isDarkTheme) type.darkColor else type.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        ReminderType.GENERAL -> Icon(
                                            imageVector = Icons.Default.Timer, 
                                            contentDescription = null,
                                            tint = if (isDarkTheme) type.darkColor else type.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(3.dp))
                                
                                // 調整標籤字體大小
                                Text(
                                    text = labelText,
                                    fontSize = if (isChineseLanguage) 12.sp else 12.sp,  // 增大英文字體大小
                                    color = if (isDarkTheme) type.darkColor else type.color,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) selectedType.darkColor else selectedType.color
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 重复日期选择
                Text(
                    text = if (isChineseLanguage) "重複日期" else "Repeat Days",
                    fontSize = 14.sp,
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDays.joinToString(" · "),
                            fontSize = 14.sp,
                            color = if (isDarkTheme) selectedType.darkColor else selectedType.color,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 取消按鈕
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "取消" else "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    
                    // 確認按鈕
                    Button(
                        onClick = {
                            try {
                                // 確保標題不為空
                                val safeTitle = title.takeIf { it.isNotBlank() } ?: "提醒"
                                onConfirm(safeTitle, timeFormatted, selectedDays, selectedType)
                            } catch (e: Exception) {
                                // 如果發生錯誤，仍然關閉對話框
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && selectedDays.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) selectedType.darkColor else selectedType.color,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isChineseLanguage) "確認" else "Confirm",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
} 