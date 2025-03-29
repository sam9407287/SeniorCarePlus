package com.seniorcareplus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.seniorcareplus.app.MainActivity
import androidx.compose.foundation.BorderStroke
import android.util.Log

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 用於滾動內容
    val scrollState = rememberScrollState()
    
    // 狀態變量
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 協程作用域
    val scope = rememberCoroutineScope()
    
    // 鍵盤控制
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // 檢查用戶是否已登錄或是否有保存的登錄憑證
    LaunchedEffect(Unit) {
        if (UserManager.isLoggedIn()) {
            // 如果已登錄，自動填充用戶名
            username = UserManager.getCurrentUsername() ?: ""
        } else if (UserManager.hasRememberedCredentials()) {
            // 如果有保存的憑證，自動填充用戶名和密碼
            username = UserManager.getSavedUsername()
            password = UserManager.getSavedPassword()
            rememberMe = true
            
            Log.d("LoginScreen", "自動填充記住的憑證: $username")
        }
    }
    
    // UI文字翻譯
    val loginTitle = if (isChineseLanguage) "歡迎回來" else "Welcome Back"
    val loginSubtitle = if (isChineseLanguage) "請登入您的帳號" else "Please sign in to your account"
    val usernameLabel = if (isChineseLanguage) "用戶名" else "Username"
    val passwordLabel = if (isChineseLanguage) "密碼" else "Password"
    val rememberMeText = if (isChineseLanguage) "記住我" else "Remember me"
    val forgotPasswordText = if (isChineseLanguage) "忘記密碼?" else "Forgot password?"
    val loginButtonText = if (isChineseLanguage) "登入" else "Login"
    val orText = if (isChineseLanguage) "或者" else "OR"
    val googleSignInText = if (isChineseLanguage) "使用Google帳號登入" else "Sign in with Google"
    val registerText = if (isChineseLanguage) "沒有帳號? 註冊" else "Don't have an account? Register"
    val loginErrorTitle = if (isChineseLanguage) "登入失敗" else "Login Failed"
    val loginSuccessTitle = if (isChineseLanguage) "登入成功" else "Login Successful"
    val loginSuccessMessage = if (isChineseLanguage) "歡迎回來，" else "Welcome back, "
    
    // 登入錯誤對話框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                isLoggingIn = false  // 確保關閉對話框時重置登入狀態
            },
            title = { Text(loginErrorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { 
                    showErrorDialog = false
                    isLoggingIn = false  // 確保點擊確認按鈕時重置登入狀態
                }) {
                    Text(if (isChineseLanguage) "確定" else "OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.error,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    // 登入成功對話框
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                // 導航到主頁
                navController.navigate("home") {
                    popUpTo("home") {
                        inclusive = true
                    }
                }
            },
            title = { Text(loginSuccessTitle) },
            text = { Text(loginSuccessMessage + username) },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        // 導航到主頁
                        navController.navigate("home") {
                            popUpTo("home") {
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
            // 頂部欄 (移除返回按鈕)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "登入" else "Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
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
            
            // 登入圖標和標題
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = loginTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = loginSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 用戶名輸入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(usernameLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Email, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
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
            
            // 密碼輸入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(passwordLabel) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "隱藏密碼" else "顯示密碼",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
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
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 記住我和忘記密碼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { 
                        // 切換記住我狀態
                        rememberMe = !rememberMe 
                        
                        // 如果取消「記住我」，可以立即清除已保存的憑證
                        if (!rememberMe && UserManager.hasRememberedCredentials()) {
                            // 取消記住我時，如果用戶已登出，則清除已保存的憑證
                            if (!UserManager.isLoggedIn()) {
                                UserManager.logout(clearRememberMe = true)
                            }
                        }
                    }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { newState -> 
                            rememberMe = newState 
                            
                            // 如果取消「記住我」，可以立即清除已保存的憑證
                            if (!newState && UserManager.hasRememberedCredentials()) {
                                // 取消記住我時，如果用戶已登出，則清除已保存的憑證
                                if (!UserManager.isLoggedIn()) {
                                    UserManager.logout(clearRememberMe = true)
                                }
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = rememberMeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (isChineseLanguage) "修改密碼" else "Change Password",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { 
                                // 導航到修改密碼頁面
                                navController.navigate("change_password")
                            }
                            .padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = forgotPasswordText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { 
                            // 導航到忘記密碼頁面
                            navController.navigate("forgot_password")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 登入按鈕
            Button(
                onClick = { 
                    // 隱藏鍵盤並開始登入
                    keyboardController?.hide()
                    isLoggingIn = true
                    
                    scope.launch {
                        // 嘗試登入，並傳遞「記住我」的狀態
                        if (UserManager.login(username, password, rememberMe)) {
                            // 登入成功
                            showSuccessDialog = true
                            
                            // 通知ReminderViewModel更新提醒數據
                            MainActivity.sharedReminderViewModel?.onLoginStateChanged()
                            
                            // 更新登錄狀態
                            MainActivity.updateLoginState?.invoke()
                            
                            // 延遲一下再導航，讓成功訊息顯示
                            kotlinx.coroutines.delay(1000)
                            navController.navigate("home") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }
                        } else {
                            // 登入失敗
                            errorMessage = if (isChineseLanguage) 
                                "用戶名或密碼錯誤，請再試一次。" 
                            else 
                                "Username or password is incorrect. Please try again."
                            showErrorDialog = true
                            isLoggingIn = false  // 重置登入狀態
                        }
                    }
                },
                enabled = username.isNotBlank() && password.isNotBlank() && !isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = loginButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 分隔線和"或者"文字
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                )
                
                Text(
                    text = orText,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Divider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Google登入按鈕
            OutlinedButton(
                onClick = { 
                    /* Google登入處理 */ 
                    // 在實際應用中，這裡會調用Google登入API
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 使用Google圖標（實際應用應該使用真正的Google圖標）
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color.Blue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = googleSignInText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 註冊選項
            Text(
                text = if (isChineseLanguage) "沒有帳戶？註冊" else "Don't have an account? Register",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { navController.navigate("register") },
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 預設帳號提示
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
                        text = if (isChineseLanguage) "預設管理員帳號" else "Default Admin Account",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isChineseLanguage) "用戶名: admin" else "Username: admin",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (isChineseLanguage) "密碼: 00000000" else "Password: 00000000",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (isChineseLanguage) "郵箱: admin@example.com" else "Email: admin@example.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 