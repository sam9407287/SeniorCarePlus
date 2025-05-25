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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.seniorcareplus.app.ui.components.TimeRangeChip
import com.seniorcareplus.app.ui.components.AbnormalFilterChip
import com.seniorcareplus.app.ui.viewmodels.HeartRateViewModel
import com.seniorcareplus.app.models.HeartRateData
import com.seniorcareplus.app.models.HeartRateStatus

// 心率數據採樣函數 - 參考溫度頁面的邏輯
fun getSampledHeartRateData(
    data: List<HeartRateData>,
    maxPoints: Int = 50
): List<HeartRateData> {
    if (data.isEmpty()) return emptyList()
    if (data.size <= maxPoints) return data.sortedBy { it.getLocalDateTime() }
    
    val sortedData = data.sortedBy { it.getLocalDateTime() }
    val startTime = sortedData.first().getLocalDateTime()
    val endTime = sortedData.last().getLocalDateTime()
    val timeRange = java.time.Duration.between(startTime, endTime)
    val interval = timeRange.dividedBy(maxPoints.toLong())
    
    val sampledData = mutableListOf<HeartRateData>()
    var currentTime = startTime
    var dataIndex = 0
    
    while (dataIndex < sortedData.size && sampledData.size < maxPoints) {
        // 找到最接近當前時間點的數據
        var closestData = sortedData[dataIndex]
        var minTimeDiff = java.time.Duration.between(currentTime, closestData.getLocalDateTime()).abs()
        
        for (i in dataIndex until sortedData.size) {
            val timeDiff = java.time.Duration.between(currentTime, sortedData[i].getLocalDateTime()).abs()
            if (timeDiff < minTimeDiff) {
                closestData = sortedData[i]
                minTimeDiff = timeDiff
                dataIndex = i
            } else {
                break
            }
        }
        
        sampledData.add(closestData)
        currentTime = currentTime.plus(interval)
        dataIndex++
    }
    
    return sampledData
}

@Composable
fun HeartRateMonitorScreen(navController: NavController) {
    val heartRateViewModel: HeartRateViewModel = viewModel()
    
    // 判断是否为深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 收集患者數據
    val heartRateGroups by heartRateViewModel.heartRateGroups.collectAsState()
    val selectedPatientId by heartRateViewModel.selectedPatientId.collectAsState()
    val abnormalFilter by heartRateViewModel.abnormalFilter.collectAsState()
    val timeRangeFilter by heartRateViewModel.timeRangeFilter.collectAsState()
    
    // UI狀態變量
    var selectedPatientName by remember { mutableStateOf("") }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        if (isChineseLanguage) "今天" else "Today",
        if (isChineseLanguage) "昨天" else "Yesterday", 
        if (isChineseLanguage) "前天" else "Day Before"
    )
    
    // 參考心率值
    var targetHeartRate by remember { mutableFloatStateOf(75f) }
    
    // 當患者列表更新時，初始化選中的患者
    LaunchedEffect(heartRateGroups) {
        if (heartRateGroups.isNotEmpty() && selectedPatientId?.isEmpty() != false) {
            val firstPatientId = heartRateGroups.first().patientId
            heartRateViewModel.setSelectedPatientId(firstPatientId)
        }
    }
    
    // 更新選中患者的名稱
    LaunchedEffect(selectedPatientId, heartRateGroups) {
        if (!selectedPatientId.isNullOrEmpty()) {
            val patientGroup = heartRateGroups.find { it.patientId == selectedPatientId }
            selectedPatientName = patientGroup?.patientName ?: ""
        }
    }
    
    // 獲取選中患者的完整心率數據
    val currentPatientId = selectedPatientId
    val fullHeartRateData = if (!currentPatientId.isNullOrEmpty()) {
        heartRateViewModel.getHeartRateDataForPatient(currentPatientId)
    } else {
        emptyList()
    }
    
    // 根據時間範圍和過濾類型篩選數據
    val filteredData = remember(fullHeartRateData, selectedTabIndex, abnormalFilter) {
        val now = LocalDateTime.now()
        val startTime = when (selectedTabIndex) {
            0 -> now.toLocalDate().atStartOfDay() // 今天
            1 -> now.minusDays(1).toLocalDate().atStartOfDay() // 昨天
            2 -> now.minusDays(2).toLocalDate().atStartOfDay() // 前天
            else -> now.minusDays(7).toLocalDate().atStartOfDay()
        }
        val endTime = startTime.plusDays(1)
        
        fullHeartRateData.filter { data ->
            data.getLocalDateTime() >= startTime && data.getLocalDateTime() < endTime
        }.let { timeFilteredData ->
            when (abnormalFilter) {
                1 -> timeFilteredData.filter { it.getHeartRateStatus() == HeartRateStatus.HIGH }
                2 -> timeFilteredData.filter { it.getHeartRateStatus() == HeartRateStatus.LOW }
                else -> timeFilteredData
            }
        }
    }
    
    // 使用LazyColumn替代Column讓整個頁面可以滾動
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
                    text = if (isChineseLanguage) "心率監測" else "Heart Rate Monitor",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        // 病患選擇
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
                                "患者: $selectedPatientName"
                            else
                                "Patient: $selectedPatientName",
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
                            onDismissRequest = { showPatientDropdown = false }
                        ) {
                            heartRateGroups.forEach { patientGroup ->
                                DropdownMenuItem(
                                    text = { Text(patientGroup.patientName) },
                                    onClick = {
                                        heartRateViewModel.setSelectedPatientId(patientGroup.patientId)
                                        showPatientDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 時間範圍選項卡
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
        
        // 過濾選項
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AbnormalFilterChip(
                    text = if (isChineseLanguage) "全部" else "All",
                    isSelected = abnormalFilter == 0,
                    onClick = { heartRateViewModel.setAbnormalFilter(0) }
                )
                
                AbnormalFilterChip(
                    text = if (isChineseLanguage) "高心率" else "High HR",
                    isSelected = abnormalFilter == 1,
                    onClick = { heartRateViewModel.setAbnormalFilter(1) }
                )
                
                AbnormalFilterChip(
                    text = if (isChineseLanguage) "低心率" else "Low HR",
                    isSelected = abnormalFilter == 2,
                    onClick = { heartRateViewModel.setAbnormalFilter(2) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 心率趨勢圖表
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
                            text = if (isChineseLanguage) "心率趨勢圖" else "Heart Rate Trend",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = if (isChineseLanguage)
                                "目標心率: ${targetHeartRate.toInt()} BPM"
                            else
                                "Target: ${targetHeartRate.toInt()} BPM",
                            fontSize = 14.sp,
                            color = if (isDarkTheme) DarkChartLine else LightChartLine
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 滑塊用於設置目標心率
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
                            heartRateData = getSampledHeartRateData(filteredData, 30), 
                            targetHeartRate = targetHeartRate.toInt(),
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 心率記錄列表
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) DarkCardBackground else LightCardBackground
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isChineseLanguage) "心率記錄" else "Heart Rate Records",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (filteredData.isEmpty()) {
                        Text(
                            text = if (isChineseLanguage) "暫無數據" else "No data available",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        filteredData.take(10).forEach { record ->
                            HeartRateRecordItem(
                                heartRateData = record,
                                isDarkTheme = isDarkTheme,
                                isChineseLanguage = isChineseLanguage
                            )
                            
                            if (record != filteredData.take(10).last()) {
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        
                        if (filteredData.size > 10) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isChineseLanguage) 
                                    "還有 ${filteredData.size - 10} 條記錄..." 
                                else 
                                    "${filteredData.size - 10} more records...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HeartRateRecordItem(
    heartRateData: HeartRateData,
    isDarkTheme: Boolean,
    isChineseLanguage: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 心率狀態指示器
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    when (heartRateData.getHeartRateStatus()) {
                        HeartRateStatus.HIGH -> Color.Red
                        HeartRateStatus.LOW -> Color.Blue
                        HeartRateStatus.NORMAL -> Color.Green
                    }
                )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${heartRateData.heartRate} BPM",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = heartRateData.getFormattedTime(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // 異常標記
        if (heartRateData.isAbnormal) {
            Text(
                text = when (heartRateData.getHeartRateStatus()) {
                    HeartRateStatus.HIGH -> if (isChineseLanguage) "偏高" else "High"
                    HeartRateStatus.LOW -> if (isChineseLanguage) "偏低" else "Low"
                    HeartRateStatus.NORMAL -> ""
                },
                fontSize = 12.sp,
                color = when (heartRateData.getHeartRateStatus()) {
                    HeartRateStatus.HIGH -> Color.Red
                    HeartRateStatus.LOW -> Color.Blue
                    HeartRateStatus.NORMAL -> Color.Green
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HeartRateChart(
    heartRateData: List<HeartRateData>,
    targetHeartRate: Int,
    isDarkTheme: Boolean
) {
    val chartBackgroundColor = if (isDarkTheme) DarkChartBackground else LightChartBackground
    val chartLineColor = if (isDarkTheme) DarkChartLine else LightChartLine
    val targetLineColor = Color.Red.copy(alpha = 0.7f)
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        
        // 繪製背景
        drawRect(
            color = chartBackgroundColor,
            size = size
        )
        
        if (heartRateData.isNotEmpty()) {
            // 計算數據範圍
            val minHeartRate = (heartRateData.minOfOrNull { it.heartRate } ?: 60) - 10
            val maxHeartRate = (heartRateData.maxOfOrNull { it.heartRate } ?: 100) + 10
            val heartRateRange = maxHeartRate - minHeartRate
            
            // 繪製目標心率線
            val targetY = canvasHeight - padding - 
                ((targetHeartRate - minHeartRate).toFloat() / heartRateRange * (canvasHeight - 2 * padding))
            
            drawLine(
                color = targetLineColor,
                start = Offset(padding, targetY),
                end = Offset(canvasWidth - padding, targetY),
                strokeWidth = 3f
            )
            
            // 繪製心率數據線
            val path = Path()
            heartRateData.forEachIndexed { index, data ->
                val x = padding + (index.toFloat() / (heartRateData.size - 1).coerceAtLeast(1)) * (canvasWidth - 2 * padding)
                val y = canvasHeight - padding - 
                    ((data.heartRate - minHeartRate).toFloat() / heartRateRange * (canvasHeight - 2 * padding))
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // 繪製數據點
                drawCircle(
                    color = when (data.getHeartRateStatus()) {
                        HeartRateStatus.HIGH -> Color.Red
                        HeartRateStatus.LOW -> Color.Blue
                        HeartRateStatus.NORMAL -> chartLineColor
                    },
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
            
            // 繪製連接線
            drawPath(
                path = path,
                color = chartLineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            
            // 繪製Y軸標籤
            val paint = Paint().apply {
                color = chartLineColor.toArgb()
                textSize = 24f
                isAntiAlias = true
            }
            
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "$maxHeartRate",
                    10f,
                    padding + 10f,
                    paint
                )
                drawText(
                    "$minHeartRate",
                    10f,
                    canvasHeight - padding + 10f,
                    paint
                )
                drawText(
                    "$targetHeartRate",
                    canvasWidth - 60f,
                    targetY + 5f,
                    paint
                )
            }
        } else {
            // 無數據時顯示提示
            val paint = Paint().apply {
                color = chartLineColor.toArgb()
                textSize = 32f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "No Data",
                    canvasWidth / 2,
                    canvasHeight / 2,
                    paint
                )
            }
        }
    }
} 