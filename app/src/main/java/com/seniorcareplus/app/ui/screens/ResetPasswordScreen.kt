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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun ResetPasswordScreen(navController: NavController, username: String, email: String) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 用於滾動內容
    val scrollState = rememberScrollState()
    
    // 狀態變量
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 密碼錯誤標識
    val passwordsMatch = newPassword == confirmPassword
    val passwordLongEnough = newPassword.length >= 6
    
    // 協程作用域
    val scope = rememberCoroutineScope()
    
    // 鍵盤控制
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // UI文字翻譯
    val screenTitle = if (isChineseLanguage) "重設密碼" else "Reset Password"
    val screenSubtitle = if (isChineseLanguage) "請設置您的新密碼" else "Please set your new password"
    val newPasswordLabel = if (isChineseLanguage) "新密碼" else "New Password"
    val confirmPasswordLabel = if (isChineseLanguage) "確認密碼" else "Confirm Password"
    val resetButtonText = if (isChineseLanguage) "重設密碼" else "Reset Password"
    val passwordErrorText = if (isChineseLanguage) "密碼不匹配" else "Passwords do not match"
    val passwordLengthErrorText = if (isChineseLanguage) "密碼至少需要6個字符" else "Password must be at least 6 characters"
    val errorTitle = if (isChineseLanguage) "錯誤" else "Error"
    val successTitle = if (isChineseLanguage) "成功" else "Success"
    val successMessage = if (isChineseLanguage) "密碼已成功重設，請使用新密碼登入。" else "Password has been reset successfully. Please login with your new password."
    
    // 錯誤對話框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                isResetting = false 
            },
            title = { Text(errorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        isResetting = false
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
            
            // 頁面圖標和標題
            Icon(
                imageVector = Icons.Default.LockReset,
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
            
            // 新密碼輸入框
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(newPasswordLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showNewPassword) "隱藏密碼" else "顯示密碼",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                isError = newPassword.isNotEmpty() && !passwordLongEnough,
                supportingText = {
                    if (newPassword.isNotEmpty() && !passwordLongEnough) {
                        Text(passwordLengthErrorText, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 確認密碼輸入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(confirmPasswordLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showConfirmPassword) "隱藏密碼" else "顯示密碼",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Text(passwordErrorText, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 重設密碼按鈕
            Button(
                onClick = { 
                    keyboardController?.hide()
                    isResetting = true
                    
                    scope.launch {
                        // 重設密碼
                        val result = UserManager.resetPassword(username, newPassword)
                        isResetting = false
                        
                        if (result) {
                            showSuccessDialog = true
                        } else {
                            errorMessage = if (isChineseLanguage) 
                                "重設密碼失敗，請稍後再試。" 
                            else 
                                "Failed to reset password. Please try again later."
                            showErrorDialog = true
                        }
                    }
                },
                enabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && passwordsMatch && passwordLongEnough && !isResetting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isResetting) {
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
            
            // 返回登录页面链接
            Text(
                text = if (isChineseLanguage) "返回登入頁面" else "Back to Login",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        // 导航到登录页面
                        navController.navigate("login") {
                            // 清除导航栈直到登录页面
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
} 