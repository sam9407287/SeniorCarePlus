package com.example.myapplication.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// 體溫數據類
data class TemperatureRecord(
    val patientId: String,
    val patientName: String,
    val temperature: Float,
    val timestamp: LocalDateTime,
    val isAbnormal: Boolean = temperature > 37.5f || temperature < 36.0f
)

@Composable
fun TemperatureMonitorScreen(navController: NavController) {
    // 示例數據
    val patients = listOf(
        "張三" to "001",
        "李四" to "002",
        "王五" to "003",
        "趙六" to "004",
        "孫七" to "005"
    )
    
    // 選中的病患
    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    
    // 選中的時間範圍
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("今日", "本週", "本月")
    
    // 模擬體溫記錄
    val temperatureRecords = remember {
        val records = mutableListOf<TemperatureRecord>()
        val now = LocalDateTime.now()
        val patient = patients[selectedPatientIndex]
        
        // 今日數據（每2小時一條）
        for (hour in 0..23 step 2) {
            val time = now.withHour(hour).withMinute(0)
            // 模擬一個合理的體溫範圍內的波動
            val temp = 36.5f + (Random.nextFloat() - 0.5f) * if ((0..10).random() > 8) 2.0f else 0.5f
            records.add(
                TemperatureRecord(
                    patientId = patient.second,
                    patientName = patient.first,
                    temperature = temp,
                    timestamp = time
                )
            )
        }
        records
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 頂部標題
        Text(
            text = "體溫監測",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
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
                    tint = Color(0xFF4169E1),
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
        
        // 時間範圍選項卡
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 體溫圖表
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "體溫趨勢圖",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    TemperatureChart(temperatureRecords = temperatureRecords)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 體溫記錄列表
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "體溫記錄",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Divider()
                
                LazyColumn {
                    items(temperatureRecords.sortedByDescending { it.timestamp }) { record ->
                        TemperatureRecordItem(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun TemperatureChart(temperatureRecords: List<TemperatureRecord>) {
    // 排序記錄，按時間順序
    val sortedRecords = temperatureRecords.sortedBy { it.timestamp }
    
    if (sortedRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "無體溫數據",
                color = Color.Gray
            )
        }
        return
    }
    
    // 獲取最高和最低體溫，添加一些邊界
    val minTemp = sortedRecords.minOfOrNull { it.temperature }?.minus(0.5f) ?: 35.5f
    val maxTemp = sortedRecords.maxOfOrNull { it.temperature }?.plus(0.5f) ?: 39.5f
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val height = size.height
        val width = size.width
        
        val yAxisWidth = 50f
        val xAxisHeight = 50f
        
        val chartHeight = height - xAxisHeight
        val chartWidth = width - yAxisWidth
        
        val verticalStepSize = chartHeight / (maxTemp - minTemp)
        val horizontalStepSize = chartWidth / (sortedRecords.size - 1).coerceAtLeast(1)
        
        // 畫Y軸
        drawLine(
            color = Color.LightGray,
            start = Offset(yAxisWidth, 0f),
            end = Offset(yAxisWidth, chartHeight),
            strokeWidth = 1f
        )
        
        // 畫X軸
        drawLine(
            color = Color.LightGray,
            start = Offset(yAxisWidth, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 1f
        )
        
        // Y軸標籤
        val ySteps = 5
        val yRange = maxTemp - minTemp
        val yStepValue = yRange / ySteps
        
        for (i in 0..ySteps) {
            val y = chartHeight - (i * yRange / ySteps) * verticalStepSize
            val temp = minTemp + (i * yStepValue)
            
            drawLine(
                color = Color.LightGray,
                start = Offset(yAxisWidth, y),
                end = Offset(width, y),
                strokeWidth = 0.5f
            )
            
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    String.format("%.1f", temp),
                    5f,
                    y + 5f,
                    Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.LEFT
                    }
                )
            }
        }
        
        // X軸標籤
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val xLabelCount = 4
        val xStep = sortedRecords.size / xLabelCount.coerceAtLeast(1)
        
        for (i in 0 until sortedRecords.size step xStep.coerceAtLeast(1)) {
            val x = yAxisWidth + i * horizontalStepSize
            val time = sortedRecords[i].timestamp.format(timeFormatter)
            
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    time,
                    x,
                    height - 10f,
                    Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
        
        // 正常範圍區域
        val normalLowY = chartHeight - (36.0f - minTemp) * verticalStepSize
        val normalHighY = chartHeight - (37.5f - minTemp) * verticalStepSize
        
        drawRect(
            color = Color(0x1A4CAF50),
            topLeft = Offset(yAxisWidth, normalHighY),
            size = androidx.compose.ui.geometry.Size(chartWidth, normalLowY - normalHighY)
        )
        
        // 畫折線
        val points = sortedRecords.mapIndexed { index, record ->
            val x = yAxisWidth + index * horizontalStepSize
            val y = chartHeight - (record.temperature - minTemp) * verticalStepSize
            Offset(x, y)
        }
        
        for (i in 0 until points.size - 1) {
            val startPoint = points[i]
            val endPoint = points[i + 1]
            
            drawLine(
                color = Color(0xFF4169E1),
                start = startPoint,
                end = endPoint,
                strokeWidth = 2f
            )
        }
        
        // 畫數據點
        points.forEachIndexed { index, offset ->
            val record = sortedRecords[index]
            val pointColor = if (record.isAbnormal) Color.Red else Color(0xFF4169E1)
            
            drawCircle(
                color = pointColor,
                radius = 4f,
                center = offset
            )
        }
    }
}

@Composable
fun TemperatureRecordItem(record: TemperatureRecord) {
    val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    val formattedTime = record.timestamp.format(timeFormatter)
    
    val temperatureColor = when {
        record.temperature > 38.0f -> Color.Red
        record.temperature > 37.5f -> Color(0xFFFF9800) // Orange
        record.temperature < 36.0f -> Color(0xFF2196F3) // Blue
        else -> Color(0xFF4CAF50) // Green
    }
    
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
                .background(temperatureColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                tint = temperatureColor
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formattedTime,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "${String.format("%.1f", record.temperature)}°C",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = temperatureColor
            )
        }
        
        Text(
            text = if (record.isAbnormal) "異常" else "正常",
            color = if (record.isAbnormal) Color.Red else Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
    
    Divider(color = Color.LightGray.copy(alpha = 0.5f))
} 