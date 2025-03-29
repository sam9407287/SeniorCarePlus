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
    var showSuccessDialog by remember { mutableStateOf(false) }
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
    val resetButtonText = if (isChineseLanguage) "重置密碼" else "Reset Password"
    val backToLoginText = if (isChineseLanguage) "返回登入頁面" else "Back to Login"
    val errorTitle = if (isChineseLanguage) "錯誤" else "Error"
    val successTitle = if (isChineseLanguage) "成功" else "Success"
    val successMessage = if (isChineseLanguage) "重置密碼申請已提交，請查看您的電子郵件獲取臨時密碼。" else "Password reset request submitted. Please check your email for a temporary password."
    
    // 成功對話框
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                // 返回登入頁面
                navController.navigate("login") {
                    popUpTo("login") {
                        inclusive = true
                    }
                }
            },
            title = { Text(successTitle) },
            text = { Text(successMessage) },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        // 返回登入頁面
                        navController.navigate("login") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text(if (isChineseLanguage) "確定" else "OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
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
            // 頂部欄
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = screenTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
            
            // 重置密碼按鈕
            Button(
                onClick = { 
                    // 隱藏鍵盤並開始重置密碼請求
                    keyboardController?.hide()
                    isRequestingReset = true
                    
                    scope.launch {
                        // 嘗試重置密碼
                        val result = UserManager.requestPasswordReset(username, email)
                        isRequestingReset = false
                        
                        when(result) {
                            0 -> showSuccessDialog = true
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
                                    "重置密碼過程中發生錯誤。" 
                                else 
                                    "An error occurred during the password reset process."
                                showErrorDialog = true
                            }
                        }
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
                        text = resetButtonText,
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
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
} 