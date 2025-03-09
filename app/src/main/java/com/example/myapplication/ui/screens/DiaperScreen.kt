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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
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

// 尿布狀態類型
enum class DiaperStatus(val label: String, val color: Color) {
    DRY("乾燥", Color(0xFF4CAF50)), // 綠色
    SLIGHTLY_WET("微濕", Color(0xFFFFC107)), // 黃色
    WET("潮濕", Color(0xFFFF9800)), // 橙色
    VERY_WET("非常潮濕", Color(0xFFF44336)), // 紅色
    SOILED("髒污", Color(0xFF9C27B0)) // 紫色
}

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
    // 示例數據
    val patients = listOf(
        "張三" to "001",
        "李四" to "002",
        "王五" to "003",
        "趙六" to "004",
        "孫七" to "005"
    )
    
    val caregivers = listOf("護工A", "護工B", "護工C", "護工D")
    
    // 選中的病患
    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    
    // 尿布狀態
    var currentStatus by remember { mutableStateOf(DiaperStatus.DRY) }
    
    // 上次更換時間
    var lastChangeTime by remember { mutableLongStateOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3) - TimeUnit.MINUTES.toMillis(Random.nextInt(30).toLong())) }
    
    // 更換記錄
    var diaperChangeRecords by remember { 
        mutableStateOf(
            listOf(
                DiaperChangeRecord(
                    id = 1L,
                    patientId = patients[0].second,
                    patientName = patients[0].first,
                    changeTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)),
                    status = DiaperStatus.WET,
                    notes = "定時更換",
                    changedBy = caregivers[0]
                ),
                DiaperChangeRecord(
                    id = 2L,
                    patientId = patients[0].second,
                    patientName = patients[0].first,
                    changeTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(6)),
                    status = DiaperStatus.SLIGHTLY_WET,
                    notes = "患者要求",
                    changedBy = caregivers[1]
                ),
                DiaperChangeRecord(
                    id = 3L,
                    patientId = patients[0].second,
                    patientName = patients[0].first,
                    changeTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(9)),
                    status = DiaperStatus.SOILED,
                    notes = "晨間護理",
                    changedBy = caregivers[2]
                )
            )
        )
    }
    
    // 添加記錄對話框
    var showAddDialog by remember { mutableStateOf(false) }
    
    // 自動通知
    var autoNotify by remember { mutableStateOf(true) }
    
    // 計算濕度百分比
    val timeSinceLastChange = System.currentTimeMillis() - lastChangeTime
    val hoursSinceLastChange = TimeUnit.MILLISECONDS.toHours(timeSinceLastChange)
    
    // 模擬濕度隨時間變化
    val wetness = when {
        hoursSinceLastChange >= 4 -> 90 // 4小時以上，非常潮濕
        hoursSinceLastChange >= 3 -> 70 // 3-4小時，潮濕
        hoursSinceLastChange >= 2 -> 40 // 2-3小時，微濕
        else -> 10 // 2小時內，基本乾燥
    }
    
    // 更新狀態
    currentStatus = when {
        wetness >= 90 -> DiaperStatus.VERY_WET
        wetness >= 70 -> DiaperStatus.WET
        wetness >= 30 -> DiaperStatus.SLIGHTLY_WET
        else -> DiaperStatus.DRY
    }
    
    // 需要更換的時間閾值（小時）
    val changeThresholdHours = 4
    
    // 是否需要更換
    val needsChange = hoursSinceLastChange >= changeThresholdHours || currentStatus == DiaperStatus.VERY_WET || currentStatus == DiaperStatus.SOILED
    
    // 使用LazyColumn替代Column让整个页面可以滚动
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部标题
        item {
            Text(
                text = "尿布監測",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // 病患选择
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "患者",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "患者: ${patients[selectedPatientIndex].first}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = { showPatientDropdown = true }
                    ) {
                        Icon(
                            imageVector = if (showPatientDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "選擇患者"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showPatientDropdown,
                        onDismissRequest = { showPatientDropdown = false }
                    ) {
                        patients.forEachIndexed { index, patient ->
                            DropdownMenuItem(
                                text = { Text(text = patient.first) },
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
        
        // 当前状态卡片
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (needsChange) Color(0xFFFCE4EC) else Color.White
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
                        Text(
                            text = "當前尿布狀態",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "自動通知",
                                fontSize = 14.sp
                            )
                            
                            Switch(
                                checked = autoNotify,
                                onCheckedChange = { autoNotify = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2196F3)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 狀態顯示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(currentStatus.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (needsChange) Icons.Default.Warning else Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = currentStatus.color,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentStatus.label,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentStatus.color
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = if (needsChange) "需要更換尿布！" else "尿布狀態良好",
                                color = if (needsChange) Color.Red else Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 濕度進度條
                            LinearProgressIndicator(
                                progress = wetness / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = currentStatus.color
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "濕度: $wetness%",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 上次更換時間
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "上次更換: ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(lastChangeTime))} (${formatTimeSince(timeSinceLastChange)}前)",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    
                    if (needsChange) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text("記錄尿布更換")
                        }
                    }
                }
            }
        }
        
        // 更換記錄列表
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp), // 设置为足够长的固定高度
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "更換記錄",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Divider(modifier = Modifier.padding(bottom = 8.dp))
                    
                    if (diaperChangeRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暫無記錄",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        // 在Card内使用LazyColumn显示记录
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(diaperChangeRecords.sortedByDescending { it.changeTime }) { record ->
                                DiaperChangeRecordItem(record = record)
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
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
                
                diaperChangeRecords = diaperChangeRecords + newRecord
                lastChangeTime = System.currentTimeMillis()
                showAddDialog = false
            },
            caregivers = caregivers,
            initialStatus = currentStatus
        )
    }
}

@Composable
fun DiaperChangeRecordItem(record: DiaperChangeRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(record.status.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = record.status.color
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(record.changeTime),
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "狀態: ${record.status.label}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = record.status.color
            )
            
            if (record.notes.isNotEmpty()) {
                Text(
                    text = "備註: ${record.notes}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Text(
            text = "更換人: ${record.changedBy}",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    
    Divider(color = Color.LightGray.copy(alpha = 0.5f))
}

@Composable
fun AddDiaperChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (DiaperStatus, String, String) -> Unit,
    caregivers: List<String>,
    initialStatus: DiaperStatus
) {
    var selectedStatus by remember { mutableStateOf(initialStatus) }
    var notes by remember { mutableStateOf("") }
    var selectedCaregiver by remember { mutableStateOf(caregivers.firstOrNull() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "記錄尿布更換",
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
                    text = "尿布狀態",
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
                                color = if (selectedStatus == status) status.color else Color.Black,
                                fontWeight = if (selectedStatus == status) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "護理人員",
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
                                color = if (selectedCaregiver == caregiver) Color(0xFF2196F3) else Color.Black,
                                fontWeight = if (selectedCaregiver == caregiver) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("備註（可選）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus, notes, selectedCaregiver) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("確認")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// 格式化時間差
fun formatTimeSince(timeDiff: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60
    
    return when {
        hours > 0 -> "$hours 小時 $minutes 分鐘"
        else -> "$minutes 分鐘"
    }
} 