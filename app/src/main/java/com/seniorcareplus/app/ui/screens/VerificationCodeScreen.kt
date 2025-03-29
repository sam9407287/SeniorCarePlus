package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.auth.UserManager
import com.seniorcareplus.app.ui.components.SettingsButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(navController: NavController, username: String, email: String) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 用於滾動內容
    val scrollState = rememberScrollState()
    
    // 狀態變量
    var verificationCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showCodeDisplay by remember { mutableStateOf(true) }
    var displayedCode by remember { mutableStateOf(UserManager.getCurrentVerificationCode() ?: "----") }
    
    // 協程作用域
    val scope = rememberCoroutineScope()
    
    // 鍵盤控制
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    // UI文字翻譯
    val screenTitle = if (isChineseLanguage) "驗證碼" else "Verification Code"
    val screenSubtitle = if (isChineseLanguage) 
        "請輸入發送到您電子郵件的四位數驗證碼" 
    else 
        "Please enter the 4-digit verification code sent to your email"
    val codeLabel = if (isChineseLanguage) "驗證碼" else "Verification Code"
    val verifyButtonText = if (isChineseLanguage) "驗證" else "Verify"
    val resendCodeText = if (isChineseLanguage) "重新發送驗證碼" else "Resend Code"
    val errorTitle = if (isChineseLanguage) "錯誤" else "Error"
    val yourCodeText = if (isChineseLanguage) "您的驗證碼是：" else "Your verification code is: "
    
    // 顯示驗證碼並倒計時隱藏
    LaunchedEffect(displayedCode) {
        if (showCodeDisplay) {
            delay(10000) // 10秒後隱藏驗證碼
            showCodeDisplay = false
        }
    }
    
    // 自動聚焦到輸入框
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // 錯誤對話框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                isVerifying = false 
            },
            title = { Text(errorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        isVerifying = false
                    }
                ) {
                    Text(if (isChineseLanguage) "確定" else "OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.error,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 頂部標題欄，使用與主頁面一致的樣式和底色
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 空白占位，替代左側的用戶圖標
                    Spacer(modifier = Modifier.size(40.dp))
                    
                    // 居中標題，使用標準粗體字體
                    Text(
                        text = if (isChineseLanguage) "長者照護系統" else "SENIOR CARE PLUS",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isChineseLanguage) 28.sp else 24.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 右側語言切換按鈕
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { LanguageManager.toggleLanguage() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = if (isChineseLanguage) "切換英文" else "Switch to Chinese",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // 添加可點擊的說明文字
                        Text(
                            text = if (isChineseLanguage) "語言" else "Language",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable { LanguageManager.toggleLanguage() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 頁面圖標和標題
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = screenSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 顯示驗證碼（實際應用中通常通過郵件發送）
            if (showCodeDisplay) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = yourCodeText,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = displayedCode,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = 4.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (isChineseLanguage) "（將在10秒後隱藏）" else "(Will hide in 10 seconds)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 驗證碼輸入框
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { 
                    // 限制只能輸入4位數字
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        verificationCode = it
                    }
                },
                label = { Text(codeLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Password, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 驗證按鈕
            Button(
                onClick = { 
                    keyboardController?.hide()
                    isVerifying = true
                    
                    scope.launch {
                        // 驗證驗證碼
                        if (UserManager.verifyCode(verificationCode)) {
                            // 驗證成功，導航到重設密碼頁面
                            navController.navigate("reset_password/$username/$email") {
                                popUpTo("forgot_password") {
                                    inclusive = true
                                }
                            }
                        } else {
                            // 驗證失敗
                            errorMessage = if (isChineseLanguage) 
                                "驗證碼錯誤，請重試。" 
                            else 
                                "Incorrect verification code. Please try again."
                            showErrorDialog = true
                        }
                        
                        isVerifying = false
                    }
                },
                enabled = verificationCode.length == 4 && !isVerifying && !isResending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = verifyButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 重新發送驗證碼按鈕
            OutlinedButton(
                onClick = { 
                    isResending = true
                    
                    scope.launch {
                        // 生成新的驗證碼
                        val newCode = UserManager.generateVerificationCode()
                        displayedCode = newCode
                        showCodeDisplay = true
                        
                        // 重置驗證碼輸入
                        verificationCode = ""
                        focusRequester.requestFocus()
                        
                        isResending = false
                    }
                },
                enabled = !isVerifying && !isResending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = resendCodeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 