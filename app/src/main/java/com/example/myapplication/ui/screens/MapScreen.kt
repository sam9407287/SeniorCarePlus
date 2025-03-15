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
    
    // 调整老人位置，避免重叠
    val locationDataList = remember {
        mutableStateListOf(
            // 老人位置 - 分配不同的头像图标和颜色，位置分散
            LocationData(
                "E001", 
                MapTexts.elderlyNames["E001"]?.get(isChineseLanguage) ?: "张三", 
                150f, 200f, 
                LocationType.ELDERLY, 
                avatarIcon = personIcons[0]
            ),
            LocationData(
                "E002",
                MapTexts.elderlyNames["E002"]?.get(isChineseLanguage) ?: "李四",
                250f, 150f,
                LocationType.ELDERLY,
                avatarIcon = personIcons[1]
            ),
            LocationData(
                "E003",
                MapTexts.elderlyNames["E003"]?.get(isChineseLanguage) ?: "王五",
                390f, 320f, // 调整位置避免与其他老人重叠
                LocationType.ELDERLY,
                avatarIcon = personIcons[2]
            ),
            LocationData(
                "E004",
                MapTexts.elderlyNames["E004"]?.get(isChineseLanguage) ?: "赵六",
                180f, 350f, // 调整位置避免与其他老人重叠
                LocationType.ELDERLY,
                avatarIcon = personIcons[3]
            ),
            LocationData(
                "E005",
                MapTexts.elderlyNames["E005"]?.get(isChineseLanguage) ?: "钱七", 
                380f, 180f, 
                LocationType.ELDERLY, 
                avatarIcon = personIcons[4]
            ),
            
            // UWB锚点位置
            LocationData(
                "U001", 
                MapTexts.anchorNames["U001"]?.get(isChineseLanguage) ?: "锚点1", 
                50f, 50f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U002", 
                MapTexts.anchorNames["U002"]?.get(isChineseLanguage) ?: "锚点2", 
                450f, 50f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U003", 
                MapTexts.anchorNames["U003"]?.get(isChineseLanguage) ?: "锚点3", 
                450f, 450f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U004", 
                MapTexts.anchorNames["U004"]?.get(isChineseLanguage) ?: "锚点4", 
                50f, 450f, 
                LocationType.UWB_ANCHOR
            )
        )
    }
    
    // 房间墙壁坐标（简化为矩形）
    val rooms = remember {
        listOf(
            Pair(Offset(50f, 50f), Offset(250f, 250f)), // 房间1
            Pair(Offset(270f, 50f), Offset(450f, 200f)), // 房间2
            Pair(Offset(50f, 270f), Offset(200f, 450f)), // 房间3
            Pair(Offset(220f, 270f), Offset(450f, 450f))  // 房间4
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
                    // 随机移动老人位置，但避免重叠
                    var newX: Float
                    var newY: Float
                    var attempts = 0
                    var positionValid: Boolean
                    
                    do {
                        newX = (data.x + Random.nextFloat() * 20 - 10).coerceIn(60f, 440f)
                        newY = (data.y + Random.nextFloat() * 20 - 10).coerceIn(60f, 440f)
                        positionValid = true
                        
                        // 检查是否与已有位置重叠
                        for (pos in occupiedPositions) {
                            val distance = kotlin.math.sqrt((newX - pos.first) * (newX - pos.first) + (newY - pos.second) * (newY - pos.second))
                            if (distance < 40) { // 最小距离为40像素
                                positionValid = false
                                break
                            }
                        }
                        
                        attempts++
                    } while (!positionValid && attempts < 10) // 最多尝试10次
                    
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
        
        // 地图内容区域
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // 地图区域
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackgroundColor
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 底层地图图像 - 只显示右侧部分
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            // 绘制房间 - 只显示右侧部分
                            // 房间1 (右上) - 紫色
                            drawRect(
                                color = Color(0xFFF3E5F5).copy(alpha = if (isDarkTheme) 0.3f else 0.5f),
                                topLeft = Offset(canvasWidth * 0.5f, 0f),
                                size = androidx.compose.ui.geometry.Size(canvasWidth * 0.5f, canvasHeight * 0.5f)
                            )
                            
                            // 房间2 (右下) - 橙色
                            drawRect(
                                color = Color(0xFFFFF3E0).copy(alpha = if (isDarkTheme) 0.3f else 0.5f),
                                topLeft = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f),
                                size = androidx.compose.ui.geometry.Size(canvasWidth * 0.5f, canvasHeight * 0.5f)
                            )
                            
                            // 绘制房间边界
                            val borderColor = if (isDarkTheme) Color.Gray.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.7f)
                            
                            // 水平中线
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, canvasHeight * 0.5f),
                                end = Offset(canvasWidth, canvasHeight * 0.5f),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            // 垂直中线
                            drawLine(
                                color = borderColor,
                                start = Offset(canvasWidth * 0.5f, 0f),
                                end = Offset(canvasWidth * 0.5f, canvasHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            // 外边框
                            drawRect(
                                color = borderColor,
                                topLeft = Offset(0f, 0f),
                                size = androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        
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
                        
                        // 老人位置图标
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
                                            color = personColor.copy(alpha = 0.8f),
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
                                            personColor.copy(alpha = 0.7f),
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
            
            // 图例和位置列表
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .padding(start = 16.dp)
            ) {
                // 图例
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackgroundColor
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            MapTexts.legend[isChineseLanguage]!!, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 人物头像图例
                        locationDataList.filter { it.type == LocationType.ELDERLY }
                            .take(5)
                            .forEachIndexed { index, data ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(
                                                personColors[index % personColors.size],
                                                CircleShape
                                            )
                                    ) {
                                        data.avatarIcon?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(data.name, fontSize = 14.sp, color = textColor)
                                }
                            }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // UWB锚点图例
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                MapTexts.uwbAnchor[isChineseLanguage]!!, 
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                    }
                }
                
                // 位置列表
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackgroundColor
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = MapTexts.locationList[isChineseLanguage]!!,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(locationDataList.filter { it.type == LocationType.ELDERLY }) { data ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val index = locationDataList.indexOf(data)
                                    val personColor = personColors[index % personColors.size]
                                    
                                    // 人物标识点
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(personColor, CircleShape)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // 位置信息
                                    Column {
                                        Text(
                                            text = data.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "X: ${data.x.toInt()}, Y: ${data.y.toInt()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor.copy(alpha = 0.7f)
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