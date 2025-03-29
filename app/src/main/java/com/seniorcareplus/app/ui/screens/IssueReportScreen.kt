package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueReportScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 用於滾動內容
    val scrollState = rememberScrollState()
    
    // 狀態變量
    var issueTitle by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // CoroutineScope用於異步操作
    val scope = rememberCoroutineScope()
    
    // 類別選項列表
    val categories = if (isChineseLanguage) {
        listOf("應用程式問題", "裝置連接問題", "數據同步問題", "其他")
    } else {
        listOf("App Issues", "Device Connection Issues", "Data Synchronization Issues", "Others")
    }
    
    // 成功對話框
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text(text = if (isChineseLanguage) "問題已送出" else "Issue Submitted") },
            text = { Text(text = if (isChineseLanguage) "您的問題已成功送出，技術人員將盡快處理。" else "Your issue has been successfully submitted. Our technical staff will handle it as soon as possible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text(text = if (isChineseLanguage) "確定" else "OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
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
            horizontalAlignment = Alignment.Start
        ) {
            // 頂部欄 - 已移除返回按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "問題回報" else "Issue Report",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // 問題回報表單
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isChineseLanguage) "問題類別" else "Issue Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 類別選擇
                    categories.forEachIndexed { index, category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == index,
                                onClick = { selectedCategory = index },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 問題標題
                    Text(
                        text = if (isChineseLanguage) "問題標題" else "Issue Title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = issueTitle,
                        onValueChange = { issueTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                text = if (isChineseLanguage) "簡短描述您的問題" else "Briefly describe your issue",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 問題描述
                    Text(
                        text = if (isChineseLanguage) "問題詳細描述" else "Issue Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = issueDescription,
                        onValueChange = { issueDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { 
                            Text(
                                text = if (isChineseLanguage) "請詳細描述您遇到的問題，以便技術人員更好地幫助您" else "Please describe the issue in detail to help our technical staff assist you better",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 提交按鈕
                    Button(
                        onClick = {
                            // 模擬提交操作
                            scope.launch {
                                isSubmitting = true
                                // 模擬網絡請求的延遲
                                delay(1500)
                                isSubmitting = false
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = issueTitle.isNotBlank() && issueDescription.isNotBlank() && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isChineseLanguage) "送出問題回報" else "Submit Issue Report"
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 技術支援信息卡片
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isChineseLanguage) "技術支援資訊" else "Technical Support Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isChineseLanguage) 
                            "您也可以通過以下方式直接聯繫我們的技術支援團隊:" 
                        else 
                            "You can also contact our technical support team directly through:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 聯繫方式
                    SupportItem(
                        icon = Icons.Default.Email,
                        text = "tech-support@example-tech.com.tw"
                    )
                    
                    SupportItem(
                        icon = Icons.Default.Phone,
                        text = if (isChineseLanguage) "02-00000000 (技術支援專線)" else "02-00000000 (Technical Support Hotline)"
                    )
                    
                    SupportItem(
                        icon = Icons.Default.Schedule,
                        text = if (isChineseLanguage) "工作時間: 週一至週五 9:00-18:00" else "Working Hours: Mon-Fri 9:00-18:00"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SupportItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 