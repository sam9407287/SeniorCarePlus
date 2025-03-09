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
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.myapplication.ui.theme.DarkCardBackground
import com.example.myapplication.ui.theme.DarkChartBackground
import com.example.myapplication.ui.theme.DarkChartLine
import com.example.myapplication.ui.theme.LightCardBackground
import com.example.myapplication.ui.theme.LightChartBackground
import com.example.myapplication.ui.theme.LightChartLine
import com.example.myapplication.ui.theme.ThemeManager
import kotlin.random.Random

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
    // 判断是否为深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    
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
    
    // 模擬心率記錄 - 增加更多记录使页面可滚动
    val heartRateRecords = remember {
        val records = mutableListOf<HeartRateRecord>()
        val now = LocalDateTime.now()
        val patient = patients[selectedPatientIndex]
        
        // 模拟更多数据 - 过去7天每小时一条记录
        for (day in 0..6) {
            for (hour in 0..23) {
                val time = now.minusDays(day.toLong()).withHour(hour).withMinute(0)
                val rate = 70 + Random.nextInt(-20, 30)
                records.add(
                    HeartRateRecord(
                        patientId = patient.second,
                        patientName = patient.first,
                        heartRate = rate,
                        timestamp = time
                    )
                )
            }
        }
        records
    }
    
    // 使用LazyColumn替代Column让整个页面可以滚动
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
                    text = "心率監測",
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
                        contentDescription = "切換主題",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // 病患选择
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
                            contentDescription = "患者",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "患者: ${patients[selectedPatientIndex].first}",
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
                                contentDescription = "選擇患者",
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
        
        // 心率趋势图表
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "心率趨勢圖",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "目標心率: ${targetHeartRate.toInt()} BPM",
                            fontSize = 16.sp,
                            color = if (isDarkTheme) DarkChartLine else LightChartLine
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 滑块用于设置目标心率
                    Slider(
                        value = targetHeartRate,
                        onValueChange = { targetHeartRate = it },
                        valueRange = 40f..130f,
                        steps = 90,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDarkTheme) DarkChartLine else LightChartLine,
                            activeTrackColor = if (isDarkTheme) DarkChartLine else LightChartLine
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        HeartRateChart(
                            heartRateRecords = heartRateRecords, 
                            targetHeartRate = targetHeartRate.toInt(),
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 心率记录列表
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
                        text = "心率記錄",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Divider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = if (isDarkTheme) Color.DarkGray else Color.LightGray
                    )
                    
                    // 在Card内使用LazyColumn显示记录
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(heartRateRecords.sortedByDescending { it.timestamp }) { record ->
                            HeartRateRecordItem(record = record, isDarkTheme = isDarkTheme)
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = if (isDarkTheme) Color.DarkGray.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f)
                            )
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
}

@Composable
fun HeartRateChart(heartRateRecords: List<HeartRateRecord>, targetHeartRate: Int, isDarkTheme: Boolean) {
    // 排序記錄，按時間順序
    val sortedRecords = heartRateRecords.sortedBy { it.timestamp }
    
    if (sortedRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "無心率數據",
                color = if (isDarkTheme) Color.LightGray else Color.Gray
            )
        }
        return
    }
    
    // 獲取最高和最低心率，添加一些邊界
    val minRate = (sortedRecords.minOfOrNull { it.heartRate }?.minus(10) ?: 40).coerceAtLeast(40)
    val maxRate = (sortedRecords.maxOfOrNull { it.heartRate }?.plus(10) ?: 130).coerceAtMost(130)
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) DarkChartBackground else LightChartBackground)
    ) {
        val height = size.height
        val width = size.width
        
        val yAxisWidth = 50f
        val xAxisHeight = 50f
        
        val chartHeight = height - xAxisHeight
        val chartWidth = width - yAxisWidth
        
        val verticalStepSize = chartHeight / (maxRate - minRate)
        val horizontalStepSize = chartWidth / (sortedRecords.size - 1).coerceAtLeast(1)
        
        // 设置网格和轴线颜色
        val gridColor = if (isDarkTheme) Color(0xFF444444) else Color.LightGray
        val textColor = if (isDarkTheme) Color(0xFFCCCCCC) else Color.DarkGray
        
        // 畫Y軸
        drawLine(
            color = gridColor,
            start = Offset(yAxisWidth, 0f),
            end = Offset(yAxisWidth, chartHeight),
            strokeWidth = 1f
        )
        
        // 畫X軸
        drawLine(
            color = gridColor,
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
                color = gridColor,
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
                        color = textColor.toArgb()
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
                        color = textColor.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
        
        // 目标心率线
        val targetY = chartHeight - (targetHeartRate - minRate) * verticalStepSize
        drawLine(
            color = if (isDarkTheme) Color(0xFF4FC3F7) else Color(0xFF2196F3),
            start = Offset(yAxisWidth, targetY),
            end = Offset(width, targetY),
            strokeWidth = 2f
        )
        
        // 绘制点和线
        val points = sortedRecords.mapIndexed { index, record ->
            val x = yAxisWidth + index * horizontalStepSize
            val y = chartHeight - (record.heartRate - minRate) * verticalStepSize
            Offset(x, y)
        }
        
        // 绘制整个折线图下方的填充
        val belowCurvePath = Path()
        belowCurvePath.moveTo(yAxisWidth, chartHeight)
        for (point in points) {
            belowCurvePath.lineTo(point.x, point.y)
        }
        belowCurvePath.lineTo(width, chartHeight)
        belowCurvePath.close()
        
        // 使用根据主题调整的填充颜色
        drawPath(
            path = belowCurvePath,
            color = if (isDarkTheme) 
                DarkChartLine.copy(alpha = 0.15f) 
            else 
                Color(0x33E91E63) // 浅色模式保持原来的淡红色
        )
        
        // 畫折線
        for (i in 0 until points.size - 1) {
            drawLine(
                color = if (isDarkTheme) DarkChartLine else LightChartLine,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.5f
            )
        }
        
        // 畫數據點
        points.forEachIndexed { index, offset ->
            val record = sortedRecords[index]
            // 根据深色模式调整点的颜色
            val pointColor = if (record.isAbnormal) {
                if (isDarkTheme) Color(0xFFFF5252) else Color.Red
            } else {
                if (isDarkTheme) DarkChartLine else LightChartLine
            }
            
            drawCircle(
                color = pointColor,
                radius = 4f,
                center = offset
            )
        }
    }
}

@Composable
fun HeartRateRecordItem(record: HeartRateRecord, isDarkTheme: Boolean) {
    val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    val formattedTime = record.timestamp.format(timeFormatter)
    
    // 根据深色模式调整颜色
    val heartRateColor = when {
        record.heartRate > 100 -> if (isDarkTheme) Color(0xFFFF5252) else Color.Red
        record.heartRate < 60 -> if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3) // Blue
        else -> if (isDarkTheme) Color(0xFF81C784) else Color(0xFF4CAF50) // Green
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
                .background(heartRateColor.copy(alpha = if (isDarkTheme) 0.3f else 0.2f)),
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
            color = if (record.isAbnormal) {
                if (isDarkTheme) Color(0xFFFF5252) else Color.Red
            } else {
                if (isDarkTheme) Color(0xFF81C784) else Color(0xFF4CAF50)
            },
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
} 