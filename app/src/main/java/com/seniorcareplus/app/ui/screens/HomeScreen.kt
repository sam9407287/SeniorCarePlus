package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathroom
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seniorcareplus.app.ui.theme.DarkCardBackground
import com.seniorcareplus.app.ui.theme.LightCardBackground
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager

data class FeatureItem(
    val nameZh: String,
    val nameEn: String,
    val icon: ImageVector,
    val route: String,
    val descriptionZh: String,
    val descriptionEn: String,
    val iconTint: Color
) {
    fun getName(isChinese: Boolean): String = if (isChinese) nameZh else nameEn
    fun getDescription(isChinese: Boolean): String = if (isChinese) descriptionZh else descriptionEn
}

@Composable
fun HomeScreen(navController: NavController) {
    // 获取当前主题状态和语言状态
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 功能列表 - 根据深色模式调整图标颜色
    val features = listOf(
        FeatureItem(
            nameZh = "體溫監測",
            nameEn = "Temperature Monitor",
            icon = Icons.Default.Thermostat,
            route = "temperature_monitor",
            descriptionZh = "監測患者體溫變化，及時發現異常",
            descriptionEn = "Monitor patient temperature changes, detect abnormalities in time",
            iconTint = if (isDarkTheme) Color(0xFFF48FB1) else Color(0xFFE91E63)
        ),
        FeatureItem(
            nameZh = "心率監測",
            nameEn = "Heart Rate Monitor",
            icon = Icons.Default.Favorite,
            route = "heart_rate_monitor",
            descriptionZh = "實時記錄心率數據，設置預警值",
            descriptionEn = "Record heart rate data in real-time, set alert thresholds",
            iconTint = if (isDarkTheme) Color(0xFFF48FB1) else Color(0xFFE91E63)
        ),
        FeatureItem(
            nameZh = "尿布監測",
            nameEn = "Diaper Monitor",
            icon = Icons.Default.BabyChangingStation,
            route = "diaper_monitor",
            descriptionZh = "監測尿布狀態，提醒及時更換",
            descriptionEn = "Monitor diaper status, remind for timely replacement",
            iconTint = if (isDarkTheme) Color(0xFF81D4FA) else Color(0xFF2196F3)
        ),
        FeatureItem(
            nameZh = "緊急呼叫",
            nameEn = "Emergency Call",
            icon = Icons.Default.Call,
            route = "emergency_button",
            descriptionZh = "一鍵呼叫護理人員緊急援助",
            descriptionEn = "One-click call for emergency assistance from care staff",
            iconTint = if (isDarkTheme) Color(0xFFFF7A7A) else Color(0xFFE53935)
        ),
        FeatureItem(
            nameZh = "定時提醒",
            nameEn = "Timer Reminder",
            icon = Icons.Default.Schedule,
            route = "timer",
            descriptionZh = "設置藥物、進食等定時提醒",
            descriptionEn = "Set timed reminders for medication, meals, etc.",
            iconTint = if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF4CAF50)
        ),
        FeatureItem(
            nameZh = "區域管理",
            nameEn = "Region Management",
            icon = Icons.Default.LocationOn,
            route = "region",
            descriptionZh = "查看患者位置，管理區域分配",
            descriptionEn = "View patient locations, manage area assignments",
            iconTint = if (isDarkTheme) Color(0xFFCE93D8) else Color(0xFF9C27B0)
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部标题和主题切换按钮
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "智慧照護系統" else "Smart Care System",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                

            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // 功能卡片列表
        items(features) { feature ->
            FeatureCard(
                feature = feature,
                isDarkTheme = isDarkTheme,
                isChineseLanguage = isChineseLanguage,
                onClick = { 
                    navController.navigate(feature.route) {
                        launchSingleTop = true
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FeatureCard(feature: FeatureItem, isDarkTheme: Boolean, isChineseLanguage: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DarkCardBackground else LightCardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(feature.iconTint.copy(alpha = if (isDarkTheme) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.getName(isChineseLanguage),
                    tint = feature.iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 文字内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.getName(isChineseLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = feature.getDescription(isChineseLanguage),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
} 