@file:Suppress("UNUSED_IMPORT", "NAME_SHADOWING", "UnusedImport")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.ButtonScreen
import com.example.myapplication.ui.screens.DiaperScreen
import com.example.myapplication.ui.screens.HeartRateScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.MapScreen
import com.example.myapplication.ui.screens.MonitorScreen
import com.example.myapplication.ui.screens.RegionScreen
import com.example.myapplication.ui.screens.TemperatureScreen
import com.example.myapplication.ui.screens.TimerScreen
import com.example.myapplication.ui.screens.NotificationScreen
import com.example.myapplication.ui.screens.EmergencyButtonScreen
import com.example.myapplication.ui.screens.DiaperMonitorScreen
import com.example.myapplication.ui.screens.HeartRateMonitorScreen
import com.example.myapplication.ui.screens.TemperatureMonitorScreen
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.ui.screens.EquipmentManagementScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemeManager
import com.example.myapplication.ui.theme.LanguageManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在启动后切换到普通主题
        setTheme(R.style.Theme_MyApplication)
        
        super.onCreate(savedInstanceState)
        setContent {
            // 使用我們的自定義主題，isDarkTheme會從ThemeManager中獲取
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun SeniorCareTopBar(onUserIconClick: () -> Unit = {}, onNotificationClick: () -> Unit = {}) {
    // 使用MaterialTheme的顏色而不是硬編碼的顏色
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側用戶圖標
            IconButton(
                onClick = onUserIconClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = if (isChineseLanguage) "用戶菜單" else "User Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // 居中標題
            Text(
                text = if (isChineseLanguage) "長者照護系統" else "SENIOR CARE PLUS",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 右側通知圖標
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = if (isChineseLanguage) "通知" else "Notifications",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // 管理頁面右側邊欄
    var showRightDrawer by remember { mutableStateOf(false) }
    
    // 檢查語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 使用MaterialTheme的顏色而不是硬編碼的顏色
    val navigationBarColor = if (ThemeManager.isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    // 定義底部導航欄的項目
    val bottomNavItems = listOf(
        BottomNavItem(
            name = if (isChineseLanguage) "主頁" else "Home",
            route = "home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "監控" else "Monitor",
            route = "monitor",
            icon = Icons.AutoMirrored.Filled.ShowChart
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "地圖" else "Map",
            route = "map",
            icon = Icons.Default.Map
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "定時" else "Timer",
            route = "timer",
            icon = Icons.Default.DateRange
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "更多" else "More",
            route = "more",
            icon = Icons.Default.MoreHoriz
        )
    )
    
    // 定義左側邊欄項目
    val leftDrawerItems = listOf(
        DrawerItem(
            if (isChineseLanguage) "個人郵箱" else "Personal Email", 
            if (isChineseLanguage) "登錄您的郵箱賬號" else "Log in to your email account", 
            Icons.Default.Email
        ),
        DrawerItem(
            if (isChineseLanguage) "個人資料" else "Personal Profile", 
            if (isChineseLanguage) "查看和編輯您的資料" else "View and edit your profile", 
            Icons.Default.Person
        ),
        DrawerItem(
            if (isChineseLanguage) "設置" else "Settings", 
            if (isChineseLanguage) "應用程序設置" else "Application settings", 
            Icons.Default.Settings
        ),
        DrawerItem(
            if (isChineseLanguage) "關於我們" else "About Us", 
            if (isChineseLanguage) "了解更多信息" else "Learn more about us", 
            Icons.Default.Info
        )
    )
    
    // 定義右側邊欄項目 - 管理頁面
    val adminItems = listOf(
        AdminItem(
            if (isChineseLanguage) "院友管理" else "Patient Management", 
            "patient_admin", 
            Icons.Default.People
        ),
        AdminItem(
            if (isChineseLanguage) "員工管理" else "Staff Management", 
            "staff_admin", 
            Icons.Default.Work
        ),
        AdminItem(
            if (isChineseLanguage) "設備管理" else "Equipment Management", 
            "equipment_admin", 
            Icons.Default.Build
        ),
        AdminItem(
            if (isChineseLanguage) "設定" else "Settings", 
            "settings_admin", 
            Icons.Default.Settings
        )
    )
    
    // 構建UI界面
    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    if (isChineseLanguage) "長者照護系統" else "SENIOR CARE PLUS",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                leftDrawerItems.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Column {
                                Text(item.title, style = MaterialTheme.typography.bodyLarge)
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                leftDrawerState.close()
                            }
                            // 處理側邊欄項目點擊
                            if (index == 2) { // 設置項目的索引
                                // 導航到設定頁面
                                navController.navigate("settings") {
                                    // 清空之前的settings路由，確保每次都是新進入
                                    popUpTo("settings") {
                                        inclusive = true
                                    }
                                    // 防止創建多個實例
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 主界面內容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars) // 添加狀態欄填充
            ) {
                // 獲取當前路由
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // 根據當前路由決定點擊用戶圖標的行為
                SeniorCareTopBar(
                    onUserIconClick = {
                        // 在所有頁面都打開側欄，不再做返回操作
                        scope.launch {
                            if (!leftDrawerState.isOpen) {
                                leftDrawerState.open()
                            } else {
                                leftDrawerState.close()
                            }
                        }
                    },
                    onNotificationClick = {
                        // 導航到通知頁面，不清空整個導航堆棧
                        navController.navigate("notifications") {
                            // 保留導航堆棧中的現有目的地
                            // 防止創建多個實例
                            launchSingleTop = true
                            // 保存和恢復狀態
                            restoreState = true
                        }
                    }
                )
                
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntryForBottomNav by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntryForBottomNav?.destination
                            
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.name) },
                                    label = { Text(item.name) },
                                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                    onClick = {
                                        if (item.route == "more") {
                                            // 點擊"更多"顯示右側邊欄
                                            showRightDrawer = true
                                        } else {
                                            // 針對首頁按鈕做特殊處理
                                            if (item.route == "home") {
                                                // 完全清空導航堆棧到主頁
                                                navController.navigate("home") {
                                                    popUpTo("home") {
                                                        inclusive = true
                                                    }
                                                }
                                            } 
                                            // 針對監控按鈕做特殊處理
                                            else if (item.route == "monitor" && currentRoute in listOf("temperature_monitor", "heart_rate_monitor", "diaper_monitor")) {
                                                // 如果已經在監控子頁面，點擊監控按鈕應該返回主監控頁面
                                                navController.navigate("monitor") {
                                                    // 清空監控相關頁面
                                                    popUpTo("monitor") {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                            else {
                                                navController.navigate(item.route) {
                                                    // 防止創建多個實例
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    // 防止重複點擊
                                                    launchSingleTop = true
                                                    // 恢復狀態
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("monitor") { MonitorScreen(navController) }
                        composable("map") { MapScreen(navController) }
                        composable("timer") { 
                            // 直接使用完整的TimerScreen實現
                            TimerScreen(navController) 
                        }
                        
                        // 添加新功能頁面的導航路由
                        composable("region") { RegionScreen(navController) }
                        composable("temperature") { TemperatureScreen(navController) }
                        composable("temperature_monitor") { TemperatureMonitorScreen(navController) }
                        composable("diaper") { DiaperScreen(navController) }
                        composable("diaper_monitor") { DiaperMonitorScreen(navController) }
                        composable("button") { ButtonScreen(navController) }
                        composable("emergency_button") { EmergencyButtonScreen(navController) }
                        composable("heart_rate") { HeartRateScreen(navController) }
                        composable("heart_rate_monitor") { HeartRateMonitorScreen(navController) }
                        
                        // 通知頁面
                        composable("notifications") { NotificationScreen(navController) }
                        
                        // 設定頁面
                        composable("settings") { SettingsScreen(navController) }
                        
                        // 管理頁面
                        composable("patient_admin") { AdminPageTemplate(title = "院友管理", navController = navController) }
                        composable("staff_admin") { AdminPageTemplate(title = "員工管理", navController = navController) }
                        composable("equipment_admin") { EquipmentManagementScreen(navController) }
                        composable("settings_admin") { AdminPageTemplate(title = "設定", navController = navController) }
                    }
                }
            }
            
            // 右側邊欄 - 管理功能菜單
            if (showRightDrawer) {
                AdminDrawer(
                    adminItems = adminItems,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            // 防止創建多個實例
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // 防止重複點擊
                            launchSingleTop = true
                            // 恢復狀態
                            restoreState = true
                        }
                        showRightDrawer = false
                    },
                    onClose = { showRightDrawer = false }
                )
            }
        }
    }
}

@Composable
fun AdminDrawer(
    adminItems: List<AdminItem>,
    onItemClick: (String) -> Unit,
    onClose: () -> Unit
) {
    // 檢查是否為深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 半透明背景，點擊關閉抽屉
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x44000000))
            .clickable { onClose() }
    ) {
        // 右側菜單內容
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                // 增加寬度以適應英文文本
                .fillMaxWidth(0.55f)
                .background(if (isDarkTheme) MaterialTheme.colorScheme.surface else Color(0xFFCDCDCD))
                .padding(top = 48.dp, start = 12.dp, end = 12.dp)
                // 阻止點擊事件傳遞到下層
                .clickable(enabled = false, onClick = {})
        ) {
            // 菜單項目
            adminItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)  // 減少垂直間距
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE0E0E0))
                        .clickable { onItemClick(item.route) }
                        .padding(vertical = 16.dp, horizontal = 12.dp),  // 添加水平內邊距，減少垂直內邊距
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,  // 確保文本居中對齊
                        maxLines = 2,  // 允許最多兩行文本
                        // 字體大小根據語言略微調整，英文字體略小以適應更長的文本
                        fontSize = if (isChineseLanguage) 16.sp else 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminPageTemplate(title: String, navController: androidx.navigation.NavController) {
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 可根据不同页面添加相应内容
                Text(
                    text = if (isChineseLanguage) "管理功能頁面" else "Management Function Page",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // 删除返回按钮
            }
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// 側邊欄項目數據類
data class DrawerItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// 管理功能項目數據類
data class AdminItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)