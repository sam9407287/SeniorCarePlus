package com.seniorcareplus.app.ui.screens

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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.seniorcareplus.app.ui.theme.DarkCardBackground
import com.seniorcareplus.app.ui.theme.DarkChartBackground
import com.seniorcareplus.app.ui.theme.DarkChartLine
import com.seniorcareplus.app.ui.theme.LightCardBackground
import com.seniorcareplus.app.ui.theme.LightChartBackground
import com.seniorcareplus.app.ui.theme.LightChartLine
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import com.seniorcareplus.app.ui.components.TimeRangeChip
import com.seniorcareplus.app.ui.components.AbnormalFilterChip

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
    // 檢查語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 示例數據
    val patients = listOf(
        (if (isChineseLanguage) "張三" else "Zhang San") to "001",
        (if (isChineseLanguage) "李四" else "Li Si") to "002",
        (if (isChineseLanguage) "王五" else "Wang Wu") to "003",
        (if (isChineseLanguage) "趙六" else "Zhao Liu") to "004",
        (if (isChineseLanguage) "孫七" else "Sun Qi") to "005"
    )
    
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
    
    // 記錄過濾設置
    var showOnlyAbnormal by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(7) } // 默認顯示7天數據
    
    // 增加異常類型過濾
    var filterType by remember { mutableStateOf(0) } // 0:全部, 1:僅高溫, 2:僅低溫
    
    // 模擬體溫記錄 - 增加更多记录使页面可滚动
    val rawTemperatureRecords = remember {
        val records = mutableListOf<TemperatureRecord>()
        val now = LocalDateTime.now()
        val patient = patients[selectedPatientIndex]
        
        // 模拟更多数据 - 过去7天每30分钟一条记录（增加數據密度，便於分桶處理）
        for (day in 0..6) {
            for (hour in 0..23) {
                for (minute in listOf(0, 30)) {
                    val time = now.minusDays(day.toLong()).withHour(hour).withMinute(minute)
                    
                    // 生成一些异常体温值，提高低温数据的比例
                    val temp = when {
                        Random.nextInt(100) < 5 -> 38.0f + Random.nextFloat() * 1.5f // 高温: 38.0-39.5度，约5%
                        Random.nextInt(100) < 15 -> 35.0f + Random.nextFloat() * 0.9f // 低温: 35.0-35.9度，约15%
                        else -> 36.5f + (Random.nextFloat() - 0.5f) * 0.6f // 正常体温，小范围浮动
                    }
                    
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
        }
        
        // 确保在最近的数据中有明显异常的值，方便测试
        // 添加高温数据
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 38.7f,
                timestamp = now.minusHours(2)
            )
        )
        
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 39.2f,
                timestamp = now.minusHours(4)
            )
        )
        
        // 添加低温数据
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 35.6f, 
                timestamp = now.minusHours(6)
            )
        )
        
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 35.2f, 
                timestamp = now.minusHours(3)
            )
        )
        
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 35.7f, 
                timestamp = now.minusHours(8)
            )
        )
        
        // 添加正常数据，确保有一些明确的正常值
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 37.0f, 
                timestamp = now.minusHours(1)
            )
        )
        
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 36.8f, 
                timestamp = now.minusHours(5)
            )
        )
        
        records.add(
            TemperatureRecord(
                patientId = patient.second,
                patientName = patient.first,
                temperature = 36.2f, 
                timestamp = now.minusHours(7)
            )
        )
        
        records
    }
    
    // 對原始數據進行分桶處理，每小時一個桶，計算平均值
    val temperatureRecords = remember(rawTemperatureRecords) {
        rawTemperatureRecords
            .groupBy { record -> 
                record.timestamp.withMinute(0).withSecond(0).withNano(0) // 按小時分組
            }
            .map { (hourBucket, recordsInBucket) ->
                // 計算該小時的平均體溫
                val avgTemp = recordsInBucket.map { it.temperature }.average().toFloat()
                // 檢查是否有異常體溫
                val hasAbnormal = recordsInBucket.any { it.isAbnormal }
                
                TemperatureRecord(
                    patientId = recordsInBucket.first().patientId,
                    patientName = recordsInBucket.first().patientName,
                    temperature = avgTemp,
                    timestamp = hourBucket,
                    isAbnormal = hasAbnormal
                )
            }
            .sortedBy { it.timestamp }
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
                // 主標題和主題切換按鈕
                Text(
                    text = if (isChineseLanguage) "體溫監測" else "Temperature Monitor",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                

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
                            contentDescription = if (isChineseLanguage) "患者" else "Patient",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = if (isChineseLanguage)
                                "患者: ${patients[selectedPatientIndex].first}"
                            else
                                "Patient: ${patients[selectedPatientIndex].first}",
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
                        text = if (isChineseLanguage) "體溫趨勢圖" else "Temperature Trend",
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
                        TemperatureChart(temperatureRecords = temperatureRecords, isDarkTheme = isDarkTheme, isChineseLanguage = isChineseLanguage)
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
                    // 標題單獨一行
                    Text(
                        text = if (isChineseLanguage) "體溫記錄" else "Temperature Records",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 過濾器單獨一行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isChineseLanguage) "過濾:" else "Filter:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        // 過濾類型選擇按鈕
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AbnormalFilterChip(
                                text = if (isChineseLanguage) "全部" else "All",
                                isSelected = filterType == 0,
                                onClick = { filterType = 0 },
                                isDarkTheme = isDarkTheme
                            )
                            
                            AbnormalFilterChip(
                                text = if (isChineseLanguage) "高溫" else "High Temp",
                                isSelected = filterType == 1,
                                onClick = { filterType = 1 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFFFF5252) else Color.Red
                            )
                            
                            AbnormalFilterChip(
                                text = if (isChineseLanguage) "低溫" else "Low Temp",
                                isSelected = filterType == 2,
                                onClick = { filterType = 2 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3)
                            )
                        }
                    }
                    
                    // 時間範圍選擇
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimeRangeChip(
                            text = if (isChineseLanguage) "1天" else "1 Day",
                            isSelected = selectedTimeRange == 1,
                            onClick = { selectedTimeRange = 1 },
                            isDarkTheme = isDarkTheme
                        )
                        
                        TimeRangeChip(
                            text = if (isChineseLanguage) "3天" else "3 Days",
                            isSelected = selectedTimeRange == 3,
                            onClick = { selectedTimeRange = 3 },
                            isDarkTheme = isDarkTheme
                        )
                        
                        TimeRangeChip(
                            text = if (isChineseLanguage) "7天" else "7 Days",
                            isSelected = selectedTimeRange == 7,
                            onClick = { selectedTimeRange = 7 },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = if (isDarkTheme) Color.DarkGray else Color.LightGray
                    )
                    
                    // 過濾並按時間範圍顯示記錄
                    val now = LocalDateTime.now()
                    val filteredRecords = temperatureRecords
                        .filter { record -> 
                            // 時間範圍過濾
                            val daysAgo = ChronoUnit.DAYS.between(record.timestamp, now).toInt()
                            val inTimeRange = daysAgo < selectedTimeRange
                            
                            // 異常類型過濾
                            val matchesFilter = when (filterType) {
                                0 -> true // 全部顯示
                                1 -> record.temperature > 37.5f // 僅高溫
                                2 -> record.temperature < 36.0f // 僅低溫
                                else -> true
                            }
                            
                            inTimeRange && matchesFilter
                        }
                        .sortedByDescending { it.timestamp }
                    
                    // 在Card内使用LazyColumn显示记录
                    if (filteredRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isChineseLanguage) "無符合條件的數據" else "No matching data",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredRecords) { record ->
                                TemperatureRecordItem(record = record, isDarkTheme = isDarkTheme, isChineseLanguage = isChineseLanguage)
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
}

@Composable
fun TemperatureChart(temperatureRecords: List<TemperatureRecord>, isDarkTheme: Boolean, isChineseLanguage: Boolean) {
    // 排序記錄，按時間順序
    val sortedRecords = temperatureRecords.sortedBy { it.timestamp }
    
    if (sortedRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isChineseLanguage) "無體溫數據" else "No temperature data",
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
            val pointColor = when {
                record.temperature > 37.5f -> if (isDarkTheme) Color(0xFFFF5252) else Color.Red
                record.temperature < 36.0f -> if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3) // Blue
                else -> if (isDarkTheme) DarkChartLine else LightChartLine
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
fun TemperatureRecordItem(record: TemperatureRecord, isDarkTheme: Boolean, isChineseLanguage: Boolean) {
    val formattedTime = record.timestamp.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    
    // 根据深色模式调整颜色
    val temperatureColor = when {
        record.temperature > 37.5f -> if (isDarkTheme) Color(0xFFFF5252) else Color.Red
        record.temperature < 36.0f -> if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3) // Blue
        else -> if (isDarkTheme) Color(0xFF81C784) else Color(0xFF4CAF50) // Green
    }
    
    // 體溫狀態文本
    val statusText = when {
        record.temperature > 37.5f -> if (isChineseLanguage) "體溫過高" else "Temp Too High"
        record.temperature < 36.0f -> if (isChineseLanguage) "體溫過低" else "Temp Too Low"
        else -> if (isChineseLanguage) "正常" else "Normal"
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
            text = statusText,
            color = temperatureColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
} 