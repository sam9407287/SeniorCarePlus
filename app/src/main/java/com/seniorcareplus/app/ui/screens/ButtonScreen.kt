package com.seniorcareplus.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DarkMode
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
import androidx.compose.material3.RadioButton
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.seniorcareplus.app.ui.theme.DarkCardBackground
import com.seniorcareplus.app.ui.theme.LightCardBackground
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager

// 緊急類型
enum class EmergencyType(val chineseLabel: String, val englishLabel: String, val color: Color) {
    FALL("跌倒", "Fall", Color(0xFFE53935)),
    PAIN("疼痛", "Pain", Color(0xFFFF9800)),
    TOILET("如廁協助", "Toilet Assistance", Color(0xFF4CAF50)),
    MEDICATION("藥物協助", "Medication Help", Color(0xFF2196F3)),
    OTHER("其他協助", "Other Assistance", Color(0xFF9C27B0))
}

// 根據語言設置獲取標籤
val EmergencyType.label: String
    get() = if (LanguageManager.isChineseLanguage) this.chineseLabel else this.englishLabel

// 緊急狀態
enum class EmergencyStatus(val chineseLabel: String, val englishLabel: String, val color: Color) {
    WAITING("等待響應", "Waiting", Color(0xFFE53935)),
    RESPONDING("正在處理", "Responding", Color(0xFF1565C0)),
    RESOLVED("已解決", "Resolved", Color(0xFF2E7D32))
}

// 根據語言設置獲取標籤
val EmergencyStatus.label: String
    get() = if (LanguageManager.isChineseLanguage) this.chineseLabel else this.englishLabel

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
    
    val locations = if (isChineseLanguage) {
        listOf("A區 101房", "A區 102房", "B區 201房", "B區 202房", "C區 301房")
    } else {
        listOf("Area A Room 101", "Area A Room 102", "Area B Room 201", "Area B Room 202", "Area C Room 301")
    }
    
    val responders = if (isChineseLanguage) {
        listOf("護工A", "護工B", "護工C", "護工D")
    } else {
        listOf("Caregiver A", "Caregiver B", "Caregiver C", "Caregiver D")
    }
    
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 頂部標題
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "緊急呼叫" else "Emergency Call",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                

            }
        }
        
        // 顯示當前緊急狀態
        item {
            if (isEmergencyActive && activeEmergency != null) {
                EmergencyActiveCard(
                    emergency = activeEmergency!!,
                    onCancel = {
                        showResponseDialog = true
                    },
                    isChineseLanguage = isChineseLanguage
                )
            } else {
                // 病患選擇
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
                
                // 位置選擇
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = if (isChineseLanguage) "位置" else "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = if (isChineseLanguage) "位置: ${locations[selectedLocationIndex]}" else "Location: ${locations[selectedLocationIndex]}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(
                                onClick = { showLocationDropdown = true }
                            ) {
                                Icon(
                                    imageVector = if (showLocationDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isChineseLanguage) "選擇位置" else "Select Location",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showLocationDropdown,
                                onDismissRequest = { showLocationDropdown = false },
                                modifier = Modifier.background(
                                    if (isDarkTheme) 
                                        MaterialTheme.colorScheme.surfaceVariant 
                                    else 
                                        Color.White
                                )
                            ) {
                                locations.forEachIndexed { index, location ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = location,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            selectedLocationIndex = index
                                            showLocationDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 緊急按鈕
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFFEC407A) else Color(0xFFEC407A))
                            .border(
                                BorderStroke(4.dp, if (isDarkTheme) Color(0xFFB71C1C) else Color(0xFFC62828)),
                                CircleShape
                            )
                            .clickable { showEmergencyDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isChineseLanguage) {
                                "緊 急\n呼 叫"
                            } else {
                                "SOS"
                            },
                            fontSize = if (isChineseLanguage) 28.sp else 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = if (isChineseLanguage) 40.sp else 48.sp,
                            letterSpacing = if (isChineseLanguage) 4.sp else 0.sp
                        )
                    }
                }
            }
        }
        
        // 歷史記錄標題
        item {
            Text(
                text = if (isChineseLanguage) "呼叫記錄" else "Call Records",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
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
                        text = if (isChineseLanguage) "暫無呼叫記錄" else "No call records",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(emergencyCallRecords.sortedByDescending { it.callTime }) { record ->
                EmergencyRecordItem(record = record, isDarkTheme = isDarkTheme, isChineseLanguage = isChineseLanguage)
            }
        }
    }
    
    // 緊急類型選擇對話框
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = {
                Text(
                    text = if (isChineseLanguage) "選擇緊急類型" else "Select Emergency Type",
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
                                    status = EmergencyStatus.WAITING
                                )
                                
                                // 更新狀態
                                activeEmergency = newEmergency
                                isEmergencyActive = true
                                emergencyCallRecords = emergencyCallRecords + newEmergency
                                
                                showEmergencyDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) 
                                    type.color.copy(alpha = 0.7f)
                                else 
                                    type.color
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = type.label,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { showEmergencyDialog = false },
                    modifier = Modifier.padding(end = 8.dp).padding(bottom = 8.dp)
                ) {
                    Text(
                        "取消",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = if (ThemeManager.isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
        )
    }
    
    // 響應對話框
    if (showResponseDialog) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            title = {
                Text(
                    text = if (isChineseLanguage) "取消緊急呼叫" else "Cancel Emergency Call",
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
            },
            containerColor = if (ThemeManager.isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
        )
    }
}

@Composable
fun EmergencyActiveCard(
    emergency: EmergencyCallRecord,
    onCancel: () -> Unit,
    isChineseLanguage: Boolean = LanguageManager.isChineseLanguage
) {
    val isResponding = emergency.status == EmergencyStatus.RESPONDING
    val isDarkTheme = ThemeManager.isDarkTheme
    
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
            .padding(horizontal = 16.dp).padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                if (isResponding) Color(0xFF3E2723) else Color(0xFF3F2323)
            } else {
                if (isResponding) Color(0xFFFFF9C4) else Color(0xFFFFEBEE)
            }
        ),
        shape = RoundedCornerShape(16.dp)
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
                    text = if (isChineseLanguage) "正在響應中..." else "Emergency Call in Progress...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) {
                        if (isResponding) Color(0xFFFFB74D) else Color(0xFFEF9A9A)
                    } else {
                        if (isResponding) Color(0xFFFF9800) else Color(0xFFE53935)
                    }
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
                label = if (isChineseLanguage) "患者:" else "Patient:",
                value = emergency.patientName,
                valueColor = if (isDarkTheme) Color.White else Color.Black,
                isDarkTheme = isDarkTheme
            )
            
            EmergencyInfoRow(
                label = if (isChineseLanguage) "位置:" else "Location:",
                value = emergency.location,
                valueColor = if (isDarkTheme) Color.White else Color.Black,
                isDarkTheme = isDarkTheme
            )
            
            EmergencyInfoRow(
                label = if (isChineseLanguage) "類型:" else "Type:",
                value = emergency.type.label,
                valueColor = emergency.type.color,
                isDarkTheme = isDarkTheme
            )
            
            EmergencyInfoRow(
                label = if (isChineseLanguage) "呼叫時間:" else "Call Time:",
                value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(emergency.callTime),
                valueColor = if (isDarkTheme) Color.White else Color.Black,
                isDarkTheme = isDarkTheme
            )
            
            EmergencyInfoRow(
                label = if (isChineseLanguage) "等待時間:" else "Waiting Time:",
                value = displayWaitTime,
                valueColor = if (isDarkTheme) {
                    if (isResponding) Color(0xFFFFB74D) else Color(0xFFEF9A9A)
                } else {
                    if (isResponding) Color(0xFFFF9800) else Color(0xFFE53935)
                },
                isDarkTheme = isDarkTheme
            )
            
            if (isResponding) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Divider(color = if (isDarkTheme) Color.Gray.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                EmergencyInfoRow(
                    label = if (isChineseLanguage) "響應人員:" else "Responder:",
                    value = emergency.responder ?: "",
                    valueColor = if (isDarkTheme) Color.White else Color.Black,
                    isDarkTheme = isDarkTheme
                )
                
                if (emergency.responseTime != null) {
                    EmergencyInfoRow(
                        label = if (isChineseLanguage) "響應時間:" else "Response Time:",
                        value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(emergency.responseTime),
                        valueColor = if (isDarkTheme) Color.White else Color.Black,
                        isDarkTheme = isDarkTheme
                    )
                }
                
                Text(
                    text = "護理人員正趕往現場...",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = if (isDarkTheme) Color(0xFFFFB74D) else Color(0xFFFF9800),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isResponding) {
                        if (isDarkTheme) Color(0xFF757575) else Color(0xFF9E9E9E)
                    } else {
                        if (isDarkTheme) Color(0xFFD32F2F) else Color(0xFFE53935)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = if (isResponding) Icons.Default.Refresh else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (isChineseLanguage) "刷新狀態" else "Refresh Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun EmergencyInfoRow(
    label: String,
    value: String,
    valueColor: Color,
    valueSize: Int = 16,
    isDarkTheme: Boolean = ThemeManager.isDarkTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = valueSize.sp,
            fontWeight = FontWeight.Normal,
            color = if (isDarkTheme) Color.Gray else Color.DarkGray,
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = value,
            fontSize = valueSize.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun EmergencyRecordItem(
    record: EmergencyCallRecord,
    isDarkTheme: Boolean,
    isChineseLanguage: Boolean
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(record.callTime)
    val resolvedTime = if (record.status == EmergencyStatus.RESOLVED && record.resolvedTime != null) {
        dateFormat.format(record.resolvedTime)
    } else {
        if (isChineseLanguage) "未解決" else "Unresolved"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DarkCardBackground else LightCardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 狀態圖標
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when (record.status) {
                                EmergencyStatus.WAITING -> if (isDarkTheme) Color(0xFFAD1457).copy(alpha = 0.7f) else Color(0xFFFCE4EC)
                                EmergencyStatus.RESPONDING -> if (isDarkTheme) Color(0xFF1565C0).copy(alpha = 0.7f) else Color(0xFFE3F2FD)
                                EmergencyStatus.RESOLVED -> if (isDarkTheme) Color(0xFF2E7D32).copy(alpha = 0.7f) else Color(0xFFE8F5E9)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (record.status) {
                            EmergencyStatus.WAITING -> Icons.Default.Warning
                            EmergencyStatus.RESPONDING -> Icons.Default.PlayArrow
                            EmergencyStatus.RESOLVED -> Icons.Default.Check
                        },
                        contentDescription = when (record.status) {
                            EmergencyStatus.WAITING -> if (isChineseLanguage) "等待中" else "Waiting"
                            EmergencyStatus.RESPONDING -> if (isChineseLanguage) "響應中" else "Responding"
                            EmergencyStatus.RESOLVED -> if (isChineseLanguage) "已解決" else "Resolved"
                        },
                        tint = when (record.status) {
                            EmergencyStatus.WAITING -> if (isDarkTheme) Color.White else Color(0xFFE53935)
                            EmergencyStatus.RESPONDING -> if (isDarkTheme) Color.White else Color(0xFF1565C0)
                            EmergencyStatus.RESOLVED -> if (isDarkTheme) Color.White else Color(0xFF2E7D32)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 呼叫類型
                    Text(
                        text = record.type.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = record.type.color
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 狀態文字
                    Text(
                        text = when (record.status) {
                            EmergencyStatus.WAITING -> if (isChineseLanguage) "等待響應" else "Waiting for response"
                            EmergencyStatus.RESPONDING -> if (isChineseLanguage) "正在處理" else "Being processed"
                            EmergencyStatus.RESOLVED -> if (isChineseLanguage) "已解決" else "Resolved"
                        },
                        fontSize = 14.sp,
                        color = when (record.status) {
                            EmergencyStatus.WAITING -> if (isDarkTheme) Color(0xFFE57373) else Color(0xFFE53935)
                            EmergencyStatus.RESPONDING -> if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1565C0)
                            EmergencyStatus.RESOLVED -> if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(
                color = if (isDarkTheme) Color.DarkGray else Color.LightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 患者和位置信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = if (isChineseLanguage) "患者" else "Patient",
                        tint = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = record.patientName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = if (isChineseLanguage) "位置" else "Location",
                        tint = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = record.location,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 時間信息
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = if (isChineseLanguage) "呼叫時間" else "Call Time",
                        tint = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = if (isChineseLanguage) "呼叫時間: $formattedTime" else "Call: $formattedTime",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (record.status == EmergencyStatus.RESOLVED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = if (isChineseLanguage) "處理時間" else "Resolution Time",
                            tint = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = if (isChineseLanguage) "處理時間: $resolvedTime" else "Resolved: $resolvedTime",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // 響應人員信息
            if (record.responder != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChineseLanguage) "處理人員: ${record.responder}" else "Responder: ${record.responder}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
} 