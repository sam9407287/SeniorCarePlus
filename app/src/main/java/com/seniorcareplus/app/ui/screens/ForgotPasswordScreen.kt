package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 用於滾動內容
    val scrollState = rememberScrollState()
    
    // 狀態變量
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isRequestingReset by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 協程作用域
    val scope = rememberCoroutineScope()
    
    // 鍵盤控制
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // UI文字翻譯
    val screenTitle = if (isChineseLanguage) "忘記密碼" else "Forgot Password"
    val screenSubtitle = if (isChineseLanguage) "請輸入您的帳號和電子郵件以重置密碼" else "Please enter your username and email to reset your password"
    val usernameLabel = if (isChineseLanguage) "用戶名" else "Username"
    val emailLabel = if (isChineseLanguage) "電子郵件" else "Email"
    val requestCodeButtonText = if (isChineseLanguage) "發送驗證碼" else "Send Verification Code"
    val backToLoginText = if (isChineseLanguage) "返回登入頁面" else "Back to Login"
    val errorTitle = if (isChineseLanguage) "錯誤" else "Error"
    val testAccountTitle = if (isChineseLanguage) "測試用帳號" else "Test Account"
    
    // 錯誤對話框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                isRequestingReset = false 
            },
            title = { Text(errorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        isRequestingReset = false
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
                        fontSize = 28.sp,
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
                        
                        // 添加說明文字
                        Text(
                            text = if (isChineseLanguage) "語言" else "Language",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // 頁面圖標和標題
            Icon(
                imageVector = Icons.Default.Lock,
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
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 用戶名輸入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(usernameLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Person, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 電子郵件輸入框
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(emailLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Email, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 發送驗證碼按鈕
            Button(
                onClick = { 
                    // 隱藏鍵盤並開始請求驗證碼
                    keyboardController?.hide()
                    isRequestingReset = true
                    
                    scope.launch {
                        // 檢查用戶資訊是否正確
                        val result = UserManager.verifyUserEmail(username, email)
                        
                        when(result) {
                            0 -> {
                                // 生成驗證碼並導航到驗證碼頁面
                                val verificationCode = UserManager.generateVerificationCode()
                                // 在實際應用中，這裡應發送電子郵件
                                // 在這個示例中，我們只是將驗證碼顯示在日誌中
                                navController.navigate("verification_code/$username/$email")
                            }
                            1 -> {
                                errorMessage = if (isChineseLanguage) 
                                    "找不到此用戶。" 
                                else 
                                    "User not found."
                                showErrorDialog = true
                            }
                            2 -> {
                                errorMessage = if (isChineseLanguage) 
                                    "提供的電子郵件與用戶記錄不匹配。" 
                                else 
                                    "The provided email does not match the user record."
                                showErrorDialog = true
                            }
                            else -> {
                                errorMessage = if (isChineseLanguage) 
                                    "發送驗證碼過程中發生錯誤。" 
                                else 
                                    "An error occurred during the verification code process."
                                showErrorDialog = true
                            }
                        }
                        
                        isRequestingReset = false
                    }
                },
                enabled = username.isNotBlank() && email.isNotBlank() && !isRequestingReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isRequestingReset) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = requestCodeButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 返回登入頁面
            TextButton(
                onClick = { 
                    navController.navigate("login") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = backToLoginText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 測試用帳號卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = testAccountTitle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isChineseLanguage) "用戶名: admin" else "Username: admin",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (isChineseLanguage) "電子郵件: admin@example.com" else "Email: admin@example.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (isChineseLanguage) "注意: 每次發送驗證碼都會生成新的四位數驗證碼" else "Note: A new 4-digit code will be generated each time",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
} 