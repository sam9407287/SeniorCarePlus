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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
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
import com.seniorcareplus.app.ui.components.TimeRangeChip
import com.seniorcareplus.app.ui.components.AbnormalFilterChip
import com.seniorcareplus.app.models.TemperatureData
import com.seniorcareplus.app.ui.viewmodels.TemperatureViewModel
import com.seniorcareplus.app.models.TemperatureStatus

/**
 * 按固定的時間間隔（10分鐘）對溫度數據進行取樣
 * 如果每10分鐘的整點附近有多個數據點，則按時間取最接近整點的那個
 */
fun getSampledTemperatureData(temperatureData: List<TemperatureData>): List<TemperatureData> {
    if (temperatureData.isEmpty()) return emptyList()
    
    // 按時間排序
    val sortedData = temperatureData.sortedBy { it.getLocalDateTime() }
    
    // 如果數據少於24點，直接返回原数据，避免圖表過稀疏
    if (sortedData.size <= 24) return sortedData
    
    val result = mutableListOf<TemperatureData>()
    val startTime = sortedData.first().getLocalDateTime()
    val endTime = sortedData.last().getLocalDateTime()
    
    // 計算一天的時間點（10分鐘一個）
    val timePoints = mutableListOf<LocalDateTime>()
    var currentTime = startTime.withMinute((startTime.minute / 10) * 10).withSecond(0).withNano(0)
    
    while (currentTime.isBefore(endTime) || currentTime.isEqual(endTime)) {
        timePoints.add(currentTime)
        currentTime = currentTime.plusMinutes(10)
    }
    
    // 為每個10分鐘的整點找最接近的數據點
    for (timePoint in timePoints) {
        // 如果有多個與整點相同的時間，選擇第一個
        val exactMatch = sortedData.firstOrNull { 
            val recordTime = it.getLocalDateTime()
            recordTime.minute == timePoint.minute && 
            recordTime.hour == timePoint.hour && 
            recordTime.dayOfMonth == timePoint.dayOfMonth && 
            recordTime.month == timePoint.month && 
            recordTime.year == timePoint.year
        }
        
        if (exactMatch != null) {
            result.add(exactMatch)
            continue
        }
        
        // 找最接近整點的數據
        val closestRecord = sortedData.minByOrNull { record -> 
            abs(ChronoUnit.SECONDS.between(record.getLocalDateTime(), timePoint)).toInt()
        }
        
        // 如果最接近的點與整點相差不超過5分鐘，才添加
        if (closestRecord != null && 
            abs(ChronoUnit.MINUTES.between(closestRecord.getLocalDateTime(), timePoint)) <= 5) {
            // 確保不重復添加
            if (!result.contains(closestRecord)) {
                result.add(closestRecord)
            }
        }
    }
    
    return result
}

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
    
    // 日期選擇狀態
    var selectedDayIndex by remember { mutableStateOf(0) } // 0:今天, 1:昨天, 2:前天
    
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
        listOf("今日", "昨天", "前天")
    } else {
        listOf("Today", "Yesterday", "Day Before")
    }

    
    // 根據選擇的標籤頁索引更新顯示選擇的日期
    LaunchedEffect(selectedTabIndex) {
        selectedDayIndex = selectedTabIndex // 直接同步選擇的天數索引
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
        
        // 頁籤選擇列已移至體溫趨勢圖卡片中
        // 此處移除重複的Tab元素
        
        // 頁籤欄 - 改為今天、昨天、前天
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) 
                        MaterialTheme.colorScheme.surface 
                    else 
                        Color(0xFFF0F4FF) // 淺藍色背景，與心率圖表一致
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 頁籤選擇列
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        val tabTitles = if (isChineseLanguage) {
                            listOf("今日", "昨天", "前天")
                        } else {
                            listOf("Today", "Yesterday", "Day Before")
                        }
                        
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }
                    
                    // 體溫趨勢圖標題
                    Text(
                        text = if (isChineseLanguage) "體溫趨勢圖" else "Temperature Trend",
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 顯示體溫圖表
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp)
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
                            // 篩選數據 - 根據所選日期和10分鐘間隔進行過濾
                            val today = java.time.LocalDate.now()
                            val selectedDate = when(selectedTabIndex) {
                                0 -> today // 今天
                                1 -> today.minusDays(1) // 昨天
                                else -> today.minusDays(2) // 前天
                            }
                            
                            // 過濾所選日期的數據
                            val dateFilteredData = fullTemperatureData.filter { record ->
                                val recordDate = record.getLocalDateTime().toLocalDate()
                                recordDate == selectedDate
                            }
                            
                            // 按10分鐘間隔取樣數據
                            val sampledData = getSampledTemperatureData(dateFilteredData)
                            
                            TemperatureChart(
                                temperatureData = sampledData,
                                isDarkTheme = isDarkTheme,
                                isChineseLanguage = isChineseLanguage
                            )
                        }
                    }
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
                    
                    // 過濾按鈕行 - 等寬布局
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 全部
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AbnormalFilterChip(
                                text = if (isChineseLanguage) "全部" else "All",
                                isSelected = filterType == 0,
                                onClick = { filterType = 0 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF4CAF50), // 綠色
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                        
                        // 高溫
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AbnormalFilterChip(
                                text = if (isChineseLanguage) "高溫" else "High",
                                isSelected = filterType == 1,
                                onClick = { filterType = 1 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFFFF5252) else Color.Red,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                        
                        // 低溫
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AbnormalFilterChip(
                                text = if (isChineseLanguage) "低溫" else "Low",
                                isSelected = filterType == 2,
                                onClick = { filterType = 2 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                    }
                    
                    // 時間範圍選擇 - 等寬布局
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1天
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            TimeRangeChip(
                                text = if (isChineseLanguage) "1天" else "1 Day",
                                isSelected = selectedTimeRange == 1,
                                onClick = { selectedTimeRange = 1 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF4CAF50), // 綠色
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                        
                        // 3天
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            TimeRangeChip(
                                text = if (isChineseLanguage) "3天" else "3 Days",
                                isSelected = selectedTimeRange == 3,
                                onClick = { selectedTimeRange = 3 },
                                isDarkTheme = isDarkTheme,
                                color = if (isDarkTheme) Color(0xFFFF5252) else Color.Red, // 紅色，與高溫按鈕相同
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                        
                        // 7天
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            TimeRangeChip(
                                text = if (isChineseLanguage) "7天" else "7 Days",
                                isSelected = selectedTimeRange == 7,
                                onClick = { selectedTimeRange = 7 },
                                isDarkTheme = isDarkTheme,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
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
    val sortedRecords = temperatureData.sortedBy { it.getLocalDateTime() }
    
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
    
    // 固定溫度顯示範圍為34°C到40°C
    val minTemp = 34.0f // 圖表最低顯示溫度
    val maxTemp = 40.0f // 圖表最高顯示溫度
    
    // 定義高溫和低溫閾值
    val highTempThreshold = 37.5f
    val lowTempThreshold = 36.0f
    
    // 定義顏色
    val backgroundWhite = Color.White // 白色圖表背景
    val fillPink = Color(0xFFFFECEF).copy(alpha = 0.9f) // 非常淺的粉色填充區域
    val normalPointColor = Color(0xFFFF4081) // 正常點的淺紅色
    val lineColor = Color(0xFFFF4081) // 淺紅色線條
    val highTempColor = Color(0xFFE53935) // 高溫點的深紅色
    val lowTempColor = Color(0xFF2196F3) // 低溫點的藍色
    
    val chartBackgroundColor = if (isDarkTheme) Color(0xFF121212) else backgroundWhite
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(chartBackgroundColor)
    ) {
        val height = size.height
        val width = size.width
        
        val yAxisWidth = 50f
        val xAxisHeight = 50f
        
        val chartHeight = height - xAxisHeight
        val chartWidth = width - yAxisWidth
        
        val verticalStepSize = chartHeight / (maxTemp - minTemp)
        
        // 設置網格和軸線顏色
        val gridColor = if (isDarkTheme) Color(0xFF444444) else Color.LightGray.copy(alpha = 0.4f)
        val textColor = if (isDarkTheme) Color(0xFFCCCCCC) else Color.DarkGray
        
        // 計算高溫和低溫閾值線的Y坐標
        val highTempY = chartHeight - (highTempThreshold - minTemp) * verticalStepSize
        val lowTempY = chartHeight - (lowTempThreshold - minTemp) * verticalStepSize
        
        // 先不繪製背景填充，稍後在點之間的連線下面填充
        // 繪製高溫閾值水平線
        drawLine(
            color = highTempColor,
            start = Offset(0f, highTempY),
            end = Offset(width, highTempY),
            strokeWidth = 2.5f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        
        // 繪製低溫閾值水平線
        drawLine(
            color = lowTempColor,
            start = Offset(0f, lowTempY),
            end = Offset(width, lowTempY),
            strokeWidth = 2.5f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        
        // 在右側增加高低溫閾值標註
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                String.format("%.1f", highTempThreshold),
                width - 15f,
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
                width - 15f,
                lowTempY - 5f,
                Paint().apply {
                    color = lowTempColor.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                }
            )
        }
        
        // Y軸溫度标签
        val ySteps = 5
        val temperatureRange = maxTemp - minTemp
        for (i in 0..ySteps) {
            val temperature = minTemp + (temperatureRange * i) / ySteps
            val y = chartHeight - (temperature - minTemp) * verticalStepSize
            
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    String.format("%.1f", temperature),
                    15f,
                    y + 5f,
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.LEFT
                    }
                )
            }
        }
        
        // 分前為24小時，繪製 X 軸小時刻度
        val hours = 24
        val hourWidth = width / hours
        
        // 取得日期部分作為標題
        val date = sortedRecords.firstOrNull()?.getLocalDateTime()?.toLocalDate() ?: java.time.LocalDate.now()
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        
        // 在頂部繪制日期標題
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                dateStr,
                width / 2,
                20f,
                Paint().apply {
                    color = textColor.toArgb()
                    textSize = 14.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }
        
        // 繪製每個小時的標記
        for (hour in 0 until hours) {
            val x = hour * hourWidth
            
            // 繪製垂直網格線
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, chartHeight),
                strokeWidth = 0.5f
            )
            
            // 只在每3小時顯示時間標簽，減少擠擬
            if (hour % 3 == 0 || hour == 23) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        String.format("%02d:00", hour),
                        x,
                        height - 5f,
                        Paint().apply {
                            color = textColor.toArgb()
                            textSize = 12.sp.toPx()
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        }
        
        // 將體溫資料點映射到圖表上
        if (sortedRecords.isNotEmpty()) {
            // 計算一天內的相對時間位置
            val dayStartTime = sortedRecords.first().getLocalDateTime().withHour(0).withMinute(0).withSecond(0)
            
            val points = sortedRecords.map { record ->
                val recordTime = record.getLocalDateTime()
                val minutesSinceDayStart = ChronoUnit.MINUTES.between(dayStartTime, recordTime).toFloat()
                val dayProgressRatio = minutesSinceDayStart / (24 * 60)
                val x = dayProgressRatio * width
                val y = chartHeight - (record.temperature - minTemp) * verticalStepSize
                Triple(x, y, record)
            }
            
            // 繪製體溫折線和填充區域 - 類似心率圖的效果
            if (points.size >= 2) {
                // 先為所有點間的連線創建填充路徑
                val fillPath = Path().apply {
                    // 路徑起點為第一個點
                    moveTo(points.first().first, points.first().second)
                    
                    // 連接所有點
                    for (i in 1 until points.size) {
                        lineTo(points[i].first, points[i].second)
                    }
                    
                    // 向下走到圖表底部右角
                    lineTo(points.last().first, chartHeight)
                    
                    // 水平走回到圖表左下角
                    lineTo(points.first().first, chartHeight)
                    
                    // 往上回到起點以封閉路徑
                    close()
                }
                
                // 繪製紅色填充區域
                drawPath(
                    path = fillPath,
                    color = fillPink
                )
                
                // 繪製所有點之間的連線
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = Offset(points[i].first, points[i].second),
                        end = Offset(points[i + 1].first, points[i + 1].second),
                        strokeWidth = 2f
                    )
                }
            }
            
            // 繪製體溫點
            points.forEach { (x, y, record) ->
                val pointColor = when {
                    record.temperature > highTempThreshold -> highTempColor // 高溫
                    record.temperature < lowTempThreshold -> lowTempColor  // 低溫
                    else -> normalPointColor // 正常
                }
                
                drawCircle(
                    color = pointColor,
                    radius = 5f,
                    center = Offset(x, y)
                )
            }
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