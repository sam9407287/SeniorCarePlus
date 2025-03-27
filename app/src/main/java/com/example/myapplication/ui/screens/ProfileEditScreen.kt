package com.example.myapplication.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.auth.UserManager
import com.example.myapplication.models.UserProfile
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(navController: NavController) {
    val context = LocalContext.current
    
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 從 UserManager 獲取當前登入用戶的資料
    val currentUserProfile = UserManager.getCurrentUserProfile()
    
    // 如果沒有登入用戶，返回上一頁
    if (currentUserProfile == null) {
        navController.popBackStack()
        return
    }
    
    // 記錄編輯中的個人資料
    var username by remember { mutableStateOf(currentUserProfile.username) }
    var chineseName by remember { mutableStateOf(currentUserProfile.chineseName ?: "") }
    var englishName by remember { mutableStateOf(currentUserProfile.englishName ?: "") }
    var email by remember { mutableStateOf(currentUserProfile.email ?: "") }
    var birthday by remember { mutableStateOf(currentUserProfile.birthday ?: "") }
    var gender by remember { mutableStateOf(currentUserProfile.gender) }
    var phoneNumber by remember { mutableStateOf(currentUserProfile.phoneNumber ?: "") }
    var address by remember { mutableStateOf(currentUserProfile.address ?: "") }
    var accountType by remember { mutableStateOf(currentUserProfile.accountType) }
    
    // 控制性別選擇下拉列表的顯示
    var showGenderDropdown by remember { mutableStateOf(false) }
    // 控制日期選擇器的顯示
    var showDatePicker by remember { mutableStateOf(false) }
    // 顯示帳號類型說明的對話框
    var showAccountTypeInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isChineseLanguage) "編輯個人資料" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = if (isChineseLanguage) "返回" else "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 用戶頭像區域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "用戶頭像",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 用戶名 (只顯示，不可編輯)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isChineseLanguage) "用戶名" else "Username",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = username,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // 中文姓名
            OutlinedTextField(
                value = chineseName,
                onValueChange = { chineseName = it },
                label = { Text(if (isChineseLanguage) "中文姓名" else "Chinese Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 英文姓名
            OutlinedTextField(
                value = englishName,
                onValueChange = { englishName = it },
                label = { Text(if (isChineseLanguage) "英文姓名" else "English Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 帳號類型 (只顯示，一般不可變更)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showAccountTypeInfo = true },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isChineseLanguage) "帳號類型" else "Account Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = if (isChineseLanguage) "帳號類型說明" else "Account Type Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = UserManager.getAccountTypeName(accountType, isChineseLanguage),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // 電子郵件
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (isChineseLanguage) "電子郵件" else "Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 生日選擇
            OutlinedTextField(
                value = birthday,
                onValueChange = { /* 透過日期選擇器變更 */ },
                label = { Text(if (isChineseLanguage) "生日" else "Birthday") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showDatePicker = true },
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = if (isChineseLanguage) "選擇日期" else "Select Date",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                }
            )
            
            // 性別選擇
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = UserManager.getGenderName(gender, isChineseLanguage),
                    onValueChange = { /* 透過下拉選單變更 */ },
                    label = { Text(if (isChineseLanguage) "性別" else "Gender") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = if (isChineseLanguage) "選擇性別" else "Select Gender",
                            modifier = Modifier.clickable { showGenderDropdown = true }
                        )
                    }
                )
                
                // 性別下拉選單
                DropdownMenu(
                    expanded = showGenderDropdown,
                    onDismissRequest = { showGenderDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isChineseLanguage) "未設定" else "Unspecified") },
                        onClick = {
                            gender = UserManager.GENDER_UNSPECIFIED
                            showGenderDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isChineseLanguage) "男" else "Male") },
                        onClick = {
                            gender = UserManager.GENDER_MALE
                            showGenderDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isChineseLanguage) "女" else "Female") },
                        onClick = {
                            gender = UserManager.GENDER_FEMALE
                            showGenderDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isChineseLanguage) "其他" else "Other") },
                        onClick = {
                            gender = UserManager.GENDER_OTHER
                            showGenderDropdown = false
                        }
                    )
                }
            }
            
            // 聯絡電話
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(if (isChineseLanguage) "聯絡電話" else "Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 地址
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(if (isChineseLanguage) "地址" else "Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // 儲存按鈕
            Button(
                onClick = {
                    // 建立更新後的個人資料
                    val updatedProfile = UserProfile(
                        username = username,
                        chineseName = chineseName.ifEmpty { null },
                        englishName = englishName.ifEmpty { null },
                        email = email.ifEmpty { null },
                        birthday = birthday.ifEmpty { null },
                        gender = gender,
                        phoneNumber = phoneNumber.ifEmpty { null },
                        address = address.ifEmpty { null },
                        accountType = accountType // 帳號類型不可隨意更改
                    )
                    
                    // 更新個人資料
                    val result = UserManager.updateUserProfile(updatedProfile)
                    
                    if (result) {
                        Toast.makeText(
                            context, 
                            if (isChineseLanguage) "個人資料更新成功" else "Profile updated successfully", 
                            Toast.LENGTH_SHORT
                        ).show()
                        // 返回個人資料頁面
                        navController.popBackStack()
                    } else {
                        Toast.makeText(
                            context, 
                            if (isChineseLanguage) "個人資料更新失敗" else "Failed to update profile", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(if (isChineseLanguage) "儲存變更" else "Save Changes")
            }
        }
        
        // 日期選擇器對話框
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            
            // 如果已有生日日期，則設定為日期選擇器的初始值
            if (birthday.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = dateFormat.parse(birthday)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    // 日期格式錯誤，使用當前日期
                }
            }
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            // 根據當前語言建立不同的日期選擇器
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                // 格式化選擇的日期為 yyyy-MM-dd
                birthday = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                showDatePicker = false
            }
            
            val datePickerDialog = if (isChineseLanguage) {
                DatePickerDialog(context,
                    dateSetListener, year, month, day)
            } else {
                // 英文版的日期選擇器
                val dialog = DatePickerDialog(context, dateSetListener, year, month, day)
                dialog.setTitle("Select Birthday")
                dialog
            }
            
            // 設定最大日期為今天（不能選擇未來的日期）
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            
            datePickerDialog.show()
        }
        
        // 帳號類型說明對話框
        if (showAccountTypeInfo) {
            AlertDialog(
                onDismissRequest = { showAccountTypeInfo = false },
                title = { Text(if (isChineseLanguage) "帳號類型" else "Account Type") },
                text = {
                    Column {
                        Text(
                            if (isChineseLanguage)
                                "目前帳號類型為：${UserManager.getAccountTypeName(accountType, isChineseLanguage)}"
                            else
                                "Current account type: ${UserManager.getAccountTypeName(accountType, isChineseLanguage)}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isChineseLanguage) "帳號類型說明：" else "Account Type Description:")
                        if (isChineseLanguage) {
                            Text("• 院友：機構內住民")
                            Text("• 家屬：住民的家人或親友")
                            Text("• 員工：機構工作人員")
                            Text("• 管理人員：系統管理者")
                            Text("• 開發人員：系統開發者")
                        } else {
                            Text("• Resident: Person living in the facility")
                            Text("• Family: Family member or relative of a resident")
                            Text("• Staff: Facility staff member")
                            Text("• Administrator: System administrator")
                            Text("• Developer: System developer")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (isChineseLanguage)
                                "注意：帳號類型變更需要特殊權限，無法在此變更。如需變更帳號類型，請聯絡系統管理員。"
                            else
                                "Note: Changing account type requires special privileges and cannot be changed here. Please contact a system administrator if you need to change your account type.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showAccountTypeInfo = false }) {
                        Text(if (isChineseLanguage) "了解" else "OK")
                    }
                }
            )
        }
    }
}
