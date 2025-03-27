package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.auth.UserManager
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailboxScreen(navController: NavController) {
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    val isLoggedIn = remember { mutableStateOf(UserManager.isLoggedIn()) }
    
    // 模擬郵件數據
    val mailboxMessages = remember {
        mutableStateListOf(
            MailboxMessage(
                id = 1,
                title = if (isChineseLanguage) "歡迎使用長者照護系統" else "Welcome to Senior Care Plus",
                content = if (isChineseLanguage) 
                    "感謝您使用長者照護系統！我們希望這款應用能為您提供便利的照護體驗。如有任何問題或建議，請隨時聯繫我們。" 
                else 
                    "Thank you for using Senior Care Plus! We hope this app provides you with a convenient care experience. If you have any questions or suggestions, please feel free to contact us.",
                date = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3天前
                isRead = true,
                isImportant = false
            ),
            MailboxMessage(
                id = 2,
                title = if (isChineseLanguage) "系統更新通知" else "System Update Notice",
                content = if (isChineseLanguage) 
                    "我們的應用剛剛進行了更新，新增了郵箱功能，現在您可以接收應用公告和重要訊息了！請保持應用為最新版本以獲得最佳體驗。" 
                else 
                    "Our app has just been updated with the new mailbox feature. Now you can receive app announcements and important messages! Please keep the app updated to get the best experience.",
                date = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000, // 1天前
                isRead = false,
                isImportant = true
            ),
            MailboxMessage(
                id = 3,
                title = if (isChineseLanguage) "提醒功能改進" else "Reminder Feature Improvements",
                content = if (isChineseLanguage) 
                    "我們最近優化了提醒功能，現在提醒可以更穩定地工作，即使在切換主題後也不會出現問題。您的提醒數據已與您的帳戶關聯，確保數據安全。" 
                else 
                    "We have recently optimized the reminder function. Now reminders can work more stably, even after switching themes. Your reminder data is now associated with your account to ensure data security.",
                date = System.currentTimeMillis() - 2 * 60 * 60 * 1000, // 2小時前
                isRead = false,
                isImportant = false
            )
        )
    }
    
    // 當前選中的郵件
    var selectedMessage by remember { mutableStateOf<MailboxMessage?>(null) }
    
    // 加載狀態
    var isLoading by remember { mutableStateOf(true) }
    
    // 模擬加載過程
    LaunchedEffect(key1 = Unit) {
        delay(800)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isChineseLanguage) "個人郵箱" else "Personal Mailbox",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
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
                },
                actions = {
                    // 主題切換按鈕
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
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 未登錄提示
            if (!isLoggedIn.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isChineseLanguage) "請先登入" else "Please Login First",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isChineseLanguage) 
                            "您需要登入才能查看個人郵箱訊息" 
                        else 
                            "You need to login to view your mailbox messages",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            navController.navigate("login") {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Text(if (isChineseLanguage) "前往登入" else "Go to Login")
                    }
                }
                return@Scaffold
            }
            
            // 加載中
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }
            
            // 郵件列表
            if (selectedMessage == null) {
                if (mailboxMessages.isEmpty()) {
                    // 空郵箱提示
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isChineseLanguage) "郵箱空空如也" else "Your Mailbox is Empty",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isChineseLanguage) 
                                "當有公告或重要通知時，您將在此收到消息" 
                            else 
                                "You will receive messages here when there are announcements or important notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // 顯示郵件列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(mailboxMessages) { message ->
                            MessageItem(
                                message = message,
                                onClick = {
                                    selectedMessage = message
                                    // 標記為已讀
                                    val index = mailboxMessages.indexOf(message)
                                    if (index != -1) {
                                        mailboxMessages[index] = message.copy(isRead = true)
                                    }
                                },
                                onDelete = {
                                    mailboxMessages.remove(message)
                                }
                            )
                        }
                    }
                }
            } else {
                // 顯示選中郵件詳情
                MessageDetail(
                    message = selectedMessage!!,
                    onBackClick = { selectedMessage = null }
                )
            }
        }
    }
}

@Composable
fun MessageItem(
    message: MailboxMessage,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (!message.isRead) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 郵件圖標
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = if (message.isImportant) 
                        MaterialTheme.colorScheme.error
                    else 
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 郵件內容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = message.title,
                    fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dateFormat.format(Date(message.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // 刪除按鈕
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (isChineseLanguage) "刪除" else "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 未讀標記
            if (!message.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun MessageDetail(
    message: MailboxMessage,
    onBackClick: () -> Unit
) {
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 返回按鈕
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.Start)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = if (isChineseLanguage) "返回" else "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 郵件標題
        Text(
            text = message.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // 時間
        Text(
            text = dateFormat.format(Date(message.date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 郵件內容
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

// 郵件數據類
data class MailboxMessage(
    val id: Int,
    val title: String,
    val content: String,
    val date: Long, // 時間戳
    val isRead: Boolean = false,
    val isImportant: Boolean = false
)
