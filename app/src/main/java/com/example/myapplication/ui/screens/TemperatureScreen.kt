package com.example.myapplication.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Path
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
import com.example.myapplication.ui.theme.DarkCardBackground
import com.example.myapplication.ui.theme.DarkCardContentBackground
import com.example.myapplication.ui.theme.DarkChartBackground
import com.example.myapplication.ui.theme.DarkChartLine
import com.example.myapplication.ui.theme.LightCardBackground
import com.example.myapplication.ui.theme.LightChartBackground
import com.example.myapplication.ui.theme.LightChartLine
import com.example.myapplication.ui.theme.ThemeManager

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
    
    // 模擬體溫記錄 - 增加更多记录使页面可滚动
    val temperatureRecords = remember {
        val records = mutableListOf<TemperatureRecord>()
        val now = LocalDateTime.now()
        val patient = patients[selectedPatientIndex]
        
        // 模拟更多数据 - 过去7天每2小时一条记录
        for (day in 0..6) {
            for (hour in 0..23 step 2) {
                val time = now.minusDays(day.toLong()).withHour(hour).withMinute(0)
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
            Text(
                text = "體溫監測",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
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
        
        // 体温趋势图表 - 保持原来的高度
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
                    Text(
                        text = "體溫趨勢圖",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        TemperatureChart(temperatureRecords = temperatureRecords, isDarkTheme = isDarkTheme)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 体温记录部分 - 使用Card包含，并设置足够长的固定高度
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp) // 设置为足够长的固定高度
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
                        text = "體溫記錄",
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
                        items(temperatureRecords.sortedByDescending { it.timestamp }) { record ->
                            TemperatureRecordItem(record = record, isDarkTheme = isDarkTheme)
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
fun TemperatureChart(temperatureRecords: List<TemperatureRecord>, isDarkTheme: Boolean) {
    // 排序記錄，按時間順序
    val sortedRecords = temperatureRecords.sortedBy { it.timestamp }
    
    if (sortedRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "無體溫數據",
                color = if (isDarkTheme) Color.LightGray else Color.Gray
            )
        }
        return
    }
    
    // 獲取最高和最低體溫，添加一些邊界
    val minTemp = sortedRecords.minOfOrNull { it.temperature }?.minus(0.5f) ?: 35.5f
    val maxTemp = sortedRecords.maxOfOrNull { it.temperature }?.plus(0.5f) ?: 39.5f
    
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
        
        val verticalStepSize = chartHeight / (maxTemp - minTemp)
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
        val yRange = maxTemp - minTemp
        val yStepValue = yRange / ySteps
        
        for (i in 0..ySteps) {
            val y = chartHeight - (i * yRange / ySteps) * verticalStepSize
            val temp = minTemp + (i * yStepValue)
            
            drawLine(
                color = gridColor,
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
                        color = textColor.toArgb()
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
                        color = textColor.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
        
        // 正常範圍區域
        val normalLowY = chartHeight - (36.0f - minTemp) * verticalStepSize
        val normalHighY = chartHeight - (37.5f - minTemp) * verticalStepSize
        
        // 畫折線
        val points = sortedRecords.mapIndexed { index, record ->
            val x = yAxisWidth + index * horizontalStepSize
            val y = chartHeight - (record.temperature - minTemp) * verticalStepSize
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
                Color(0x55FFB6C1) // 浅色模式保持原来的淡红色
        )
        
        // 畫折線
        for (i in 0 until points.size - 1) {
            val startPoint = points[i]
            val endPoint = points[i + 1]
            
            drawLine(
                color = if (isDarkTheme) DarkChartLine else LightChartLine,
                start = startPoint,
                end = endPoint,
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
fun TemperatureRecordItem(record: TemperatureRecord, isDarkTheme: Boolean) {
    val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    val formattedTime = record.timestamp.format(timeFormatter)
    
    // 根据深色模式调整颜色
    val temperatureColor = when {
        record.temperature > 38.0f -> if (isDarkTheme) Color(0xFFFF5252) else Color.Red
        record.temperature > 37.5f -> if (isDarkTheme) Color(0xFFFFB74D) else Color(0xFFFF9800) // Orange
        record.temperature < 36.0f -> if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3) // Blue
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
                .background(temperatureColor.copy(alpha = if (isDarkTheme) 0.3f else 0.2f)),
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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