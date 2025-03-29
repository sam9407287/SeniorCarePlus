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

// 位置数据类
data class LocationData(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val type: LocationType,
    val avatarIcon: ImageVector? = null,
    val avatarId: Int? = null
)

// 位置类型枚举
enum class LocationType {
    ELDERLY, // 老人
    UWB_ANCHOR // UWB三角定位锚点
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
    
    // 使用更大的坐标值，确保在大地图上分布更合理
    val locationDataList = remember {
        mutableStateListOf(
            // 老人位置 - 分配不同的头像图标和颜色，使用更大的坐标差距
            LocationData(
                "E001", 
                MapTexts.elderlyNames["E001"]?.get(isChineseLanguage) ?: "張三", 
                1000f, 1000f,
                LocationType.ELDERLY, 
                avatarIcon = personIcons[0]
            ),
            LocationData(
                "E002",
                MapTexts.elderlyNames["E002"]?.get(isChineseLanguage) ?: "李四",
                2000f, 1500f,
                LocationType.ELDERLY,
                avatarIcon = personIcons[1]
            ),
            LocationData(
                "E003",
                MapTexts.elderlyNames["E003"]?.get(isChineseLanguage) ?: "王五",
                1000f, 2000f, 
                LocationType.ELDERLY,
                avatarIcon = personIcons[2]
            ),
            LocationData(
                "E004",
                MapTexts.elderlyNames["E004"]?.get(isChineseLanguage) ?: "趙六",
                2500f, 4000f,
                LocationType.ELDERLY,
                avatarIcon = personIcons[3]
            ),
            LocationData(
                "E005",
                MapTexts.elderlyNames["E005"]?.get(isChineseLanguage) ?: "錢七", 
                1800f, 3000f,
                LocationType.ELDERLY, 
                avatarIcon = personIcons[4]
            ),
            
            // UWB锚点位置 - 放置在建筑物的四个角落，使用更大的坐标范围
            LocationData(
                "U001", 
                MapTexts.anchorNames["U001"]?.get(isChineseLanguage) ?: "錨點1", 
                500f, 500f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U002", 
                MapTexts.anchorNames["U002"]?.get(isChineseLanguage) ?: "錨點2", 
                2500f, 500f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U003", 
                MapTexts.anchorNames["U003"]?.get(isChineseLanguage) ?: "錨點3", 
                2500f, 2500f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U004", 
                MapTexts.anchorNames["U004"]?.get(isChineseLanguage) ?: "錨點4", 
                500f, 2500f, 
                LocationType.UWB_ANCHOR
            )
        )
    }
    
    // 當語言改變時，更新位置數據的名稱
    LaunchedEffect(isChineseLanguage) {
        for (i in locationDataList.indices) {
            val data = locationDataList[i]
            val newName = when (data.type) {
                LocationType.ELDERLY -> MapTexts.elderlyNames[data.id]?.get(isChineseLanguage) ?: data.name
                LocationType.UWB_ANCHOR -> MapTexts.anchorNames[data.id]?.get(isChineseLanguage) ?: data.name
            }
            locationDataList[i] = data.copy(name = newName)
        }
    }
    
    // 注意：已移除隨機移動功能，將來會直接從後端接收位置數據
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // 頂部標題和主題切換按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = MapTexts.title[isChineseLanguage]!!,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = textColor
            )
            
            // 區域管理按鈕
            IconButton(
                onClick = { 
                    navController.navigate("region") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = if (isChineseLanguage) "區域管理" else "Region Management",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
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
                    contentDescription = if (isChineseLanguage) "切換主題" else "Toggle Theme",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // 地圖內容區域 - 佔據整個頁面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 地圖區域 - 現在佔用整個空間
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundColor
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 添加點擊監聽器到整個地圖區域，點擊非圖標區域時隱藏詳細信息
                        .clickable { hoveredDeviceId = null }
                ) {
                    // 使用上傳的地圖圖片作為背景
                    Image(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = "Floor Plan Map",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 繪製UWB錨點位置 - 調整坐標系統的比例因子為1000f
                    locationDataList.filter { it.type == LocationType.UWB_ANCHOR }.forEach { data ->
                        val x = data.x / 1000f
                        val y = data.y / 1000f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopStart)
                                .offset(
                                    x = (x * 100).toFloat().dp,
                                    y = (y * 100).toFloat().dp
                                )
                        ) {
                            // UWB錨點圖標
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .clickable(onClick = { 
                                        // 切換懸停狀態
                                        hoveredDeviceId = if (hoveredDeviceId == data.id) null else data.id
                                    })
                                    .zIndex(1f)
                            )
                        }
                    }
                    
                    // 老人位置圖標 - 在地圖上層顯示 - 調整坐標系統的比例因子為1000f
                    locationDataList.filter { it.type == LocationType.ELDERLY }.forEachIndexed { index, data ->
                        val personColor = personColors[index % personColors.size]
                        
                        val x = data.x / 1000f
                        val y = data.y / 1000f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopStart)
                                .offset(
                                    x = (x * 100).toFloat().dp,
                                    y = (y * 100).toFloat().dp
                                )
                        ) {
                            // 顯示人物頭像圖標
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = personColor.copy(alpha = 0.9f),
                                        shape = CircleShape
                                    )
                                    .clickable(onClick = {
                                        // 切換懸停狀態
                                        hoveredDeviceId = if (hoveredDeviceId == data.id) null else data.id
                                    })
                                    .padding(4.dp)
                                    .zIndex(1f)
                            ) {
                                data.avatarIcon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = data.name,
                                        tint = Color.White,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            
                            // 人物名稱標簽
                            Text(
                                text = data.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .offset(y = 32.dp)
                                    .background(
                                        personColor.copy(alpha = 0.8f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .zIndex(1f)
                            )
                        }
                    }
                    
                    // 單獨渲染所有設備的信息提示框，確保它們在最上層
                    locationDataList.forEach { data ->
                        if (hoveredDeviceId == data.id) {
                            val x = data.x / 1000f
                            val y = data.y / 1000f
                            val personColor = if (data.type == LocationType.ELDERLY) {
                                val index = locationDataList.filter { it.type == LocationType.ELDERLY }.indexOfFirst { it.id == data.id }
                                personColors[index % personColors.size]
                            } else {
                                Color.Red
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.TopStart)
                                    .offset(
                                        x = (x * 100).toFloat().dp,
                                        y = (y * 100).toFloat().dp
                                    )
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 