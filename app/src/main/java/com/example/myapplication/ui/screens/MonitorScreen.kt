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
import androidx.compose.material.icons.filled.Bathroom
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.PauseCircle
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
enum class AlertType(val displayName: String) {
    TEMPERATURE("體溫"),
    HEART_RATE("心率"),
    DIAPER("尿濕"),
    CALL("呼叫"),
    TIMER("定時"),
    BED_EXIT("離床"),
    AREA_EXIT("離區"),
    LOW_POSITION("低地"),
    STILL("靜止")
}

// 過濾選項
enum class FilterOption {
    ALL, ABNORMAL, NORMAL
}

@Composable
fun MonitorScreen(navController: NavController = rememberNavController()) {
    // 示例病患資料
    val patients = remember {
        listOf(
            Patient(
                name = "張三", 
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
                name = "李四", 
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
                name = "王五", 
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
                name = "趙六", 
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
                name = "孫七", 
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
            .padding(16.dp)
    ) {
        // 頁面標題
        Text(
            text = "監控",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 過濾選項
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            MonitorFilterButton(
                text = "All",
                isSelected = currentFilter == FilterOption.ALL,
                onClick = { currentFilter = FilterOption.ALL }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            MonitorFilterButton(
                text = "Abnormal",
                isSelected = currentFilter == FilterOption.ABNORMAL,
                onClick = { currentFilter = FilterOption.ABNORMAL }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            MonitorFilterButton(
                text = "Normal",
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) Color(0xFFE0E0E0) else Color(0xFFF5F5F5))
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray,
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
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
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
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
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
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Age: ${patient.age}, GCS: ${patient.gcs}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            // 狀態指示燈
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (patient.alerts.values.any { it }) Color.Green else Color.Gray)
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
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEEEEEE))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 根據警報類型顯示不同的圖標
        when (alertType) {
            AlertType.TEMPERATURE -> Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFFFF5722) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.HEART_RATE -> Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFFE91E63) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.DIAPER -> Icon(
                imageVector = Icons.Default.Bathroom,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFF9C27B0) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.CALL -> Icon(
                imageVector = Icons.Default.Call,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFF2196F3) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.TIMER -> Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.BED_EXIT -> Icon(
                imageVector = Icons.Default.Bed,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFF795548) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.AREA_EXIT -> Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFFFF9800) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.LOW_POSITION -> Icon(
                imageVector = Icons.Default.LowPriority,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFF607D8B) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            AlertType.STILL -> Icon(
                imageVector = Icons.Default.PauseCircle,
                contentDescription = alertType.displayName,
                tint = if (isActive) Color(0xFF673AB7) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 