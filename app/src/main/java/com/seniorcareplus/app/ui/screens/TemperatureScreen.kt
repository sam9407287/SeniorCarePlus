package com.seniorcareplus.app.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.unit.DpOffset
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
    
    // 設置過濾類型: 0 = 全部, 1 = 僅高溫, 2 = 僅低溫
    var filterType by remember { mutableStateOf(0) }
    
    // 獲取指定病人的完整溫度數據（用於圖表顯示，不受過濾選項影響）
    val fullTemperatureData by temperatureViewModel.getPatientTemperatureData(
        patientId = selectedPatientId, 
        daysToShow = selectedTimeRange, 
        showAbnormalOnly = false // 始終顯示全部數據
    ).collectAsState(initial = emptyList())
    
    // 獲取經過過濾的溫度數據（同時考慮溫度類型和時間範圍）
    val now = LocalDateTime.now()
    val filteredRecords = fullTemperatureData
        .filter { record -> 
            // 時間範圍過濾 - 使用getLocalDateTime()方法來獲取LocalDateTime對象
            val recordDateTime = record.getLocalDateTime()
            val daysAgo = ChronoUnit.DAYS.between(recordDateTime, now).toInt()
            val inTimeRange = daysAgo < selectedTimeRange
            
            // 異常類型過濾
            val matchesFilter = when (filterType) {
                1 -> record.temperature > 37.5f // 高溫
                2 -> record.temperature < 36.0f // 低溫
                else -> true // 全部顯示
            }
            
            inTimeRange && matchesFilter
        }
        .sortedByDescending { it.getLocalDateTime() }
    
    // 使用語言設置
    // val isChineseLanguage already defined above
    
    // 標籤頁選項
    val tabTitles = if (isChineseLanguage) {
        listOf("今日", "本週", "本月")
    } else {
        listOf("Today", "This Week", "This Month")
    }

    
    // 根據選擇的標籤頁索引更新顯示天數
    LaunchedEffect(selectedTabIndex) {
        selectedTimeRange = when (selectedTabIndex) {
            0 -> 1     // 今日
            1 -> 7     // 本週
            else -> 30 // 本月
        }
    }
    
    // 我們現在直接在UI層過濾數據，不再需要通知ViewModel
    
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
                            onClick = { showPatientDropdown = true }
                        ) {
                            Icon(
                                imageVector = if (showPatientDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isChineseLanguage) "選擇患者" else "Select Patient",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 病患選擇下拉選單
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (isChineseLanguage) "選擇患者" else "Select Patient",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                                
                                IconButton(
                                    onClick = { showPatientDropdown = false },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.CenterEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (isChineseLanguage) "關閉" else "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            HorizontalDivider()
                            
                            actualPatientList.forEach { patient ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = patient.second,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
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
                if (fullTemperatureData.isEmpty()) {
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
                        temperatureData = fullTemperatureData,
                        isDarkTheme = isDarkTheme,
                        isChineseLanguage = isChineseLanguage
                    )
                }
            }
        }
        
        // 體溫記錄卡片
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 標題
                    Text(
                        text = if (isChineseLanguage) "體溫記錄" else "Temperature Records",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 過濾按鈕行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 全部
                        AbnormalFilterChip(
                            text = if (isChineseLanguage) "全部" else "All",
                            isSelected = filterType == 0,
                            onClick = { filterType = 0 },
                            isDarkTheme = isDarkTheme
                        )
                        
                        // 高溫
                        AbnormalFilterChip(
                            text = if (isChineseLanguage) "高溫" else "High Temp",
                            isSelected = filterType == 1,
                            onClick = { filterType = 1 },
                            isDarkTheme = isDarkTheme,
                            color = if (isDarkTheme) Color(0xFFFF5252) else Color.Red
                        )
                        
                        // 低溫
                        AbnormalFilterChip(
                            text = if (isChineseLanguage) "低溫" else "Low Temp",
                            isSelected = filterType == 2,
                            onClick = { filterType = 2 },
                            isDarkTheme = isDarkTheme,
                            color = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3)
                        )
                    }
                    
                    // 時間範圍選擇
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1天
                        TimeRangeChip(
                            text = if (isChineseLanguage) "1天" else "1 Day",
                            isSelected = selectedTimeRange == 1,
                            onClick = { selectedTimeRange = 1 },
                            isDarkTheme = isDarkTheme
                        )
                        
                        // 3天
                        TimeRangeChip(
                            text = if (isChineseLanguage) "3天" else "3 Days",
                            isSelected = selectedTimeRange == 3,
                            onClick = { selectedTimeRange = 3 },
                            isDarkTheme = isDarkTheme
                        )
                        
                        // 7天
                        TimeRangeChip(
                            text = if (isChineseLanguage) "7天" else "7 Days",
                            isSelected = selectedTimeRange == 7,
                            onClick = { selectedTimeRange = 7 },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = if (isDarkTheme) Color.DarkGray else Color.LightGray
                    )
                    
                    // 在固定大小的Box內使用LazyColumn顯示溫度記錄
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)  // 給記錄列表一個合適的固定高度
                    ) {
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
                                    TemperatureRecordItem(
                                        record = record,
                                        isDarkTheme = isDarkTheme,
                                        isChineseLanguage = isChineseLanguage
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = if (isDarkTheme) Color.DarkGray.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
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
    
    // 定義高溫和低溫閾值
    val highTempThreshold = 37.5f
    val lowTempThreshold = 36.0f
    
    // 定義顏色
    val mainLineColor = Color(0xFFFF4081) // 主要線顏色（粉紅色）
    val highTempColor = Color(0xFFFF5252) // 高溫閾值線顏色（紅色）
    val lowTempColor = Color(0xFF2196F3)  // 低溫閾值線顏色（藍色）
    val backgroundFillColor = Color(0x20FF4081) // 圖表背景填充顏色（淡粉色）
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val height = size.height
        val width = size.width
        
        val yAxisWidth = 50f
        val xAxisHeight = 50f
        
        val chartHeight = height - xAxisHeight
        val chartWidth = width - yAxisWidth
        
        val verticalStepSize = chartHeight / (maxTemp - minTemp)
        val horizontalStepSize = chartWidth / (sortedRecords.size - 1).coerceAtLeast(1)
        
        // 設置網格和軸線顏色
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
        
        // 繪製圖表底色
        drawRect(
            color = backgroundFillColor,
            topLeft = Offset(yAxisWidth, 0f),
            size = Size(width - yAxisWidth, chartHeight)
        )

        // 繪製高溫閾值水平線
        val highTempY = chartHeight - (highTempThreshold - minTemp) * verticalStepSize
        drawLine(
            color = highTempColor,
            start = Offset(yAxisWidth, highTempY),
            end = Offset(width, highTempY),
            strokeWidth = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        
        // 繪製低溫閾值水平線
        val lowTempY = chartHeight - (lowTempThreshold - minTemp) * verticalStepSize
        drawLine(
            color = lowTempColor,
            start = Offset(yAxisWidth, lowTempY),
            end = Offset(width, lowTempY),
            strokeWidth = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        
        // 在高溫閾值線旁添加文字標註
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                String.format("%.1f", highTempThreshold),
                width - 40f,
                highTempY - 5f,
                Paint().apply {
                    color = highTempColor.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                }
            )
            drawText(
                String.format("%.1f", lowTempThreshold),
                width - 40f,
                lowTempY - 5f,
                Paint().apply {
                    color = lowTempColor.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                }
            )
        }
        
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
        
        // 畫折線
        val points = sortedRecords.mapIndexed { index, record ->
            val x = yAxisWidth + index * horizontalStepSize
            val y = chartHeight - (record.temperature - minTemp) * verticalStepSize
            Offset(x, y)
        }
        
        // 1. 低溫區域填充（藍色）
        if (points.isNotEmpty()) {
            val lowTempPath = Path()
            lowTempPath.moveTo(yAxisWidth, lowTempY)
            for (point in points) {
                // 只有低於閾值的點才連接到曲線上
                if (point.y > lowTempY) {
                    lowTempPath.lineTo(point.x, point.y)
                } else {
                    lowTempPath.lineTo(point.x, lowTempY)
                }
            }
            lowTempPath.lineTo(width, lowTempY)
            lowTempPath.close()
            
            drawPath(
                path = lowTempPath,
                color = lowTempColor.copy(alpha = 0.15f)
            )
        }
        
        // 2. 高溫區域填充（紅色）
        if (points.isNotEmpty()) {
            val highTempPath = Path()
            highTempPath.moveTo(yAxisWidth, highTempY)
            for (point in points) {
                // 只有高於閾值的點才連接到曲線上
                if (point.y < highTempY) {
                    highTempPath.lineTo(point.x, point.y)
                } else {
                    highTempPath.lineTo(point.x, highTempY)
                }
            }
            highTempPath.lineTo(width, highTempY)
            highTempPath.close()
            
            drawPath(
                path = highTempPath,
                color = highTempColor.copy(alpha = 0.15f)
            )
        }
        
        // 繪製点和線
        for (i in 0 until points.size - 1) {
            val current = points[i]
            val next = points[i + 1]
            
            drawLine(
                color = mainLineColor,
                start = current,
                end = next,
                strokeWidth = 2.5f
            )
            
            // 異常體溫彩色點
            val record = sortedRecords[i]
            val pointColor = when {
                record.temperature > 37.5f -> highTempColor
                record.temperature < 36.0f -> lowTempColor
                else -> mainLineColor
            }
            
            drawCircle(
                color = pointColor,
                radius = 5f,
                center = current
            )
        }
        
        // 最後一個點
        if (points.isNotEmpty()) {
            val lastRecord = sortedRecords.last()
            val lastPointColor = when {
                lastRecord.temperature > 37.5f -> highTempColor
                lastRecord.temperature < 36.0f -> lowTempColor
                else -> mainLineColor
            }
            
            // 最後一個點顯示為較大的圓點
            drawCircle(
                color = lastPointColor,
                radius = 8f,
                center = points.last()
            )
            // 加外圈
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = points.last(),
                style = Stroke(width = 2f)
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