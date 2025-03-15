package com.example.myapplication.ui.screens

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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.math.min
import kotlin.random.Random
import com.example.myapplication.R
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager

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

// 多语言文本映射
private object MapTexts {
    val title = mapOf(
        true to "室内地图与定位",
        false to "Indoor Map & Positioning"
    )
    
    val mapTitle = mapOf(
        true to "室内实时位置图",
        false to "Real-time Indoor Positioning"
    )
    
    val legend = mapOf(
        true to "图例",
        false to "Legend"
    )
    
    val uwbAnchor = mapOf(
        true to "UWB锚点",
        false to "UWB Anchor"
    )
    
    val locationList = mapOf(
        true to "位置列表",
        false to "Location List"
    )
    
    val elderlyNames = mapOf(
        "E001" to mapOf(true to "张三", false to "Zhang San"),
        "E002" to mapOf(true to "李四", false to "Li Si"),
        "E003" to mapOf(true to "王五", false to "Wang Wu"),
        "E004" to mapOf(true to "赵六", false to "Zhao Liu"),
        "E005" to mapOf(true to "钱七", false to "Qian Qi")
    )
    
    val anchorNames = mapOf(
        "U001" to mapOf(true to "锚点1", false to "Anchor 1"),
        "U002" to mapOf(true to "锚点2", false to "Anchor 2"),
        "U003" to mapOf(true to "锚点3", false to "Anchor 3"),
        "U004" to mapOf(true to "锚点4", false to "Anchor 4")
    )
    
    val deviceInfo = mapOf(
        true to "设备信息",
        false to "Device Info"
    )
    
    val deviceType = mapOf(
        true to "设备类型",
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
    // 获取当前语言和主题状态
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 定义老人头像图标（使用Material Icons中的不同人物图标）
    val personIcons = listOf(
        Icons.Default.Face,           // 脸部图标
        Icons.Default.Person,         // 人物图标
        Icons.Default.AccountCircle,  // 账户圆形图标
        Icons.Default.SupervisedUserCircle, // 用户监控图标
        Icons.Default.EmojiPeople     // 人物表情图标
    )
    
    // 定义颜色列表，给每个人物一个不同的颜色
    val personColors = listOf(
        Color(0xFF2196F3), // 蓝色
        Color(0xFF4CAF50), // 绿色
        Color(0xFFFF9800), // 橙色
        Color(0xFFE91E63), // 粉色
        Color(0xFF9C27B0)  // 紫色
    )
    
    // 获取适合当前主题的背景和文本颜色
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
    val textColor = MaterialTheme.colorScheme.onBackground
    val tooltipBgColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF)
    
    // 用于记录当前悬停的设备
    var hoveredDeviceId by remember { mutableStateOf<String?>(null) }
    
    // 调整老人位置，使他们分布更加分散
    val locationDataList = remember {
        mutableStateListOf(
            // 老人位置 - 分配不同的头像图标和颜色，位置更加分散
            LocationData(
                "E001", 
                MapTexts.elderlyNames["E001"]?.get(isChineseLanguage) ?: "张三", 
                120f, 180f, 
                LocationType.ELDERLY, 
                avatarIcon = personIcons[0]
            ),
            LocationData(
                "E002",
                MapTexts.elderlyNames["E002"]?.get(isChineseLanguage) ?: "李四",
                280f, 120f,
                LocationType.ELDERLY,
                avatarIcon = personIcons[1]
            ),
            LocationData(
                "E003",
                MapTexts.elderlyNames["E003"]?.get(isChineseLanguage) ?: "王五",
                420f, 350f, // 调整位置避免与其他老人重叠
                LocationType.ELDERLY,
                avatarIcon = personIcons[2]
            ),
            LocationData(
                "E004",
                MapTexts.elderlyNames["E004"]?.get(isChineseLanguage) ?: "赵六",
                150f, 380f, // 调整位置避免与其他老人重叠
                LocationType.ELDERLY,
                avatarIcon = personIcons[3]
            ),
            LocationData(
                "E005",
                MapTexts.elderlyNames["E005"]?.get(isChineseLanguage) ?: "钱七", 
                350f, 230f, 
                LocationType.ELDERLY, 
                avatarIcon = personIcons[4]
            ),
            
            // UWB锚点位置 - 放置在建筑物的四个角落
            LocationData(
                "U001", 
                MapTexts.anchorNames["U001"]?.get(isChineseLanguage) ?: "锚点1", 
                70f, 70f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U002", 
                MapTexts.anchorNames["U002"]?.get(isChineseLanguage) ?: "锚点2", 
                470f, 70f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U003", 
                MapTexts.anchorNames["U003"]?.get(isChineseLanguage) ?: "锚点3", 
                470f, 430f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U004", 
                MapTexts.anchorNames["U004"]?.get(isChineseLanguage) ?: "锚点4", 
                70f, 430f, 
                LocationType.UWB_ANCHOR
            )
        )
    }
    
    // 当语言改变时，更新位置数据的名称
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
    
    // 模拟数据更新（每5秒随机移动老人位置，但确保不会重叠）
    LaunchedEffect(key1 = "locationUpdate") {
        while(true) {
            kotlinx.coroutines.delay(5000)
            
            // 创建一个已占用位置的列表，用于避免重叠
            val occupiedPositions = mutableListOf<Pair<Float, Float>>()
            
            for (i in locationDataList.indices) {
                val data = locationDataList[i]
                if (data.type == LocationType.ELDERLY) {
                    // 随机移动老人位置，但避免重叠，增加最小距离
                    var newX: Float
                    var newY: Float
                    var attempts = 0
                    var positionValid: Boolean
                    
                    do {
                        newX = (data.x + Random.nextFloat() * 30 - 15).coerceIn(70f, 470f)
                        newY = (data.y + Random.nextFloat() * 30 - 15).coerceIn(70f, 430f)
                        positionValid = true
                        
                        // 检查是否与已有位置重叠，增加最小距离要求
                        for (pos in occupiedPositions) {
                            val distance = kotlin.math.sqrt((newX - pos.first) * (newX - pos.first) + (newY - pos.second) * (newY - pos.second))
                            if (distance < 60) { // 增加最小距离为60像素
                                positionValid = false
                                break
                            }
                        }
                        
                        attempts++
                    } while (!positionValid && attempts < 15) // 增加尝试次数
                    
                    if (positionValid) {
                        occupiedPositions.add(Pair(newX, newY))
                        locationDataList[i] = data.copy(x = newX, y = newY)
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // 顶部标题和主题切换按钮
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
            
            // 主题切换按钮
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
        
        // 地图内容区域 - 占据整个页面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 地图区域 - 现在占用整个空间
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundColor
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 使用上传的地图图片作为背景
                    Image(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = "Floor Plan Map",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 绘制UWB锚点位置
                    locationDataList.filter { it.type == LocationType.UWB_ANCHOR }.forEach { data ->
                        val x = data.x / 500f
                        val y = data.y / 500f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopStart)
                                .offset(
                                    x = (x * 100).toFloat().dp,
                                    y = (y * 100).toFloat().dp
                                )
                        ) {
                            // UWB锚点图标
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .clickable { 
                                        // 切换悬停状态
                                        hoveredDeviceId = if (hoveredDeviceId == data.id) null else data.id
                                    }
                            )
                            
                            // 显示详细信息提示框（当悬停时）
                            if (hoveredDeviceId == data.id) {
                                Card(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .padding(top = 12.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-5).dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = tooltipBgColor
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
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
                                            text = "${MapTexts.deviceType[isChineseLanguage]!!}: ${MapTexts.uwbAnchor[isChineseLanguage]!!}",
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
                    
                    // 老人位置图标 - 在地图上层显示
                    locationDataList.filter { it.type == LocationType.ELDERLY }.forEachIndexed { index, data ->
                        val personColor = personColors[index % personColors.size]
                        
                        val x = data.x / 500f
                        val y = data.y / 500f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopStart)
                                .offset(
                                    x = (x * 100).toFloat().dp,
                                    y = (y * 100).toFloat().dp
                                )
                        ) {
                            // 显示人物头像图标
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = personColor.copy(alpha = 0.9f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        // 切换悬停状态
                                        hoveredDeviceId = if (hoveredDeviceId == data.id) null else data.id
                                    }
                                    .padding(4.dp)
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
                            
                            // 人物名称标签
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
                            )
                            
                            // 显示详细信息提示框（当悬停时）
                            if (hoveredDeviceId == data.id) {
                                Card(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .padding(top = 12.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-5).dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = tooltipBgColor
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
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
                                            text = "${MapTexts.deviceType[isChineseLanguage]!!}: ${MapTexts.elderly[isChineseLanguage]!!}",
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