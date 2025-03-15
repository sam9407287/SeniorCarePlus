package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController = rememberNavController()) {
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
    
    // 模拟数据
    val locationDataList = remember {
        mutableStateListOf(
            // 老人位置 - 分配不同的头像图标和颜色
            LocationData("E001", "张三", 150f, 200f, LocationType.ELDERLY, avatarIcon = personIcons[0]),
            LocationData("E002", "李四", 250f, 150f, LocationType.ELDERLY, avatarIcon = personIcons[1]),
            LocationData("E003", "王五", 300f, 350f, LocationType.ELDERLY, avatarIcon = personIcons[2]),
            LocationData("E004", "赵六", 180f, 280f, LocationType.ELDERLY, avatarIcon = personIcons[3]),
            LocationData("E005", "钱七", 380f, 180f, LocationType.ELDERLY, avatarIcon = personIcons[4]),
            
            // UWB锚点位置
            LocationData("U001", "锚点1", 50f, 50f, LocationType.UWB_ANCHOR),
            LocationData("U002", "锚点2", 450f, 50f, LocationType.UWB_ANCHOR),
            LocationData("U003", "锚点3", 450f, 450f, LocationType.UWB_ANCHOR),
            LocationData("U004", "锚点4", 50f, 450f, LocationType.UWB_ANCHOR)
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
    
    // 模拟数据更新（每5秒随机移动老人位置）
    LaunchedEffect(key1 = "locationUpdate") {
        while(true) {
            kotlinx.coroutines.delay(5000)
            for (i in locationDataList.indices) {
                val data = locationDataList[i]
                if (data.type == LocationType.ELDERLY) {
                    // 随机移动老人位置
                    val newX = (data.x + Random.nextFloat() * 20 - 10).coerceIn(60f, 440f)
                    val newY = (data.y + Random.nextFloat() * 20 - 10).coerceIn(60f, 440f)
                    locationDataList[i] = data.copy(x = newX, y = newY)
                }
            }
        }
    }
    
    var showLegend by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("室内地图与定位") },
                actions = {
                    IconButton(onClick = { showLegend = !showLegend }) {
                        Icon(Icons.Default.Settings, contentDescription = "显示/隐藏图例")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 地图标题
            Text(
                text = "室内实时位置图",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 地图与图例的行
            Row(modifier = Modifier.fillMaxSize()) {
                // 地图区域
                Box(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .background(Color(0xFFF5F5F5))
                        .border(1.dp, Color.Gray)
                ) {
                    // 底层地图图像 - 使用fallback图片资源
                    // 注意：使用预先准备的房间平面图PNG资源
                    Image(
                        painter = painterResource(id = R.drawable.floor_map_bg),
                        contentDescription = "地图背景",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                        alpha = 0.8f
                    )
                    
                    // 房间墙壁现在直接在底图上，不需要再绘制
                    
                    // 绘制UWB锚点位置
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        // 绘制UWB锚点位置
                        locationDataList.filter { it.type == LocationType.UWB_ANCHOR }.forEach { data ->
                            // 缩放到画布大小
                            val x = data.x * canvasWidth / 500f
                            val y = data.y * canvasHeight / 500f
                            
                            // 绘制UWB定位锚点（三角形）
                            val triangleSize = 12f
                            // 三角形的三个顶点
                            val p1 = Offset(x, y - triangleSize)
                            val p2 = Offset(x - triangleSize, y + triangleSize)
                            val p3 = Offset(x + triangleSize, y + triangleSize)
                            
                            drawPath(
                                path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(p1.x, p1.y)
                                    lineTo(p2.x, p2.y)
                                    lineTo(p3.x, p3.y)
                                    close()
                                },
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                    
                    // 老人位置图标 - 使用Box而不是Canvas绘制，这样可以放置图标
                    locationDataList.filter { it.type == LocationType.ELDERLY }.forEachIndexed { index, data ->
                        val personColor = personColors[index % personColors.size]
                        
                        // 计算位置坐标
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
                            data.avatarIcon?.let { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = personColor.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp)
                                ) {
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
                                color = personColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .offset(y = 42.dp)
                                    .background(Color.White.copy(alpha = 0.7f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // 图例和位置列表
                if (showLegend) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                    ) {
                        // 图例
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("图例", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // 人物头像图例
                                locationDataList.filter { it.type == LocationType.ELDERLY }
                                    .take(5)
                                    .forEachIndexed { index, data ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            data.avatarIcon?.let { icon ->
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = personColors[index % personColors.size],
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(data.name, fontSize = 12.sp)
                                        }
                                    }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(modifier = Modifier.size(16.dp)) {
                                        val triangleSize = 6f
                                        val p1 = Offset(8f, 4f)
                                        val p2 = Offset(2f, 12f)
                                        val p3 = Offset(14f, 12f)
                                        
                                        drawPath(
                                            path = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(p1.x, p1.y)
                                                lineTo(p2.x, p2.y)
                                                lineTo(p3.x, p3.y)
                                                close()
                                            },
                                            color = Color(0xFFF44336)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("UWB锚点", fontSize = 12.sp)
                                }
                            }
                        }
                        
                        // 位置列表
                        Text(
                            text = "位置列表",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(locationDataList) { data ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (data.type == LocationType.ELDERLY) {
                                            // 为老人显示头像图标
                                            data.avatarIcon?.let { icon ->
                                                val index = locationDataList.indexOf(data)
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = personColors[index % personColors.size],
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            // UWB锚点图标
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = null,
                                                tint = Color(0xFFF44336),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "${data.name} (${data.id})",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "X: ${String.format("%.1f", data.x)}, Y: ${String.format("%.1f", data.y)}",
                                                style = MaterialTheme.typography.bodySmall
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
} 