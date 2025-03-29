package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seniorcareplus.app.R
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 頂部欄
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 添加返回按钮 - 使用popBackStack确保完全移除当前页面
                IconButton(
                    onClick = { 
                        // 使用popBackStack代替navigateUp以确保页面完全从导航栈中移除
                        navController.popBackStack() 
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = if (isChineseLanguage) "返回" else "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (isChineseLanguage) "關於我們" else "About Us",
                    fontSize = if (isChineseLanguage) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // 公司標誌
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 公司名稱
            Text(
                text = if (isChineseLanguage) "範例科技股份有限公司" else "Example Technology Co., Ltd.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 公司標語
            Text(
                text = if (isChineseLanguage) 
                    "用心照顧，守護健康" 
                else 
                    "Caring with Heart, Protecting Health",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 公司介紹區塊
            AboutSection(
                title = if (isChineseLanguage) "我們的使命" else "Our Mission",
                content = if (isChineseLanguage)
                    "致力於為長者提供最優質的照護服務，以專業、關懷和尊重的態度，讓每位長者都能享有尊嚴和舒適的晚年生活。"
                else
                    "Dedicated to providing seniors with the highest quality care services, with a professional, caring and respectful attitude, ensuring every senior can enjoy a dignified and comfortable life in their later years."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 公司歷史區塊
            AboutSection(
                title = if (isChineseLanguage) "公司歷史" else "Our History",
                content = if (isChineseLanguage)
                    "成立於2020年，我們從一家小型護理之家發展成為台灣頂尖的長者照護機構。過去三年來，我們已經服務超過5,000位長者，並在全台設有12個服務中心。"
                else
                    "Founded in 2020, we have grown from a small nursing home to become one of Taiwan's leading senior care institutions. Over the past three years, we have served more than 5,000 seniors and established 12 service centers across Taiwan."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 服務項目區塊
            AboutSection(
                title = if (isChineseLanguage) "我們的服務" else "Our Services",
                content = if (isChineseLanguage)
                    "• 專業長者護理服務\n• 居家照護支援\n• 健康監測與管理\n• 長者社交活動\n• 復健與治療服務\n• 專業醫療諮詢"
                else
                    "• Professional senior nursing care\n• Home care support\n• Health monitoring and management\n• Social activities for seniors\n• Rehabilitation and therapy services\n• Professional medical consultation"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 聯繫資訊卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isChineseLanguage) "聯繫我們" else "Contact Us",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 地址
                    ContactItem(
                        icon = Icons.Default.LocationOn,
                        text = if (isChineseLanguage) 
                            "台北市信義區市府路45號101大樓" 
                        else 
                            "Taipei 101 Tower, No. 45, Shifu Road, Xinyi District, Taipei City, Taiwan"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 電話
                    ContactItem(
                        icon = Icons.Default.Phone,
                        text = if (isChineseLanguage) 
                            "電話: 02-00000000" 
                        else 
                            "Tel: +886-2-00000000"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 手機
                    ContactItem(
                        icon = Icons.Default.Smartphone,
                        text = if (isChineseLanguage) 
                            "手機: 0900-000-000" 
                        else 
                            "Mobile: +886-900-000-000"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 郵箱
                    ContactItem(
                        icon = Icons.Default.Email,
                        text = "example@seniorcare.com.tw"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 網站
                    ContactItem(
                        icon = Icons.Default.Public,
                        text = "www.example-seniorcare.com.tw"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 服務時間
            ServiceHours(isChineseLanguage)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 版權聲明
            Text(
                text = if (isChineseLanguage) 
                    "© 2023 範例科技股份有限公司. 版權所有." 
                else 
                    "© 2023 Example Technology Co., Ltd. All Rights Reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AboutSection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ContactItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ServiceHours(isChineseLanguage: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isChineseLanguage) "服務時間" else "Service Hours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 服務時間表
            ServiceHourItem(
                day = if (isChineseLanguage) "週一至週五" else "Monday - Friday",
                hours = if (isChineseLanguage) "早上 8:00 - 晚上 6:00" else "8:00 AM - 6:00 PM"
            )
            
            ServiceHourItem(
                day = if (isChineseLanguage) "週六" else "Saturday",
                hours = if (isChineseLanguage) "早上 9:00 - 下午 5:00" else "9:00 AM - 5:00 PM"
            )
            
            ServiceHourItem(
                day = if (isChineseLanguage) "週日" else "Sunday",
                hours = if (isChineseLanguage) "早上 10:00 - 下午 4:00" else "10:00 AM - 4:00 PM"
            )
            
            ServiceHourItem(
                day = if (isChineseLanguage) "國定假日" else "Public Holidays",
                hours = if (isChineseLanguage) "早上 10:00 - 下午 2:00" else "10:00 AM - 2:00 PM"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isChineseLanguage) 
                    "* 緊急服務全天候 24 小時提供" 
                else 
                    "* Emergency services available 24/7",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ServiceHourItem(day: String, hours: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = hours,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 