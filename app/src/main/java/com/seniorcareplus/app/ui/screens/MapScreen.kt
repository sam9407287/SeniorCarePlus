package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.math.min
import kotlin.random.Random
import com.seniorcareplus.app.R
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.ThemeManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.seniorcareplus.app.ui.viewmodels.MapViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.util.Log

// 位置數據類
data class LocationData(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val type: LocationType,
    val avatarIcon: ImageVector? = null,
    val avatarId: Int? = null
)

// 位置類型枚舉
enum class LocationType {
    ELDERLY, // 老人
    UWB_ANCHOR // UWB三角定位錨點
}

// 多語言文本映射
private object MapTexts {
    val title = mapOf(
        true to "室內定位",
        false to "Indoor Positioning"
    )
    
    val mapTitle = mapOf(
        true to "室內實時位置圖",
        false to "Real-time Indoor Positioning"
    )
    
    val legend = mapOf(
        true to "圖例",
        false to "Legend"
    )
    
    val uwbAnchor = mapOf(
        true to "UWB錨點",
        false to "UWB Anchor"
    )
    
    val locationList = mapOf(
        true to "位置列表",
        false to "Location List"
    )
    
    val elderlyNames = mapOf(
        "E001" to mapOf(true to "張三", false to "Zhang San"),
        "E002" to mapOf(true to "李四", false to "Li Si"),
        "E003" to mapOf(true to "王五", false to "Wang Wu"),
        "E004" to mapOf(true to "趙六", false to "Zhao Liu"),
        "E005" to mapOf(true to "錢七", false to "Qian Qi")
    )
    
    val anchorNames = mapOf(
        "U001" to mapOf(true to "錨點1", false to "Anchor 1"),
        "U002" to mapOf(true to "錨點2", false to "Anchor 2"),
        "U003" to mapOf(true to "錨點3", false to "Anchor 3"),
        "U004" to mapOf(true to "錨點4", false to "Anchor 4")
    )
    
    val deviceInfo = mapOf(
        true to "設備信息",
        false to "Device Info"
    )
    
    val deviceType = mapOf(
        true to "設備類型",
        false to "Device Type"
    )
    
    val location = mapOf(
        true to "位置",
        false to "Location"
    )
    
    val elderly = mapOf(
        true to "老人",
        false to "Elderly"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController = rememberNavController()) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 使用ViewModel處理數據
    val mapViewModel: MapViewModel = viewModel()
    // 從ViewModel收集位置數據
    val locationDataList = mapViewModel.locationData.collectAsState().value
    
    // 新增：檢查連接狀態
    val isConnecting = mapViewModel.isConnecting.collectAsState().value
    val connectionError = mapViewModel.connectionError.collectAsState().value
    
    // 生命週期處理 - 簡化版本
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    mapViewModel.bindMqttService()
                } catch (e: Exception) {
                    Log.e("MapScreen", "綁定MQTT服務失敗", e)
                }
            }
            
            if (event == Lifecycle.Event.ON_PAUSE) {
                try {
                    mapViewModel.unbindMqttService()
                } catch (e: Exception) {
                    Log.e("MapScreen", "解綁MQTT服務失敗", e)
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        try {
            mapViewModel.bindMqttService()
        } catch (e: Exception) {
            Log.e("MapScreen", "初始綁定MQTT服務失敗", e)
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mapViewModel.unbindMqttService()
            } catch (e: Exception) {
                Log.e("MapScreen", "最終解綁MQTT服務失敗", e)
            }
        }
    }
    
    // 定義老人頭像圖標（使用Material Icons中的不同人物圖標）
    val personIcons = listOf(
        Icons.Default.Face,           // 臉部圖標
        Icons.Default.Person,         // 人物圖標
        Icons.Default.AccountCircle,  // 賬戶圓形圖標
        Icons.Default.SupervisedUserCircle, // 用戶監控圖標
        Icons.Default.EmojiPeople     // 人物表情圖標
    )
    
    // 定義顏色列表，給每個人物一個不同的顏色
    val personColors = listOf(
        Color(0xFF2196F3), // 藍色
        Color(0xFF4CAF50), // 綠色
        Color(0xFFFF9800), // 橙色
        Color(0xFFE91E63), // 粉色
        Color(0xFF9C27B0)  // 紫色
    )
    
    // 獲取適合當前主題的背景和文本顏色
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
    val textColor = MaterialTheme.colorScheme.onBackground
    val tooltipBgColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF)
    
    // 用於記錄當前懸停的設備
    var hoveredDeviceId by remember { mutableStateOf<String?>(null) }
    
    // 將ViewModel中的位置數據轉換為UI顯示數據
    val displayLocationData = locationDataList.map { data ->
        // 如果是錨點，添加本地化名稱
        if (data.type == LocationType.UWB_ANCHOR) {
            data.copy(
                name = MapTexts.anchorNames[data.id]?.get(isChineseLanguage) ?: data.name
            )
        } 
        // 如果是老人，添加本地化名稱
        else if (data.type == LocationType.ELDERLY) {
            data.copy(
                name = MapTexts.elderlyNames[data.id]?.get(isChineseLanguage) ?: data.name
            )
        } else {
            data
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // 頂部標題和狀態區域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = MapTexts.title[isChineseLanguage]!!,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                // 顯示連接狀態
                if (isConnecting || connectionError != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "連接中...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (connectionError != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "連接錯誤",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
            
            // 重新連接按鈕
            IconButton(
                onClick = {
                    try {
                        mapViewModel.unbindMqttService()
                        mapViewModel.bindMqttService()
                    } catch(e: Exception) {
                        Log.e("MapScreen", "重新連接失敗", e)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "重新連接",
                    tint = textColor
                )
            }
            
            // 主題切換按鈕
            IconButton(
                onClick = { ThemeManager.toggleTheme() }
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "切換主題",
                    tint = textColor
                )
            }
        }
        
        // 地圖區域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 頂部標題
                Text(
                    text = MapTexts.mapTitle[isChineseLanguage]!!,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(bottom = 16.dp),
                    color = textColor
                )
                
                // 地圖背景
                Image(
                    painter = painterResource(id = R.drawable.map),
                    contentDescription = "Floor Plan",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // 位置標記
                for (data in displayLocationData) {
                    val iconSize = if (data.type == LocationType.ELDERLY) 40.dp else 30.dp
                    val personColor = if (data.type == LocationType.ELDERLY) {
                        // 使用隨機但固定的顏色，根據ID確保同一老人總是同一顏色
                        val colorIndex = data.id.hashCode().rem(personColors.size).let { if (it < 0) -it else it }
                        personColors[colorIndex]
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    
                    // 記錄映射前的原始座標
                    val originalX = data.x
                    val originalY = data.y
                    
                    // 獲取螢幕密度和計算地圖邊界
                    val density = LocalDensity.current.density
                    
                    // 地圖範圍 - 調整為適合圖片的實際尺寸
                    val mapWidth = 800f
                    val mapHeight = 600f
                    
                    // 更合理的座標映射 - 從實際座標映射到顯示座標
                    // 確保座標在地圖範圍內
                    val mappedX = (originalX / 1000f) * mapWidth
                    val mappedY = (originalY / 1000f) * mapHeight
                    
                    // 調試日誌 - 輸出映射後的座標
                    Log.d("MapScreen", "地圖座標映射: ${data.name} - 原始:(${originalX},${originalY}) → 映射:(${mappedX},${mappedY})")
                    
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .offset(
                                // 直接使用修改後的座標，不再除以1000
                                x = with(LocalDensity.current) { (originalX * 0.32f).toDp() },
                                y = with(LocalDensity.current) { (originalY * 0.32f).toDp() }
                            )
                            .zIndex(if (hoveredDeviceId == data.id) 10f else 1f)
                            .clickable {
                                hoveredDeviceId = if (hoveredDeviceId == data.id) null else data.id
                            }
                    ) {
                        // 位置圖標
                        if (data.type == LocationType.ELDERLY) {
                            // 老人圖標
                            val avatarIcon = data.avatarIcon ?: personIcons[data.id.hashCode().rem(personIcons.size).let { if (it < 0) -it else it }]
                            
                            Icon(
                                imageVector = avatarIcon,
                                contentDescription = data.name,
                                modifier = Modifier
                                    .size(iconSize)
                                    .clip(CircleShape)
                                    .background(personColor)
                                    .border(1.dp, Color.White, CircleShape)
                                    .padding(4.dp),
                                tint = Color.White
                            )
                            
                            // 名字標籤
                            Text(
                                text = data.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 20.dp)
                                    .background(
                                        color = tooltipBgColor.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        } else {
                            // 錨點圖標
                            Canvas(
                                modifier = Modifier.size(iconSize)
                            ) {
                                val radius = min(size.width, size.height) / 2
                                // 外圓
                                drawCircle(
                                    color = Color.Red,
                                    radius = radius,
                                    style = Stroke(width = 2f)
                                )
                                // 內圓
                                drawCircle(
                                    color = Color.Red,
                                    radius = radius / 3,
                                    style = Stroke(width = 2f)
                                )
                                // 交叉線
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(center.x - radius, center.y),
                                    end = Offset(center.x + radius, center.y),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(center.x, center.y - radius),
                                    end = Offset(center.x, center.y + radius),
                                    strokeWidth = 2f
                                )
                            }
                        }
                        
                        // 懸停時顯示詳細信息
                        if (hoveredDeviceId == data.id) {
                            Box(
                                modifier = Modifier
                                    .zIndex(10f) // 確保在最上層
                            ) {
                                // 信息提示框顯示在圖標正下方，不會遮擋圖標本身
                                Card(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .offset(
                                            x = if (data.x > 1500f) (-170).dp else 20.dp,
                                            y = if (data.type == LocationType.ELDERLY) 60.dp else 35.dp
                                        )
                                        .shadow(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = tooltipBgColor
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 8.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = MapTexts.deviceInfo[isChineseLanguage]!!,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${data.id} - ${data.name}",
                                            fontSize = 12.sp,
                                            color = textColor
                                        )
                                        Text(
                                            text = "${MapTexts.deviceType[isChineseLanguage]!!}: ${
                                                when(data.type) {
                                                    LocationType.ELDERLY -> MapTexts.elderly[isChineseLanguage]!!
                                                    LocationType.UWB_ANCHOR -> MapTexts.uwbAnchor[isChineseLanguage]!!
                                                }
                                            }",
                                            fontSize = 12.sp,
                                            color = textColor
                                        )
                                        Text(
                                            text = "${MapTexts.location[isChineseLanguage]!!}: X=${data.x.toInt()}, Y=${data.y.toInt()}",
                                            fontSize = 12.sp,
                                            color = textColor
                                        )
                                        
                                        // 顯示Gateway ID和其他詳細資訊
                                        if (data.id.contains("_")) {
                                            val parts = data.id.split("_")
                                            if (parts.size > 1) {
                                                Text(
                                                    text = "Gateway: ${parts[0]}, ID: ${parts[1]}",
                                                    fontSize = 12.sp,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 圖例
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tooltipBgColor.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = MapTexts.legend[isChineseLanguage]!!,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 老人圖例
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "老人",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(personColors[0])
                                    .padding(2.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = MapTexts.elderly[isChineseLanguage]!!,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                        
                        // 錨點圖例
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Canvas(
                                modifier = Modifier.size(16.dp)
                            ) {
                                val radius = min(size.width, size.height) / 2
                                drawCircle(
                                    color = Color.Red,
                                    radius = radius,
                                    style = Stroke(width = 2f)
                                )
                                drawCircle(
                                    color = Color.Red,
                                    radius = radius / 3,
                                    style = Stroke(width = 2f)
                                )
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(center.x - radius, center.y),
                                    end = Offset(center.x + radius, center.y),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(center.x, center.y - radius),
                                    end = Offset(center.x, center.y + radius),
                                    strokeWidth = 2f
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = MapTexts.uwbAnchor[isChineseLanguage]!!,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
