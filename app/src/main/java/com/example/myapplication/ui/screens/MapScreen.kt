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
    
    // 使用更大的坐标值，确保在大地图上分布更合理
    val locationDataList = remember {
        mutableStateListOf(
            // 老人位置 - 分配不同的头像图标和颜色，使用更大的坐标差距
            LocationData(
                "E001", 
                MapTexts.elderlyNames["E001"]?.get(isChineseLanguage) ?: "张三", 
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
                MapTexts.elderlyNames["E004"]?.get(isChineseLanguage) ?: "赵六",
                2500f, 4000f,
                LocationType.ELDERLY,
                avatarIcon = personIcons[3]
            ),
            LocationData(
                "E005",
                MapTexts.elderlyNames["E005"]?.get(isChineseLanguage) ?: "钱七", 
                1800f, 3000f,
                LocationType.ELDERLY, 
                avatarIcon = personIcons[4]
            ),
            
            // UWB锚点位置 - 放置在建筑物的四个角落，使用更大的坐标范围
            LocationData(
                "U001", 
                MapTexts.anchorNames["U001"]?.get(isChineseLanguage) ?: "锚点1", 
                500f, 500f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U002", 
                MapTexts.anchorNames["U002"]?.get(isChineseLanguage) ?: "锚点2", 
                2500f, 500f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U003", 
                MapTexts.anchorNames["U003"]?.get(isChineseLanguage) ?: "锚点3", 
                2500f, 2500f, 
                LocationType.UWB_ANCHOR
            ),
            LocationData(
                "U004", 
                MapTexts.anchorNames["U004"]?.get(isChineseLanguage) ?: "锚点4", 
                500f, 2500f, 
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
    
    // 注意：已移除随机移动功能，将来会直接从后端接收位置数据
    
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 添加点击监听器到整个地图区域，点击非图标区域时隐藏详细信息
                        .clickable { hoveredDeviceId = null }
                ) {
                    // 使用上传的地图图片作为背景
                    Image(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = "Floor Plan Map",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 绘制UWB锚点位置 - 调整坐标系统的比例因子为1000f
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
                            // UWB锚点图标
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .clickable(onClick = { 
                                        // 切换悬停状态
                                        hoveredDeviceId = if (hoveredDeviceId == data.id) null else data.id
                                    })
                                    .zIndex(1f)
                            )
                        }
                    }
                    
                    // 老人位置图标 - 在地图上层显示 - 调整坐标系统的比例因子为1000f
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
                            // 显示人物头像图标
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = personColor.copy(alpha = 0.9f),
                                        shape = CircleShape
                                    )
                                    .clickable(onClick = {
                                        // 切换悬停状态
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
                                    .zIndex(1f)
                            )
                        }
                    }
                    
                    // 单独渲染所有设备的信息提示框，确保它们在最上层
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
                                    .zIndex(10f) // 确保在最上层
                            ) {
                                // 信息提示框显示在图标正下方，不会遮挡图标本身
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