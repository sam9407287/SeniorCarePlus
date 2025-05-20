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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.temporal.ChronoUnit
import com.seniorcareplus.app.ui.components.TimeRangeChip
import com.seniorcareplus.app.ui.components.AbnormalFilterChip
import com.seniorcareplus.app.models.TemperatureData
import com.seniorcareplus.app.ui.viewmodels.TemperatureViewModel
import com.seniorcareplus.app.models.TemperatureStatus

@Composable
fun TemperatureMonitorScreen(navController: NavController) {
    val context = LocalContext.current
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 初始化TemperatureViewModel
    val temperatureViewModel: TemperatureViewModel = viewModel()
    
    // 通過LaunchedEffect在Composition初始化時連接到MQTT服務
    LaunchedEffect(Unit) {
        temperatureViewModel.bindMqttService()
    }
    
    // 收集病人列表
    val patientList by temperatureViewModel.patientList.collectAsState()
    
    // 如果沒有病人，使用空的列表；否則使用實際數據
    val actualPatientList = if (patientList.isEmpty()) {
        emptyList()
    } else {
        patientList.map { patient -> 
            Pair(patient.first, patient.second)
        }
    }
    
    // 選擇的患者ID
    var selectedPatientId by remember { mutableStateOf("") }
    // 選擇的患者姓名
    var selectedPatientName by remember { mutableStateOf("") }
    // 是否顯示選擇患者的下拉列表
    var showPatientDropdown by remember { mutableStateOf(false) }
    // 當前選中的頁簽
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // 是否只顯示異常數據
    var showAbnormalOnly by remember { mutableStateOf(false) }
    // 選擇的時間範圍
    var selectedTimeRange by remember { mutableStateOf(7) } // 默認為7天
    
    // 當病人列表更新且非空時，選擇第一個病人
    LaunchedEffect(actualPatientList) {
        if (actualPatientList.isNotEmpty() && selectedPatientId.isEmpty()) {
            selectedPatientId = actualPatientList.first().first
            selectedPatientName = actualPatientList.first().second
        }
    }
    
    // 獲取指定病人的溫度數據
    val temperatureData by temperatureViewModel.getPatientTemperatureData(
        patientId = selectedPatientId, 
        daysToShow = selectedTimeRange, 
        showAbnormalOnly = showAbnormalOnly
    ).collectAsState(initial = emptyList())
    
    // 使用語言設置
    // val isChineseLanguage already defined above
    
    // 標籤頁選項
    val tabTitles = if (isChineseLanguage) {
        listOf("今日", "本週", "本月")
    } else {
        listOf("Today", "This Week", "This Month")
    }
    
    // 設置過濾類型: 0 = 全部, 1 = 僅高溫, 2 = 僅低溫
    var filterType by remember { mutableStateOf(0) }
    
    // 根據選擇的標籤頁索引更新顯示天數
    LaunchedEffect(selectedTabIndex) {
        selectedTimeRange = when (selectedTabIndex) {
            0 -> 1     // 今日
            1 -> 7     // 本週
            else -> 30 // 本月
        }
    }
    
    // 根據過濾類型更新異常過濾設置
    LaunchedEffect(filterType) {
        // 只有當filterType是1(高溫)或2(低溫)時才啟用異常過濾
        showAbnormalOnly = filterType != 0
        
        // 通知ViewModel更新過濾設置
        if (filterType != 0) {
            // 1 = 僅高溫, 2 = 僅低溫
            temperatureViewModel.setAbnormalFilter(
                if (filterType == 1) TemperatureStatus.HIGH else TemperatureStatus.LOW
            )
        } else {
            // 重置過濾器
            temperatureViewModel.setAbnormalFilter(null)
        }
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

                        
                        val displayText = if (selectedPatientName.isEmpty()) {
                            if (isChineseLanguage) "等待數據中..." else "Waiting for data..."
                        } else {
                            if (isChineseLanguage) "患者: $selectedPatientName" else "Patient: $selectedPatientName"
                        }
                        
                        Text(
                            text = displayText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        IconButton(
                            onClick = { showPatientDropdown = true },
                            enabled = actualPatientList.isNotEmpty() // 只有當有病患時才啟用
                        ) {
                            Icon(
                                imageVector = if (showPatientDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isChineseLanguage) "選擇患者" else "Select Patient",
                                tint = if (actualPatientList.isNotEmpty()) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        
                        // 病患選擇下拉選單
                        DropdownMenu(
                            expanded = showPatientDropdown,
                            onDismissRequest = { showPatientDropdown = false },
                            modifier = Modifier
                                .width(200.dp)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            actualPatientList.forEach { patient ->
                                DropdownMenuItem(
                                    text = { Text(text = patient.second) },
                                    onClick = {
                                        selectedPatientId = patient.first
                                        selectedPatientName = patient.second
                                        showPatientDropdown = false
                                        // 選擇病患后，通知ViewModel更新選中的病患
                                        temperatureViewModel.selectPatient(patient.first)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 頁籤選擇列
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val tabTitles = if (isChineseLanguage) {
                        listOf("今日", "本週", "本月")
                    } else {
                        listOf("Today", "This Week", "This Month")
                    }
                    
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 過濾選項
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 温度过滤按钮
                    AbnormalFilterChip(
                        text = if (isChineseLanguage) "全部" else "All",
                        isSelected = filterType == 0,
                        onClick = { filterType = 0 },
                        isDarkTheme = isDarkTheme
                    )
                    
                    AbnormalFilterChip(
                        text = if (isChineseLanguage) "高溫" else "High",
                        isSelected = filterType == 1,
                        onClick = { filterType = 1 },
                        isDarkTheme = isDarkTheme
                    )
                    
                    AbnormalFilterChip(
                        text = if (isChineseLanguage) "低溫" else "Low",
                        isSelected = filterType == 2,
                        onClick = { filterType = 2 },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
        
        // 顯示體溫圖表
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) 
                        MaterialTheme.colorScheme.surface 
                    else 
                        Color.White
                )
            ) {
                if (temperatureData.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isChineseLanguage) "無體溫記錄" else "No temperature records",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    TemperatureChart(
                        temperatureData = temperatureData,
                        isDarkTheme = isDarkTheme,
                        isChineseLanguage = isChineseLanguage
                    )
                }
            }
        }
        
        // 體溫記錄列表
        item {
            Text(
                text = if (isChineseLanguage) "體溫記錄" else "Temperature Records",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        if (temperatureData.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isChineseLanguage) "無記錄" else "No records",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // 體溫記錄項目
            items(temperatureData) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            Color.White
                    )
                ) {
                    TemperatureRecordItem(
                        record = record,
                        isDarkTheme = isDarkTheme,
                        isChineseLanguage = isChineseLanguage
                    )
                }
            }
        }
    }
}


@Composable
fun TemperatureChart(temperatureData: List<TemperatureData>, isDarkTheme: Boolean, isChineseLanguage: Boolean) {
    // 排序記錄，按時間順序
    val sortedRecords = temperatureData.sortedBy { it.timestamp }
    
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
        
        // 繪製点和線
        for (i in 0 until points.size - 1) {
            val current = points[i]
            val next = points[i + 1]
            
            // 線條
            drawLine(
                color = if (isDarkTheme) DarkChartLine else LightChartLine,
                start = current,
                end = next,
                strokeWidth = 2f
            )
            
            // 異常體溫彩色點
            val record = sortedRecords[i]
            val pointColor = when {
                record.temperature > 37.5f -> Color.Red
                record.temperature < 36.0f -> Color.Blue
                else -> if (isDarkTheme) DarkChartLine else LightChartLine
            }
            
            drawCircle(
                color = pointColor,
                radius = 4f,
                center = current
            )
        }
        
        // 最後一個點
        if (points.isNotEmpty()) {
            val lastRecord = sortedRecords.last()
            val lastPointColor = when {
                lastRecord.temperature > 37.5f -> Color.Red
                lastRecord.temperature < 36.0f -> Color.Blue
                else -> if (isDarkTheme) DarkChartLine else LightChartLine
            }
            
            drawCircle(
                color = lastPointColor,
                radius = 4f,
                center = points.last()
            )
        }
    }
}

@Composable
fun TemperatureRecordItem(record: TemperatureData, isDarkTheme: Boolean, isChineseLanguage: Boolean) {
    // 格式化日期和時間
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