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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 心率數據類
data class HeartRateRecord(
    val patientId: String,
    val patientName: String,
    val heartRate: Int,
    val timestamp: LocalDateTime,
    val isAbnormal: Boolean = heartRate > 100 || heartRate < 60
)

@Composable
fun HeartRateMonitorScreen(navController: NavController) {
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
    
    // 參考心率值
    var targetHeartRate by remember { mutableFloatStateOf(75f) }
    
    // 模擬心率記錄
    val heartRateRecords = remember {
        val records = mutableListOf<HeartRateRecord>()
        val now = LocalDateTime.now()
        val patient = patients[selectedPatientIndex]
        
        // 今日數據（每小時一條）
        for (hour in 0..23) {
            val time = now.withHour(hour).withMinute(0)
            // 模擬一個合理的心率範圍內的波動
            val heartRate = 75 + (-15..15).random() + if ((0..10).random() > 8) (20..40).random() * (if ((0..1).random() == 0) 1 else -1) else 0
            records.add(
                HeartRateRecord(
                    patientId = patient.second,
                    patientName = patient.first,
                    heartRate = heartRate,
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
            text = "心率監測",
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
                    tint = Color(0xFFE91E63),
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
        
        // 心率圖表
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "心率趨勢圖",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "目標心率: ${targetHeartRate.toInt()} BPM",
                        fontSize = 14.sp,
                        color = Color(0xFFE91E63)
                    )
                }
                
                Slider(
                    value = targetHeartRate,
                    onValueChange = { targetHeartRate = it },
                    valueRange = 50f..120f,
                    steps = 70,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    HeartRateChart(
                        heartRateRecords = heartRateRecords,
                        targetHeartRate = targetHeartRate.toInt()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 心率記錄列表
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
                    text = "心率記錄",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Divider()
                
                LazyColumn {
                    items(heartRateRecords.sortedByDescending { it.timestamp }) { record ->
                        HeartRateRecordItem(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun HeartRateChart(heartRateRecords: List<HeartRateRecord>, targetHeartRate: Int) {
    // 排序記錄，按時間順序
    val sortedRecords = heartRateRecords.sortedBy { it.timestamp }
    
    if (sortedRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "無心率數據",
                color = Color.Gray
            )
        }
        return
    }
    
    // 獲取最高和最低心率，添加一些邊界
    val minRate = (sortedRecords.minOfOrNull { it.heartRate }?.minus(10) ?: 50).coerceAtLeast(40)
    val maxRate = (sortedRecords.maxOfOrNull { it.heartRate }?.plus(10) ?: 100).coerceAtMost(180)
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val height = size.height
        val width = size.width
        
        val yAxisWidth = 50f
        val xAxisHeight = 40f
        
        val chartHeight = height - xAxisHeight
        val chartWidth = width - yAxisWidth
        
        val verticalStepSize = chartHeight / (maxRate - minRate)
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
        val yRange = maxRate - minRate
        val yStepValue = yRange / ySteps
        
        for (i in 0..ySteps) {
            val y = chartHeight - (i * yRange / ySteps) * verticalStepSize
            val rate = minRate + (i * yStepValue)
            
            drawLine(
                color = Color.LightGray,
                start = Offset(yAxisWidth, y),
                end = Offset(width, y),
                strokeWidth = 0.5f
            )
            
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "$rate",
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
        val xLabelCount = 6
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
        val normalLowY = chartHeight - (60 - minRate) * verticalStepSize
        val normalHighY = chartHeight - (100 - minRate) * verticalStepSize
        
        drawRect(
            color = Color(0x1AE91E63),
            topLeft = Offset(yAxisWidth, normalHighY),
            size = androidx.compose.ui.geometry.Size(chartWidth, normalLowY - normalHighY)
        )
        
        // 目標心率線
        val targetY = chartHeight - (targetHeartRate - minRate) * verticalStepSize
        drawLine(
            color = Color(0xFFE91E63),
            start = Offset(yAxisWidth, targetY),
            end = Offset(width, targetY),
            strokeWidth = 1f
        )
        
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                "目標",
                width - 5f,
                targetY - 5f,
                Paint().apply {
                    color = Color(0xFFE91E63).toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                }
            )
        }
        
        // 畫填充區域
        val points = sortedRecords.mapIndexed { index, record ->
            val x = yAxisWidth + index * horizontalStepSize
            val y = chartHeight - (record.heartRate - minRate) * verticalStepSize
            Offset(x, y)
        }
        
        // 創建填充路徑
        val path = Path()
        path.moveTo(yAxisWidth, chartHeight) // 從左下角開始
        points.forEach { path.lineTo(it.x, it.y) } // 添加每個點
        path.lineTo(width, chartHeight) // 到右下角
        path.close() // 閉合路徑
        
        // 畫填充區域
        drawPath(
            path = path,
            color = Color(0x33E91E63)
        )
        
        // 畫折線
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFFE91E63),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2f
            )
        }
        
        // 畫數據點
        points.forEachIndexed { index, offset ->
            val record = sortedRecords[index]
            val pointColor = if (record.isAbnormal) Color.Red else Color(0xFFE91E63)
            
            drawCircle(
                color = pointColor,
                radius = 4f,
                center = offset
            )
        }
    }
}

@Composable
fun HeartRateRecordItem(record: HeartRateRecord) {
    val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    val formattedTime = record.timestamp.format(timeFormatter)
    
    val heartRateColor = when {
        record.heartRate > 120 -> Color.Red
        record.heartRate > 100 -> Color(0xFFFF9800) // Orange
        record.heartRate < 50 -> Color.Red
        record.heartRate < 60 -> Color(0xFF2196F3) // Blue
        else -> Color(0xFFE91E63) // Pink
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
                .background(heartRateColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = heartRateColor
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
                text = "${record.heartRate} BPM",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = heartRateColor
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