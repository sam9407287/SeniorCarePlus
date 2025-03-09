package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// 緊急類型
enum class EmergencyType(val label: String, val color: Color) {
    FALL("跌倒", Color(0xFFE53935)),
    PAIN("疼痛", Color(0xFFFF9800)),
    TOILET("如廁協助", Color(0xFF4CAF50)),
    MEDICATION("藥物協助", Color(0xFF2196F3)),
    OTHER("其他協助", Color(0xFF9C27B0))
}

// 緊急狀態
enum class EmergencyStatus(val label: String, val color: Color) {
    PENDING("等待響應", Color(0xFFE53935)),
    RESPONDING("正在響應", Color(0xFFFF9800)),
    RESOLVED("已解決", Color(0xFF4CAF50))
}

// 緊急呼叫記錄
data class EmergencyCallRecord(
    val id: Long,
    val patientId: String,
    val patientName: String,
    val callTime: Date,
    val location: String,
    val type: EmergencyType,
    val status: EmergencyStatus,
    val responder: String? = null,
    val responseTime: Date? = null,
    val resolvedTime: Date? = null,
    val notes: String = ""
)

@Composable
fun EmergencyButtonScreen(navController: NavController) {
    // 示例數據
    val patients = listOf(
        "張三" to "001",
        "李四" to "002",
        "王五" to "003",
        "趙六" to "004",
        "孫七" to "005"
    )
    
    val locations = listOf(
        "A區 101房",
        "A區 102房",
        "B區 201房",
        "B區 202房",
        "C區 301房",
        "C區 302房",
        "餐廳",
        "活動室",
        "康復區"
    )
    
    val responders = listOf("護工A", "護工B", "護工C", "護工D", "醫生A", "醫生B")
    
    // 選中的病患
    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    
    // 選中的位置
    var selectedLocationIndex by remember { mutableIntStateOf(0) }
    var showLocationDropdown by remember { mutableStateOf(false) }
    
    // 模擬緊急呼叫
    var isEmergencyActive by remember { mutableStateOf(false) }
    var activeEmergency by remember { mutableStateOf<EmergencyCallRecord?>(null) }
    var emergencyType by remember { mutableStateOf(EmergencyType.FALL) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    
    // 歷史呼叫記錄
    var emergencyCallRecords by remember { 
        mutableStateOf(
            listOf(
                EmergencyCallRecord(
                    id = 1,
                    patientId = patients[0].second,
                    patientName = patients[0].first,
                    callTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)),
                    location = locations[0],
                    type = EmergencyType.FALL,
                    status = EmergencyStatus.RESOLVED,
                    responder = responders[0],
                    responseTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(1)),
                    resolvedTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(15)),
                    notes = "患者輕微擦傷，已處理"
                ),
                EmergencyCallRecord(
                    id = 2,
                    patientId = patients[1].second,
                    patientName = patients[1].first,
                    callTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)),
                    location = locations[1],
                    type = EmergencyType.PAIN,
                    status = EmergencyStatus.RESOLVED,
                    responder = responders[1],
                    responseTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(2)),
                    resolvedTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(20)),
                    notes = "患者腹痛，已給予藥物緩解"
                ),
                EmergencyCallRecord(
                    id = 3,
                    patientId = patients[2].second,
                    patientName = patients[2].first,
                    callTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(8)),
                    location = locations[2],
                    type = EmergencyType.TOILET,
                    status = EmergencyStatus.RESOLVED,
                    responder = responders[2],
                    responseTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(8) + TimeUnit.MINUTES.toMillis(3)),
                    resolvedTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(8) + TimeUnit.MINUTES.toMillis(10)),
                    notes = "協助患者如廁"
                )
            )
        )
    }
    
    // 緊急呼叫模擬響應
    LaunchedEffect(isEmergencyActive) {
        if (isEmergencyActive && activeEmergency != null) {
            delay(Random.nextLong(5000, 10000)) // 5-10秒後響應
            
            if (isEmergencyActive) {
                // 更新為響應狀態
                val responding = activeEmergency!!.copy(
                    status = EmergencyStatus.RESPONDING,
                    responder = responders[Random.nextInt(responders.size)],
                    responseTime = Date()
                )
                activeEmergency = responding
                
                // 更新記錄
                emergencyCallRecords = emergencyCallRecords.map { 
                    if (it.id == responding.id) responding else it 
                }
                
                delay(Random.nextLong(15000, 30000)) // 15-30秒後解決
                
                if (isEmergencyActive) {
                    // 更新為已解決
                    val resolved = responding.copy(
                        status = EmergencyStatus.RESOLVED,
                        resolvedTime = Date(),
                        notes = "緊急情況已處理"
                    )
                    activeEmergency = resolved
                    
                    // 更新記錄
                    emergencyCallRecords = emergencyCallRecords.map { 
                        if (it.id == resolved.id) resolved else it 
                    }
                    
                    isEmergencyActive = false
                }
            }
        }
    }
    
    // 顯示響應對話框
    var showResponseDialog by remember { mutableStateOf(false) }
    var responseNotes by remember { mutableStateOf("") }
    
    // 緊急按鈕動畫
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // 移除垂直滾動的Column，改用LazyColumn
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 頂部標題
        item {
            Text(
                text = "緊急呼叫",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )
        }
        
        // 顯示當前緊急狀態
        item {
            if (isEmergencyActive && activeEmergency != null) {
                EmergencyActiveCard(
                    emergency = activeEmergency!!,
                    onCancel = {
                        showResponseDialog = true
                    }
                )
            } else {
                // 病患選擇
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
                            tint = Color(0xFFE53935),
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
                
                // 位置選擇
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "位置",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "位置: ${locations[selectedLocationIndex]}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = { showLocationDropdown = true }
                        ) {
                            Icon(
                                imageVector = if (showLocationDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "選擇位置"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showLocationDropdown,
                            onDismissRequest = { showLocationDropdown = false }
                        ) {
                            locations.forEachIndexed { index, location ->
                                DropdownMenuItem(
                                    text = { Text(text = location) },
                                    onClick = {
                                        selectedLocationIndex = index
                                        showLocationDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 緊急按鈕
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .clickable { showEmergencyDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "緊急\n呼叫",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // 歷史記錄標題
        item {
            Text(
                text = "呼叫記錄",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // 歷史呼叫記錄
        if (emergencyCallRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暫無呼叫記錄",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(emergencyCallRecords.sortedByDescending { it.callTime }) { record ->
                EmergencyRecordItem(record = record)
            }
        }
    }
    
    // 緊急類型選擇對話框
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = {
                Text(
                    text = "選擇緊急類型",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    EmergencyType.values().forEach { type ->
                        Button(
                            onClick = {
                                emergencyType = type
                                
                                // 創建新的緊急呼叫
                                val newEmergency = EmergencyCallRecord(
                                    id = System.currentTimeMillis(),
                                    patientId = patients[selectedPatientIndex].second,
                                    patientName = patients[selectedPatientIndex].first,
                                    callTime = Date(),
                                    location = locations[selectedLocationIndex],
                                    type = type,
                                    status = EmergencyStatus.PENDING
                                )
                                
                                // 更新狀態
                                activeEmergency = newEmergency
                                isEmergencyActive = true
                                emergencyCallRecords = emergencyCallRecords + newEmergency
                                
                                showEmergencyDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = type.color),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = type.label,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 響應對話框
    if (showResponseDialog) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            title = {
                Text(
                    text = "取消緊急呼叫",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "請輸入取消原因：",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = responseNotes,
                        onValueChange = { responseNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("取消原因") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 更新為已解決
                        if (activeEmergency != null) {
                            val resolved = activeEmergency!!.copy(
                                status = EmergencyStatus.RESOLVED,
                                resolvedTime = Date(),
                                notes = "已取消：$responseNotes"
                            )
                            
                            // 更新記錄
                            emergencyCallRecords = emergencyCallRecords.map { 
                                if (it.id == resolved.id) resolved else it 
                            }
                            
                            isEmergencyActive = false
                            activeEmergency = null
                            responseNotes = ""
                            showResponseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("確認取消")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResponseDialog = false }) {
                    Text("返回")
                }
            }
        )
    }
}

@Composable
fun EmergencyActiveCard(
    emergency: EmergencyCallRecord,
    onCancel: () -> Unit
) {
    val isResponding = emergency.status == EmergencyStatus.RESPONDING
    
    // 計算等待時間
    val currentTime = System.currentTimeMillis()
    val callTime = emergency.callTime.time
    val waitTime = currentTime - callTime
    val waitMinutes = TimeUnit.MILLISECONDS.toMinutes(waitTime)
    val waitSeconds = TimeUnit.MILLISECONDS.toSeconds(waitTime) % 60
    
    // 等待時間顯示更新
    var displayWaitTime by remember { mutableStateOf("${waitMinutes}分${waitSeconds}秒") }
    var elapsedSeconds by remember { mutableLongStateOf(TimeUnit.MILLISECONDS.toSeconds(waitTime)) }
    
    // 更新等待時間
    LaunchedEffect(elapsedSeconds) {
        delay(1000)
        elapsedSeconds++
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        displayWaitTime = "${minutes}分${seconds}秒"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isResponding) Color(0xFFFFF9C4) else Color(0xFFFFEBEE)
        )
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
                    text = if (isResponding) "正在響應中..." else "緊急呼叫中...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isResponding) Color(0xFFFF9800) else Color(0xFFE53935)
                )
                
                AnimatedVisibility(
                    visible = !isResponding,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFE53935), CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            EmergencyInfoRow(
                label = "患者:",
                value = emergency.patientName,
                valueColor = Color.Black
            )
            
            EmergencyInfoRow(
                label = "位置:",
                value = emergency.location,
                valueColor = Color.Black
            )
            
            EmergencyInfoRow(
                label = "類型:",
                value = emergency.type.label,
                valueColor = emergency.type.color
            )
            
            EmergencyInfoRow(
                label = "呼叫時間:",
                value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(emergency.callTime),
                valueColor = Color.Black
            )
            
            EmergencyInfoRow(
                label = "等待時間:",
                value = displayWaitTime,
                valueColor = if (isResponding) Color(0xFFFF9800) else Color(0xFFE53935)
            )
            
            if (isResponding) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                EmergencyInfoRow(
                    label = "響應人員:",
                    value = emergency.responder ?: "",
                    valueColor = Color.Black
                )
                
                if (emergency.responseTime != null) {
                    EmergencyInfoRow(
                        label = "響應時間:",
                        value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(emergency.responseTime),
                        valueColor = Color.Black
                    )
                }
                
                Text(
                    text = "護理人員正趕往現場...",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isResponding) Color(0xFF9E9E9E) else Color(0xFFE53935)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isResponding) Icons.Default.Refresh else Icons.Default.Check,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (isResponding) "刷新狀態" else "取消呼叫"
                )
            }
        }
    }
}

@Composable
fun EmergencyInfoRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.DarkGray,
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun EmergencyRecordItem(
    record: EmergencyCallRecord
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (record.status) {
                EmergencyStatus.PENDING -> Color(0xFFFFEBEE)
                EmergencyStatus.RESPONDING -> Color(0xFFFFF9C4)
                EmergencyStatus.RESOLVED -> Color.White
            }
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(record.type.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (record.status) {
                                EmergencyStatus.PENDING -> Icons.Default.Error
                                EmergencyStatus.RESPONDING -> Icons.Default.PlayArrow
                                EmergencyStatus.RESOLVED -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = record.type.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = record.type.label,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = record.type.color
                        )
                        
                        Text(
                            text = record.status.label,
                            fontSize = 14.sp,
                            color = record.status.color
                        )
                    }
                }
                
                Text(
                    text = dateFormat.format(record.callTime),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = record.patientName,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = record.location,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            
            if (record.resolvedTime != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "處理時間: ${calculateTimeDifference(record.callTime, record.resolvedTime!!)}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
            
            if (record.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = record.notes,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// 計算時間差
fun calculateTimeDifference(startTime: Date, endTime: Date): String {
    val diffMillis = endTime.time - startTime.time
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    
    return if (minutes < 60) {
        "$minutes 分鐘"
    } else {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        "$hours 小時 $remainingMinutes 分鐘"
    }
} 