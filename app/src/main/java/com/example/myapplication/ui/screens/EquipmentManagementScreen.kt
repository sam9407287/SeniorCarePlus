package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager

// 設備類型枚舉
enum class DeviceType {
    WATCH, // 智能手錶
    DIAPER // 智能尿布傳感器
}

// 設備數據類
data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    var hardwareId: String,
    val patientId: String,
    val patientName: String,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentManagementScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 查詢關鍵詞狀態
    var searchQuery by remember { mutableStateOf("") }
    
    // 選中的設備狀態
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    
    // 替換設備硬件ID狀態
    var newHardwareId by remember { mutableStateOf("") }
    
    // 替換設備對話框狀態
    var showReplaceDialog by remember { mutableStateOf(false) }
    
    // 成功替換對話框狀態
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // 設備列表數據（在實際應用中應該從後端獲取或使用ViewModel）
    val deviceList = remember {
        mutableStateListOf(
            Device(
                id = "W001",
                name = if(isChineseLanguage) "健康監測手錶 #1" else "Health Monitor Watch #1",
                type = DeviceType.WATCH,
                hardwareId = "HWID-W23445",
                patientId = "P001",
                patientName = if(isChineseLanguage) "王大明" else "Wang Daming",
                isActive = true
            ),
            Device(
                id = "W002",
                name = if(isChineseLanguage) "健康監測手錶 #2" else "Health Monitor Watch #2",
                type = DeviceType.WATCH,
                hardwareId = "HWID-W23446",
                patientId = "P002",
                patientName = if(isChineseLanguage) "李小華" else "Li Xiaohua",
                isActive = true
            ),
            Device(
                id = "D001",
                name = if(isChineseLanguage) "智能尿布傳感器 #1" else "Smart Diaper Sensor #1",
                type = DeviceType.DIAPER,
                hardwareId = "HWID-D34589",
                patientId = "P001",
                patientName = if(isChineseLanguage) "王大明" else "Wang Daming",
                isActive = true
            ),
            Device(
                id = "D002",
                name = if(isChineseLanguage) "智能尿布傳感器 #2" else "Smart Diaper Sensor #2",
                type = DeviceType.DIAPER,
                hardwareId = "HWID-D34590",
                patientId = "P003",
                patientName = if(isChineseLanguage) "張小鳳" else "Zhang Xiaofeng",
                isActive = false
            ),
            Device(
                id = "W003",
                name = if(isChineseLanguage) "健康監測手錶 #3" else "Health Monitor Watch #3",
                type = DeviceType.WATCH,
                hardwareId = "HWID-W23450",
                patientId = "P005",
                patientName = if(isChineseLanguage) "趙一一" else "Zhao Yiyi",
                isActive = true
            ),
            Device(
                id = "D003",
                name = if(isChineseLanguage) "智能尿布傳感器 #3" else "Smart Diaper Sensor #3",
                type = DeviceType.DIAPER,
                hardwareId = "HWID-D34595",
                patientId = "P004",
                patientName = if(isChineseLanguage) "孫大龍" else "Sun Dalong",
                isActive = true
            )
        )
    }
    
    // 篩選設備列表
    val filteredDevices = remember(searchQuery, deviceList, isChineseLanguage) {
        if (searchQuery.isBlank()) {
            deviceList
        } else {
            deviceList.filter { device -> 
                // 確保支持中文搜索
                val matchName = device.name.contains(searchQuery, ignoreCase = true)
                val matchId = device.id.contains(searchQuery, ignoreCase = true)
                val matchHardwareId = device.hardwareId.contains(searchQuery, ignoreCase = true)
                val matchPatientName = device.patientName.contains(searchQuery, ignoreCase = true)
                
                // 針對不同類型的設備添加額外的關鍵詞匹配
                val matchWatchKeyword = device.type == DeviceType.WATCH && 
                    ((isChineseLanguage && (searchQuery.contains("手錶") || searchQuery.contains("手表"))) ||
                    (!isChineseLanguage && searchQuery.contains("watch")))
                    
                val matchDiaperKeyword = device.type == DeviceType.DIAPER && 
                    ((isChineseLanguage && (searchQuery.contains("尿布") || searchQuery.contains("傳感器") || searchQuery.contains("传感器"))) ||
                    (!isChineseLanguage && (searchQuery.contains("diaper") || searchQuery.contains("sensor"))))
                
                matchName || matchId || matchHardwareId || matchPatientName || matchWatchKeyword || matchDiaperKeyword
            }
        }
    }
    
    // 替換設備處理函數
    fun replaceDevice() {
        selectedDevice?.let { device ->
            // 更新設備的硬件ID
            val index = deviceList.indexOf(device)
            if (index != -1 && newHardwareId.isNotBlank()) {
                deviceList[index] = device.copy(hardwareId = newHardwareId, isActive = true)
                // 顯示成功對話框
                showReplaceDialog = false
                showSuccessDialog = true
                // 重置輸入
                newHardwareId = ""
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isChineseLanguage) "設備管理" else "Equipment Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = if (isChineseLanguage) "返回" else "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 搜索欄
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(if (isChineseLanguage) "搜索設備名稱、編號或患者名稱..." else "Search by device name, ID or patient name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = if (isChineseLanguage) "清除" else "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { /* 處理搜索 */ }
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            
            // 設備類型過濾器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = searchQuery.isEmpty(),
                    onClick = { searchQuery = "" },
                    label = { Text(if (isChineseLanguage) "全部" else "All") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    selected = searchQuery.equals("手錶", ignoreCase = true) || 
                             searchQuery.equals("手表", ignoreCase = true) || 
                             searchQuery.equals("watch", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "手錶" else "watch" },
                    label = { Text(if (isChineseLanguage) "智能手錶" else "Smart Watches") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Watch,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    selected = searchQuery.equals("尿布", ignoreCase = true) || 
                             searchQuery.equals("傳感器", ignoreCase = true) || 
                             searchQuery.equals("传感器", ignoreCase = true) || 
                             searchQuery.equals("diaper", ignoreCase = true) || 
                             searchQuery.equals("sensor", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "尿布" else "diaper" },
                    label = { Text(if (isChineseLanguage) "尿布傳感器" else "Diaper Sensors") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.BabyChangingStation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            
            // 設備列表標題
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "設備列表" else "Equipment List",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = if (isChineseLanguage) "共 ${filteredDevices.size} 個設備" else "${filteredDevices.size} Devices",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 設備列表
            if (filteredDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (isChineseLanguage) "未找到匹配的設備" else "No matching devices found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredDevices) { device ->
                        DeviceListItem(
                            device = device,
                            isChineseLanguage = isChineseLanguage,
                            onClick = {
                                selectedDevice = device
                                showReplaceDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 替換設備對話框
    if (showReplaceDialog) {
        Dialog(onDismissRequest = { showReplaceDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isChineseLanguage) "替換設備" else "Replace Device",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    selectedDevice?.let { device ->
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = if (isChineseLanguage) "當前硬件編號: ${device.hardwareId}" else "Current Hardware ID: ${device.hardwareId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = if (isChineseLanguage) "使用者: ${device.patientName}" else "Used by: ${device.patientName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // 新硬件ID輸入
                    OutlinedTextField(
                        value = newHardwareId,
                        onValueChange = { newHardwareId = it },
                        label = { Text(if (isChineseLanguage) "新硬件編號" else "New Hardware ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        placeholder = { Text(if (isChineseLanguage) "輸入新的硬件編號" else "Enter new hardware ID") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { replaceDevice() }
                        ),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showReplaceDialog = false }
                        ) {
                            Text(if (isChineseLanguage) "取消" else "Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { replaceDevice() },
                            enabled = newHardwareId.isNotBlank()
                        ) {
                            Text(if (isChineseLanguage) "確認替換" else "Confirm")
                        }
                    }
                }
            }
        }
    }
    
    // 成功替換對話框
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text(if (isChineseLanguage) "替換成功" else "Replacement Successful") },
            text = { 
                Text(
                    if (isChineseLanguage) 
                    "設備已成功替換並已激活。" 
                    else 
                    "The device has been successfully replaced and activated."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text(if (isChineseLanguage) "確定" else "OK")
                }
            }
        )
    }
}

@Composable
fun DeviceListItem(
    device: Device,
    isChineseLanguage: Boolean,
    onClick: () -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) 
                            else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 設備類型圖標
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                when (device.type) {
                    DeviceType.WATCH -> Icon(
                        Icons.Default.Watch, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    DeviceType.DIAPER -> Icon(
                        Icons.Default.BabyChangingStation, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 設備信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = device.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 活動狀態標籤
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (device.isActive) 
                                    Color(0xFF4CAF50).copy(alpha = 0.1f) 
                                else 
                                    Color(0xFFE53935).copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (device.isActive) 
                                if (isChineseLanguage) "活動" else "Active" 
                            else 
                                if (isChineseLanguage) "不活動" else "Inactive",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (device.isActive) Color(0xFF4CAF50) else Color(0xFFE53935),
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isChineseLanguage) "患者: ${device.patientName}" else "Patient: ${device.patientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 替換按鈕
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = if (isChineseLanguage) "替換" else "Replace",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 