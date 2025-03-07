@file:Suppress("UnusedImport", "RedundantSuppression")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.screens

// 強制Android Studio重新索引此文件
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.SeniorCareTopBar

@Composable
fun RegionScreen(navController: NavController) {
    FeatureScreenTemplate(
        title = "區域",
        content = "患者位置監控",
        navController = navController
    )
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
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = content,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "1 patient calling!",
                color = MaterialTheme.colorScheme.error,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
} 