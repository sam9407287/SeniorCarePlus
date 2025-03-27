package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import com.example.myapplication.auth.UserManager
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    // 語言和主題設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 表單狀態
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // 輸入驗證
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    // 密碼顯示設置
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // 註冊流程狀態
    var isRegistering by remember { mutableStateOf(false) }
    var registrationSuccess by remember { mutableStateOf(false) }
    var registrationError by remember { mutableStateOf<String?>(null) }
    
    // 鍵盤和焦點控制
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    // 驗證用戶名
    fun validateUsername(): Boolean {
        return when {
            username.isBlank() -> {
                usernameError = if (isChineseLanguage) "用戶名不能為空" else "Username cannot be empty"
                false
            }
            username.length < 4 -> {
                usernameError = if (isChineseLanguage) "用戶名至少需要4個字符" else "Username must be at least 4 characters"
                false
            }
            UserManager.isUserExists(username) -> {
                usernameError = if (isChineseLanguage) "用戶名已被使用" else "Username is already taken"
                false
            }
            else -> {
                usernameError = null
                true
            }
        }
    }
    
    // 驗證密碼
    fun validatePassword(): Boolean {
        return when {
            password.isBlank() -> {
                passwordError = if (isChineseLanguage) "密碼不能為空" else "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                passwordError = if (isChineseLanguage) "密碼至少需要6個字符" else "Password must be at least 6 characters"
                false
            }
            else -> {
                passwordError = null
                true
            }
        }
    }
    
    // 驗證確認密碼
    fun validateConfirmPassword(): Boolean {
        return when {
            confirmPassword.isBlank() -> {
                confirmPasswordError = if (isChineseLanguage) "請確認密碼" else "Please confirm your password"
                false
            }
            confirmPassword != password -> {
                confirmPasswordError = if (isChineseLanguage) "密碼不匹配" else "Passwords do not match"
                false
            }
            else -> {
                confirmPasswordError = null
                true
            }
        }
    }
    
    // 驗證電子郵件
    fun validateEmail(): Boolean {
        if (email.isBlank()) {
            // 電子郵件是可選的，允許為空
            emailError = null
            return true
        }
        
        // 簡單的電子郵件驗證
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (!email.matches(emailPattern.toRegex())) {
            emailError = if (isChineseLanguage) "無效的電子郵件格式" else "Invalid email format"
            false
        } else {
            emailError = null
            true
        }
    }
    
    // 驗證所有字段
    fun validateAll(): Boolean {
        val isUsernameValid = validateUsername()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        val isEmailValid = validateEmail()
        
        return isUsernameValid && isPasswordValid && isConfirmPasswordValid && isEmailValid
    }
    
    // 處理註冊
    fun handleRegister() {
        if (!validateAll()) {
            return
        }
        
        // 開始註冊流程
        isRegistering = true
        registrationError = null
        
        scope.launch {
            try {
                // 電子郵件為可選字段，如果為空則傳入null
                val emailToSave = if (email.isBlank()) null else email
                
                val success = UserManager.register(username, password, emailToSave)
                
                if (success) {
                    // 註冊成功
                    registrationSuccess = true
                    // 延遲一下，讓成功消息顯示
                    delay(1500)
                    // 返回登錄頁面
                    navController.popBackStack()
                } else {
                    // 註冊失敗
                    registrationError = if (isChineseLanguage) 
                        "註冊失敗，請稍後再試" 
                    else 
                        "Registration failed, please try again later"
                }
            } catch (e: Exception) {
                // 發生異常
                registrationError = e.message ?: if (isChineseLanguage) 
                    "註冊過程中發生錯誤" 
                else 
                    "An error occurred during registration"
            } finally {
                isRegistering = false
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 主要內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 標題
            Text(
                text = if (isChineseLanguage) "註冊帳戶" else "Register Account",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // 用戶名輸入
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    if (usernameError != null) validateUsername()
                },
                label = { Text(if (isChineseLanguage) "用戶名" else "Username") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    ) 
                },
                isError = usernameError != null,
                supportingText = {
                    usernameError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (usernameError != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 密碼輸入
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    if (passwordError != null) validatePassword()
                    // 當密碼更改時，重新驗證確認密碼
                    if (confirmPassword.isNotBlank()) validateConfirmPassword()
                },
                label = { Text(if (isChineseLanguage) "密碼" else "Password") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    ) 
                },
                trailingIcon = {
                    if (passwordError != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    } else {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 確認密碼輸入
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    if (confirmPasswordError != null) validateConfirmPassword()
                },
                label = { Text(if (isChineseLanguage) "確認密碼" else "Confirm Password") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    ) 
                },
                trailingIcon = {
                    if (confirmPasswordError != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    } else {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = {
                    confirmPasswordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 電子郵件輸入（可選）
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (emailError != null) validateEmail()
                },
                label = { Text(if (isChineseLanguage) "電子郵件（可選）" else "Email (Optional)") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    ) 
                },
                isError = emailError != null,
                supportingText = {
                    emailError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    if (emailError != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        keyboardController?.hide()
                        handleRegister()
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // 註冊按鈕
            Button(
                onClick = { 
                    keyboardController?.hide()
                    handleRegister() 
                },
                enabled = !isRegistering,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isChineseLanguage) "註冊" else "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 顯示成功消息
            if (registrationSuccess) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (isChineseLanguage) "註冊成功！" else "Registration successful!",
                        color = Color.Green,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 顯示錯誤消息
            if (registrationError != null) {
                Text(
                    text = registrationError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
            
            // 返回登錄頁面的選項
            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = if (isChineseLanguage) "已有帳戶？返回登錄" else "Already have an account? Back to Login",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
} 