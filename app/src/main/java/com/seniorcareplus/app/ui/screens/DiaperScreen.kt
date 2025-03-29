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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.seniorcareplus.app.ui.theme.DarkCardBackground
import com.seniorcareplus.app.ui.theme.LightCardBackground
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager

// 尿布狀態類型
enum class DiaperStatus(val chineseLabel: String, val englishLabel: String, val color: Color) {
    DRY("乾燥", "Dry", Color(0xFF4CAF50)), // 綠色
    SLIGHTLY_WET("微濕", "Slightly Wet", Color(0xFFFFC107)), // 黃色
    WET("潮濕", "Wet", Color(0xFFFF9800)), // 橙色
    VERY_WET("非常潮濕", "Very Wet", Color(0xFFF44336)), // 紅色
    SOILED("髒污", "Soiled", Color(0xFF9C27B0)) // 紫色
}

// 根據語言設置獲取標籤
val DiaperStatus.label: String
    get() = if (LanguageManager.isChineseLanguage) this.chineseLabel else this.englishLabel

// 尿布更換記錄
data class DiaperChangeRecord(
    val id: Long,
    val patientId: String,
    val patientName: String,
    val changeTime: Date,
    val status: DiaperStatus,
    val notes: String,
    val changedBy: String
)

@Composable
fun DiaperMonitorScreen(navController: NavController) {
    // 判断是否为深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    // 判断当前语言
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 示例數據
    val patients = if (isChineseLanguage) {
        listOf(
            "張三" to "001",
            "李四" to "002",
            "王五" to "003",
            "趙六" to "004",
            "孫七" to "005"
        )
    } else {
        listOf(
            "Zhang San" to "001", 
            "Li Si" to "002",
            "Wang Wu" to "003",
            "Zhao Liu" to "004",
            "Sun Qi" to "005"
        )
    }
    
    val caregivers = if (isChineseLanguage) {
        listOf("護工A", "護工B", "護工C", "護工D")
    } else {
        listOf("Caregiver A", "Caregiver B", "Caregiver C", "Caregiver D")
    }
    
    // 選中的病患
    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    
    // 選中的時間範圍
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = if (isChineseLanguage) {
        listOf("今日", "本週", "本月") 
    } else {
        listOf("Today", "This Week", "This Month")
    }
    
    // 模拟尿布状态
    var needChange by remember { mutableStateOf(true) }
    var wetness by remember { mutableStateOf(0.75f) }
    var autoNotify by remember { mutableStateOf(true) }
    
    // 模擬尿布更換記錄
    val changeRecords = remember {
        val records = mutableListOf<DiaperChangeRecord>()
        val now = LocalDateTime.now()
        val patient = patients[selectedPatientIndex]
        
        // 生成过去7天的记录，每天有2-4条随机记录
        for (day in 0..6) {
            val recordsPerDay = Random.nextInt(2, 5)
            for (record in 0 until recordsPerDay) {
                val hour = Random.nextInt(6, 23)
                val minute = Random.nextInt(0, 59)
                val time = now.minusDays(day.toLong()).withHour(hour).withMinute(minute)
                
                records.add(
                    DiaperChangeRecord(
                        id = System.currentTimeMillis(),
                        patientId = patient.second,
                        patientName = patient.first,
                        changeTime = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(day.toLong()) - TimeUnit.HOURS.toMillis((23 - hour).toLong()) - TimeUnit.MINUTES.toMillis((59 - minute).toLong())),
                        status = if (Random.nextBoolean()) DiaperStatus.WET else DiaperStatus.DRY,
                        notes = "",
                        changedBy = caregivers[Random.nextInt(caregivers.size)]
                    )
                )
            }
        }
        records
    }
    
    // 添加記錄對話框
    var showAddDialog by remember { mutableStateOf(false) }
    
    // 計算濕度百分比
    val timeSinceLastChange = System.currentTimeMillis() - changeRecords.maxOf { it.changeTime.time }
    val hoursSinceLastChange = TimeUnit.MILLISECONDS.toHours(timeSinceLastChange)
    
    // 更新狀態
    needChange = hoursSinceLastChange >= 4 || changeRecords.any { it.status == DiaperStatus.VERY_WET || it.status == DiaperStatus.SOILED }
    
    // 需要更換的時間閾值（小時）
    val changeThresholdHours = 4
    
    // 是否需要更換
    val needsChange = hoursSinceLastChange >= changeThresholdHours || needChange
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部标题
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "尿布監測" else "Diaper Monitor",
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
                        contentDescription = if (isChineseLanguage) "切換主題" else "Toggle Theme",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // 患者选择
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surface 
                        else 
                            Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = if (isChineseLanguage) "患者" else "Patient",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = if (isChineseLanguage) "患者: ${patients[selectedPatientIndex].first}" else "Patient: ${patients[selectedPatientIndex].first}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        IconButton(
                            onClick = { showPatientDropdown = true }
                        ) {
                            Icon(
                                imageVector = if (showPatientDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isChineseLanguage) "選擇患者" else "Select Patient",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showPatientDropdown,
                            onDismissRequest = { showPatientDropdown = false },
                            modifier = Modifier.background(
                                if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surfaceVariant 
                                else 
                                    Color.White
                            )
                        ) {
                            patients.forEachIndexed { index, patient ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = patient.first,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedPatientIndex = index
                                        showPatientDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 时间范围选项卡
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = if (isDarkTheme) 
                    MaterialTheme.colorScheme.surface 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title, 
                                color = if (selectedTabIndex == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 当前尿布状态卡片
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (needChange)
                        if (isDarkTheme) Color(0xFF442536) else Color(0xFFFCE4EC)
                    else
                        if (isDarkTheme) DarkCardBackground else LightCardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isChineseLanguage) "當前尿布狀態" else "Current Diaper Status",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isChineseLanguage) "自動通知" else "Auto Notify",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Switch(
                                checked = autoNotify,
                                onCheckedChange = { autoNotify = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 尿布状态指示图标
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (needChange)
                                        if (isDarkTheme) Color(0xFF9F4D6D) else Color(0xFFFFCDD2)
                                    else
                                        if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (needChange) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = if (isChineseLanguage) "需要更換" else "Change Needed",
                                    tint = if (isDarkTheme) Color.White else Color(0xFFE53935),
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.BabyChangingStation,
                                    contentDescription = if (isChineseLanguage) "尿布狀態良好" else "Diaper Status Good",
                                    tint = if (isDarkTheme) Color.White else Color(0xFF4CAF50),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isChineseLanguage) {
                                    if (needChange) "需要更換尿布！" else "尿布狀態良好"
                                } else {
                                    if (needChange) "Diaper Change Needed!" else "Diaper Status Good"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (needChange)
                                    if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFE53935)
                                else
                                    if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF4CAF50)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isChineseLanguage) "濕度: " else "Wetness: ",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                LinearProgressIndicator(
                                    progress = { wetness },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp),
                                    color = if (wetness > 0.6f)
                                        if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFE53935)
                                    else
                                        if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3)
                                )
                                
                                Text(
                                    text = " ${(wetness * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val lastChange = changeRecords.maxByOrNull { it.changeTime.time }
                            if (lastChange != null) {
                                Text(
                                    text = if (isChineseLanguage) {
                                        "上次更換時間: ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(lastChange.changeTime.time))} (${formatTimeSince(timeSinceLastChange, isChineseLanguage)}前)"
                                    } else {
                                        "Last change: ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(lastChange.changeTime.time))} (${formatTimeSince(timeSinceLastChange, isChineseLanguage)} ago)"
                                    },
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    if (needChange) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isChineseLanguage) "記錄尿布更換" else "Record Diaper Change")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 尿布更换记录
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) DarkCardBackground else LightCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isChineseLanguage) "尿布更換記錄" else "Diaper Change Records",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Divider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = if (isDarkTheme) Color.DarkGray else Color.LightGray
                    )
                    
                    if (changeRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isChineseLanguage) "暫無記錄" else "No Records",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(changeRecords.sortedByDescending { it.changeTime.time }) { record ->
                                DiaperChangeRecordItem(
                                    record = record,
                                    isDarkTheme = isDarkTheme,
                                    isChineseLanguage = isChineseLanguage
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = if (isDarkTheme) Color.DarkGray.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 底部空间，确保内容可以滚动到底部
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // 添加記錄對話框
    if (showAddDialog) {
        AddDiaperChangeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { status, notes, caregiver ->
                // 添加新記錄
                val newRecord = DiaperChangeRecord(
                    id = System.currentTimeMillis(),
                    patientId = patients[selectedPatientIndex].second,
                    patientName = patients[selectedPatientIndex].first,
                    changeTime = Date(),
                    status = status,
                    notes = notes,
                    changedBy = caregiver
                )
                
                changeRecords.add(newRecord)
                showAddDialog = false
            },
            caregivers = caregivers,
            initialStatus = changeRecords.first().status,
            isChineseLanguage = isChineseLanguage
        )
    }
}

@Composable
fun DiaperChangeRecordItem(record: DiaperChangeRecord, isDarkTheme: Boolean, isChineseLanguage: Boolean) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(record.changeTime)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (record.status == DiaperStatus.WET)
                        if (isDarkTheme) Color(0xFF90CAF9).copy(alpha = 0.3f) else Color(0xFFE3F2FD)
                    else if (record.status == DiaperStatus.SOILED)
                        if (isDarkTheme) Color(0xFF795548).copy(alpha = 0.3f) else Color(0xFFEFEBE9)
                    else
                        if (isDarkTheme) Color(0xFF64B5F6).copy(alpha = 0.3f) else Color(0xFFE3F2FD)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BabyChangingStation,
                contentDescription = null,
                tint = if (record.status == DiaperStatus.WET)
                    if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF2196F3)
                else if (record.status == DiaperStatus.SOILED)
                    if (isDarkTheme) Color(0xFFA1887F) else Color(0xFF795548)
                else
                    if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF2196F3)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formattedTime,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = if (isChineseLanguage) "狀態: ${record.status.label}" else "Status: ${record.status.label}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = record.status.color
            )
        }
        
        Text(
            text = if (isChineseLanguage) "更換人: ${record.changedBy}" else "Changed by: ${record.changedBy}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun AddDiaperChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (DiaperStatus, String, String) -> Unit,
    caregivers: List<String>,
    initialStatus: DiaperStatus,
    isChineseLanguage: Boolean
) {
    var selectedStatus by remember { mutableStateOf(initialStatus) }
    var notes by remember { mutableStateOf("") }
    var selectedCaregiver by remember { mutableStateOf(caregivers.firstOrNull() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isChineseLanguage) "記錄尿布更換" else "Record Diaper Change",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (isChineseLanguage) "尿布狀態" else "Diaper Status",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column {
                    DiaperStatus.values().forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedStatus = status }
                        ) {
                            RadioButton(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = status.color
                                )
                            )
                            
                            Text(
                                text = status.label,
                                color = if (selectedStatus == status) status.color else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedStatus == status) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isChineseLanguage) "護理人員" else "Caregiver",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column {
                    caregivers.forEach { caregiver ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedCaregiver = caregiver }
                        ) {
                            RadioButton(
                                selected = selectedCaregiver == caregiver,
                                onClick = { selectedCaregiver = caregiver },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF2196F3)
                                )
                            )
                            
                            Text(
                                text = caregiver,
                                color = if (selectedCaregiver == caregiver) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedCaregiver == caregiver) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isChineseLanguage) "備註（可選）" else "Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus, notes, selectedCaregiver) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text(if (isChineseLanguage) "確認" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isChineseLanguage) "取消" else "Cancel")
            }
        }
    )
}

// 格式化時間差
fun formatTimeSince(timeDiff: Long, isChineseLanguage: Boolean): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60
    
    return if (isChineseLanguage) {
        when {
            hours > 0 -> "$hours 小時 $minutes 分鐘"
            else -> "$minutes 分鐘"
        }
    } else {
        when {
            hours > 0 -> "$hours hours $minutes minutes"
            else -> "$minutes minutes"
        }
    }
} 