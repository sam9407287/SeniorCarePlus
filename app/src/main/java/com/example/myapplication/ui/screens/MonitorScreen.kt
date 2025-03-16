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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.ThemeManager
import com.example.myapplication.ui.theme.LanguageManager

// 病患資料類
data class Patient(
    val name: String,
    val age: Int,
    val gcs: Int,
    val alerts: Map<AlertType, Boolean> = mapOf(
        AlertType.TEMPERATURE to false,
        AlertType.HEART_RATE to false,
        AlertType.DIAPER to false,
        AlertType.CALL to false,
        AlertType.TIMER to false,
        AlertType.BED_EXIT to false,
        AlertType.AREA_EXIT to false,
        AlertType.LOW_POSITION to false,
        AlertType.STILL to false
    )
)

// 警報類型枚舉
enum class AlertType(val chineseName: String, val englishName: String) {
    TEMPERATURE("體溫", "Temperature"),
    HEART_RATE("心率", "Heart Rate"),
    DIAPER("尿濕", "Diaper"),
    CALL("呼叫", "Call"),
    TIMER("定時", "Timer"),
    BED_EXIT("離床", "Bed Exit"),
    AREA_EXIT("離區", "Area Exit"),
    LOW_POSITION("低地", "Low Position"),
    STILL("靜止", "Still");
    
    val displayName: String
        get() = if (LanguageManager.isChineseLanguage) chineseName else englishName
}

// 過濾選項
enum class FilterOption {
    ALL, ABNORMAL, NORMAL
}

@Composable
fun MonitorScreen(navController: NavController = rememberNavController()) {
    // 檢查深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 示例病患資料
    val patients = remember {
        val isChineseLanguage = LanguageManager.isChineseLanguage
        
        listOf(
            Patient(
                name = if (isChineseLanguage) "張三" else "Zhang San", 
                age = 80, 
                gcs = 8,
                alerts = mapOf(
                    AlertType.TEMPERATURE to true,
                    AlertType.HEART_RATE to false,
                    AlertType.DIAPER to true,
                    AlertType.CALL to false,
                    AlertType.TIMER to false,
                    AlertType.BED_EXIT to false,
                    AlertType.AREA_EXIT to true,
                    AlertType.LOW_POSITION to false,
                    AlertType.STILL to false
                )
            ),
            Patient(
                name = if (isChineseLanguage) "李四" else "Li Si", 
                age = 70, 
                gcs = 13,
                alerts = mapOf(
                    AlertType.TEMPERATURE to false,
                    AlertType.HEART_RATE to true,
                    AlertType.DIAPER to false,
                    AlertType.CALL to true,
                    AlertType.TIMER to false,
                    AlertType.BED_EXIT to false,
                    AlertType.AREA_EXIT to false,
                    AlertType.LOW_POSITION to false,
                    AlertType.STILL to false
                )
            ),
            Patient(
                name = if (isChineseLanguage) "王五" else "Wang Wu", 
                age = 88, 
                gcs = 12,
                alerts = mapOf(
                    AlertType.TEMPERATURE to false,
                    AlertType.HEART_RATE to false,
                    AlertType.DIAPER to false,
                    AlertType.CALL to false,
                    AlertType.TIMER to true,
                    AlertType.BED_EXIT to true,
                    AlertType.AREA_EXIT to false,
                    AlertType.LOW_POSITION to false,
                    AlertType.STILL to true
                )
            ),
            Patient(
                name = if (isChineseLanguage) "趙六" else "Zhao Liu", 
                age = 72, 
                gcs = 14,
                alerts = mapOf(
                    AlertType.TEMPERATURE to true,
                    AlertType.HEART_RATE to true,
                    AlertType.DIAPER to false,
                    AlertType.CALL to false,
                    AlertType.TIMER to false,
                    AlertType.BED_EXIT to false,
                    AlertType.AREA_EXIT to false,
                    AlertType.LOW_POSITION to true,
                    AlertType.STILL to false
                )
            ),
            Patient(
                name = if (isChineseLanguage) "孫七" else "Sun Qi", 
                age = 81, 
                gcs = 8,
                alerts = mapOf(
                    AlertType.TEMPERATURE to false,
                    AlertType.HEART_RATE to false,
                    AlertType.DIAPER to true,
                    AlertType.CALL to false,
                    AlertType.TIMER to false,
                    AlertType.BED_EXIT to false,
                    AlertType.AREA_EXIT to false,
                    AlertType.LOW_POSITION to false,
                    AlertType.STILL to false
                )
            )
        )
    }
    
    // 過濾選項狀態
    var currentFilter by remember { mutableStateOf(FilterOption.ALL) }
    
    // 根據過濾選項過濾病患
    val filteredPatients = when (currentFilter) {
        FilterOption.ALL -> patients
        FilterOption.ABNORMAL -> patients.filter { patient ->
            patient.alerts.values.any { it }
        }
        FilterOption.NORMAL -> patients.filter { patient ->
            patient.alerts.values.none { it }
        }
    }
    
    // 監控頁面主佈局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 頁面標題
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 根据语言显示标题
            val isChineseLanguage = LanguageManager.isChineseLanguage
            
            Text(
                text = if (isChineseLanguage) "監控" else "Monitoring",
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
        
        // 過濾選項
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            val isChineseLanguage = LanguageManager.isChineseLanguage
            
            MonitorFilterButton(
                text = if (isChineseLanguage) "全部" else "All",
                isSelected = currentFilter == FilterOption.ALL,
                onClick = { currentFilter = FilterOption.ALL }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            MonitorFilterButton(
                text = if (isChineseLanguage) "異常" else "Abnormal",
                isSelected = currentFilter == FilterOption.ABNORMAL,
                onClick = { currentFilter = FilterOption.ABNORMAL }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            MonitorFilterButton(
                text = if (isChineseLanguage) "正常" else "Normal",
                isSelected = currentFilter == FilterOption.NORMAL,
                onClick = { currentFilter = FilterOption.NORMAL }
            )
        }
        
        // 病患列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPatients) { patient ->
                PatientCard(
                    patient = patient,
                    onAlertClick = { patientName, alertType ->
                        // 點擊警報時導航到相應的監控頁面
                        when (alertType) {
                            AlertType.TEMPERATURE -> navController.navigate("temperature_monitor")
                            AlertType.HEART_RATE -> navController.navigate("heart_rate_monitor")
                            AlertType.DIAPER -> navController.navigate("diaper_monitor")
                            else -> {} // 其他警報類型暫未實現詳細頁面
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MonitorFilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PatientCard(
    patient: Patient,
    onAlertClick: (String, AlertType) -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
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
            // 病患頭像（使用默認Person圖標代替實際頭像）
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isDarkTheme) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        Color.Gray,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 病患信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = patient.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (isChineseLanguage) 
                        "年齡: ${patient.age}, GCS: ${patient.gcs}" 
                    else 
                        "Age: ${patient.age}, GCS: ${patient.gcs}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            
            // 狀態指示燈
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (patient.alerts.values.any { it }) Color.Green else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            )
        }
        
        // 警報圖標網格
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 第一行警報圖標
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AlertIcon(
                    alertType = AlertType.TEMPERATURE,
                    isActive = patient.alerts[AlertType.TEMPERATURE] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.TEMPERATURE) }
                )
                
                AlertIcon(
                    alertType = AlertType.HEART_RATE,
                    isActive = patient.alerts[AlertType.HEART_RATE] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.HEART_RATE) }
                )
                
                AlertIcon(
                    alertType = AlertType.DIAPER,
                    isActive = patient.alerts[AlertType.DIAPER] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.DIAPER) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 第二行警報圖標
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AlertIcon(
                    alertType = AlertType.CALL,
                    isActive = patient.alerts[AlertType.CALL] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.CALL) }
                )
                
                AlertIcon(
                    alertType = AlertType.TIMER,
                    isActive = patient.alerts[AlertType.TIMER] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.TIMER) }
                )
                
                AlertIcon(
                    alertType = AlertType.BED_EXIT,
                    isActive = patient.alerts[AlertType.BED_EXIT] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.BED_EXIT) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 第三行警報圖標
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AlertIcon(
                    alertType = AlertType.AREA_EXIT,
                    isActive = patient.alerts[AlertType.AREA_EXIT] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.AREA_EXIT) }
                )
                
                AlertIcon(
                    alertType = AlertType.LOW_POSITION,
                    isActive = patient.alerts[AlertType.LOW_POSITION] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.LOW_POSITION) }
                )
                
                AlertIcon(
                    alertType = AlertType.STILL,
                    isActive = patient.alerts[AlertType.STILL] ?: false,
                    onClick = { onAlertClick(patient.name, AlertType.STILL) }
                )
            }
        }
    }
}

@Composable
fun AlertIcon(
    alertType: AlertType,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkTheme) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
            else 
                Color(0xFFEEEEEE))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 根據警報類型顯示不同的圖標，並在深色模式下調整顏色
        when (alertType) {
            AlertType.TEMPERATURE -> Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFFF5722)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.HEART_RATE -> Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFF48FB1) else Color(0xFFE91E63)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.DIAPER -> Icon(
                imageVector = Icons.Default.BabyChangingStation,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFCE93D8) else Color(0xFF9C27B0)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.CALL -> Icon(
                imageVector = Icons.Default.Call,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF2196F3)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.TIMER -> Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF4CAF50)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.BED_EXIT -> Icon(
                imageVector = Icons.Default.Bed,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFFFCC80) else Color(0xFFFF9800)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.AREA_EXIT -> Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFFFD54F) else Color(0xFFFFC107)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.LOW_POSITION -> Icon(
                imageVector = Icons.Default.LowPriority,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFF9FA8DA) else Color(0xFF3F51B5)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.STILL -> Icon(
                imageVector = Icons.Default.PauseCircle,
                contentDescription = alertType.displayName,
                tint = if (isActive) 
                    if (isDarkTheme) Color(0xFFB39DDB) else Color(0xFF673AB7)
                else 
                    if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 