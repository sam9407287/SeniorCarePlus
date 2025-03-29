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
fun ChangePasswordScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 用於滾動內容
    val scrollState = rememberScrollState()
    
    // 狀態變量
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isChanging by remember { mutableStateOf(false) }
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
    val screenTitle = if (isChineseLanguage) "修改密碼" else "Change Password"
    val screenSubtitle = if (isChineseLanguage) "請輸入當前密碼和新密碼" else "Please enter your current password and new password"
    val currentPasswordLabel = if (isChineseLanguage) "當前密碼" else "Current Password"
    val newPasswordLabel = if (isChineseLanguage) "新密碼" else "New Password"
    val confirmPasswordLabel = if (isChineseLanguage) "確認密碼" else "Confirm Password"
    val changeButtonText = if (isChineseLanguage) "修改密碼" else "Change Password"
    val passwordErrorText = if (isChineseLanguage) "密碼不匹配" else "Passwords do not match"
    val passwordLengthErrorText = if (isChineseLanguage) "密碼至少需要6個字符" else "Password must be at least 6 characters"
    val errorTitle = if (isChineseLanguage) "錯誤" else "Error"
    val successTitle = if (isChineseLanguage) "成功" else "Success"
    val successMessage = if (isChineseLanguage) "密碼已成功修改，請使用新密碼登入。" else "Password has been changed successfully. Please login with your new password."
    
    // 錯誤對話框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                isChanging = false 
            },
            title = { Text(errorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        isChanging = false
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
            // 頂部欄 (簡潔版本)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isChineseLanguage) "返回" else "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = screenTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 設置按鈕
                SettingsButton()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            // 當前密碼輸入框
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text(currentPasswordLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showCurrentPassword) "隱藏密碼" else "顯示密碼",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
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
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            // 修改密碼按鈕
            Button(
                onClick = { 
                    keyboardController?.hide()
                    isChanging = true
                    
                    scope.launch {
                        // 首先验证当前密码
                        val username = UserManager.getCurrentUsername() ?: ""
                        val isCurrentPasswordValid = UserManager.verifyPassword(username, currentPassword)
                        
                        if (!isCurrentPasswordValid) {
                            errorMessage = if (isChineseLanguage) 
                                "當前密碼不正確，請重新輸入。" 
                            else 
                                "Current password is incorrect. Please try again."
                            showErrorDialog = true
                            isChanging = false
                            return@launch
                        }
                        
                        // 修改密碼
                        val result = UserManager.resetPassword(username, newPassword)
                        isChanging = false
                        
                        if (result) {
                            showSuccessDialog = true
                        } else {
                            errorMessage = if (isChineseLanguage) 
                                "修改密碼失敗，請稍後再試。" 
                            else 
                                "Failed to change password. Please try again later."
                            showErrorDialog = true
                        }
                    }
                },
                enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty() 
                        && passwordsMatch && passwordLongEnough && !isChanging,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isChanging) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = changeButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 