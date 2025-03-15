@file:Suppress("UnusedImport", "RedundantSuppression")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.screens

// 強制Android Studio重新索引此文件
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.SeniorCareTopBar
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager

@Composable
fun RegionScreen(navController: NavController) {
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isChineseLanguage) "返回" else "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = if (isChineseLanguage) "區域管理" else "Region Management",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // 深色模式切换按钮
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
            
            // 内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isChineseLanguage) "區域管理" else "Region Management",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isChineseLanguage) "查看和管理患者的位置信息" else "View and manage patient location information",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    IconButton(
                        onClick = { 
                            navController.navigate("map") {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = if (isChineseLanguage) "查看地圖" else "View Map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Text(
                        text = if (isChineseLanguage) "點擊查看室內地圖" else "Click to view indoor map",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureScreen(navController: NavController) {
    FeatureScreenTemplate(
        title = "體溫",
        content = "患者體溫監測數據",
        navController = navController
    )
}

@Composable
fun TimerFeatureScreen(navController: NavController) {
    FeatureScreenTemplate(
        title = "定時",
        content = "藥物和護理提醒",
        navController = navController
    )
}

@Composable
fun DiaperScreen(navController: NavController) {
    FeatureScreenTemplate(
        title = "尿布",
        content = "尿布更換記錄和提醒",
        navController = navController
    )
}

@Composable
fun ButtonScreen(navController: NavController) {
    FeatureScreenTemplate(
        title = "按鍵",
        content = "緊急呼叫按鈕",
        navController = navController
    )
}

@Composable
fun HeartRateScreen(navController: NavController) {
    FeatureScreenTemplate(
        title = "心率",
        content = "患者心率監測數據",
        navController = navController
    )
}

@Composable
fun FeatureScreenTemplate(
    title: String,
    content: String,
    navController: NavController
) {
    // 获取当前语言和主题状态
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = content,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isChineseLanguage) "1 位患者正在呼叫！" else "1 patient calling!",
                color = MaterialTheme.colorScheme.error,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
} 