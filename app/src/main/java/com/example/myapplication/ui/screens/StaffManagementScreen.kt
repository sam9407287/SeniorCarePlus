package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.LanguageManager
import com.example.myapplication.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

// 員工狀態枚舉
enum class StaffStatus {
    ACTIVE,      // 在職
    ON_LEAVE     // 休假
}

// 員工數據類
data class Staff(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val positionZh: String,
    val positionEn: String,
    val departmentZh: String,
    val departmentEn: String,
    val phoneNumber: String,
    val email: String,
    val status: StaffStatus,
    val hireDate: Date,
    val notesZh: String,
    val notesEn: String
) {
    // 獲取當前語言下的名稱
    fun getName(isChinese: Boolean): String = if (isChinese) nameZh else nameEn
    
    // 獲取當前語言下的職位
    fun getPosition(isChinese: Boolean): String = if (isChinese) positionZh else positionEn
    
    // 獲取當前語言下的部門
    fun getDepartment(isChinese: Boolean): String = if (isChinese) departmentZh else departmentEn
    
    // 獲取當前語言下的備註
    fun getNotes(isChinese: Boolean): String = if (isChinese) notesZh else notesEn
    
    // 獲取格式化的入職日期
    fun getFormattedHireDate(isChinese: Boolean): String {
        val dateFormat = if (isChinese) 
            SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        else 
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(hireDate)
    }
    
    // 獲取搜索用的所有文本
    fun getSearchableText(): String {
        return "$nameZh $nameEn $id $positionZh $positionEn $departmentZh $departmentEn $phoneNumber $email"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 查詢關鍵詞狀態
    var searchQuery by remember { mutableStateOf("") }
    
    // 選中的員工狀態
    var selectedStaff by remember { mutableStateOf<Staff?>(null) }
    
    // 查看員工詳情對話框狀態
    var showDetailsDialog by remember { mutableStateOf(false) }
    
    // 編輯備註狀態
    var editedNotes by remember { mutableStateOf("") }
    
    // 創建日期
    val createDate = { year: Int, month: Int, day: Int ->
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        calendar.time
    }
    
    // 員工列表數據
    val staffList = remember {
        mutableStateListOf(
            Staff(
                id = "S001",
                nameZh = "張明德",
                nameEn = "Zhang Mingde",
                positionZh = "護理主任",
                positionEn = "Head Nurse",
                departmentZh = "護理部",
                departmentEn = "Nursing Department",
                phoneNumber = "0912-345-678",
                email = "zhang.mingde@careplus.com",
                status = StaffStatus.ACTIVE,
                hireDate = createDate(2018, 5, 15),
                notesZh = "負責所有護理人員的管理和排班工作。擁有10年護理經驗。",
                notesEn = "Responsible for managing all nursing staff and scheduling. Has 10 years of nursing experience."
            ),
            Staff(
                id = "S002",
                nameZh = "李小梅",
                nameEn = "Li Xiaomei",
                positionZh = "護士",
                positionEn = "Nurse",
                departmentZh = "護理部",
                departmentEn = "Nursing Department",
                phoneNumber = "0923-456-789",
                email = "li.xiaomei@careplus.com",
                status = StaffStatus.ON_LEAVE,
                hireDate = createDate(2020, 8, 10),
                notesZh = "專責第二樓患者的照護工作。目前休產假，預計三個月後返回工作崗位。",
                notesEn = "Responsible for patient care on the 2nd floor. Currently on maternity leave, expected to return in three months."
            ),
            Staff(
                id = "S003",
                nameZh = "王建國",
                nameEn = "Wang Jianguo",
                positionZh = "醫生",
                positionEn = "Doctor",
                departmentZh = "醫療部",
                departmentEn = "Medical Department",
                phoneNumber = "0934-567-890",
                email = "wang.jianguo@careplus.com",
                status = StaffStatus.ACTIVE,
                hireDate = createDate(2017, 3, 20),
                notesZh = "專科醫師，負責患者的常規檢查和健康評估。每週一、三、五出診。",
                notesEn = "Specialist physician, responsible for routine examinations and health assessments. Visits patients on Mondays, Wednesdays, and Fridays."
            ),
            Staff(
                id = "S004",
                nameZh = "林美華",
                nameEn = "Lin Meihua",
                positionZh = "行政經理",
                positionEn = "Administrative Manager",
                departmentZh = "行政部",
                departmentEn = "Administration Department",
                phoneNumber = "0945-678-901",
                email = "lin.meihua@careplus.com",
                status = StaffStatus.ACTIVE,
                hireDate = createDate(2019, 10, 5),
                notesZh = "負責機構的日常行政運作和資源調配。擅長人力資源管理。",
                notesEn = "Responsible for daily administrative operations and resource allocation. Specializes in human resource management."
            ),
            Staff(
                id = "S005",
                nameZh = "陳偉誠",
                nameEn = "Chen Weicheng",
                positionZh = "照護員",
                positionEn = "Caregiver",
                departmentZh = "護理部",
                departmentEn = "Nursing Department",
                phoneNumber = "0956-789-012",
                email = "chen.weicheng@careplus.com",
                status = StaffStatus.ACTIVE,
                hireDate = createDate(2021, 2, 15),
                notesZh = "負責患者的日常生活照顧。工作認真負責，深受患者喜愛。",
                notesEn = "Responsible for patients' daily care. Diligent and responsible, well-liked by patients."
            ),
            Staff(
                id = "S006",
                nameZh = "趙小龍",
                nameEn = "Zhao Xiaolong",
                positionZh = "社工",
                positionEn = "Social Worker",
                departmentZh = "社工部",
                departmentEn = "Social Work Department",
                phoneNumber = "0967-890-123",
                email = "zhao.xiaolong@careplus.com",
                status = StaffStatus.ON_LEAVE,
                hireDate = createDate(2020, 5, 10),
                notesZh = "負責患者的心理輔導和社交活動。目前休假中，預計一個月後返回。",
                notesEn = "Responsible for patients' psychological counseling and social activities. Currently on leave, expected to return in one month."
            )
        )
    }
    
    // 更新篩選員工列表邏輯
    val filteredStaff = remember(searchQuery, staffList) {
        if (searchQuery.isBlank()) {
            staffList
        } else {
            staffList.filter { staff -> 
                // 雙語搜索：搜索所有文本，包括中英文字段
                val matchAllText = staff.getSearchableText().contains(searchQuery, ignoreCase = true)
                
                // 特殊關鍵詞搜索
                val activeKeywords = listOf("在職", "active", "正常")
                val onLeaveKeywords = listOf("休假", "leave", "請假", "on leave")
                
                val matchActiveKeyword = staff.status == StaffStatus.ACTIVE && 
                    activeKeywords.any { keyword -> searchQuery.contains(keyword, ignoreCase = true) }
                
                val matchOnLeaveKeyword = staff.status == StaffStatus.ON_LEAVE && 
                    onLeaveKeywords.any { keyword -> searchQuery.contains(keyword, ignoreCase = true) }
                
                matchAllText || matchActiveKeyword || matchOnLeaveKeyword
            }
        }
    }
    
    // 更新備註
    fun updateNotes() {
        selectedStaff?.let { staff ->
            // 更新員工的備註
            val index = staffList.indexOf(staff)
            if (index != -1 && editedNotes.isNotBlank()) {
                if (isChineseLanguage) {
                    staffList[index] = staff.copy(notesZh = editedNotes)
                } else {
                    staffList[index] = staff.copy(notesEn = editedNotes)
                }
                // 隱藏詳情對話框
                showDetailsDialog = false
                // 重置輸入
                editedNotes = ""
            }
        }
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
        ) {
            // 頂部欄
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "員工管理" else "Staff Management",
                    fontSize = if (isChineseLanguage) 30.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // 深色模式切換按鈕
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
            
            // 搜索欄
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { 
                    Text(
                        text = if (isChineseLanguage) "搜索員工姓名、編號或部門..." else "Search by staff name, ID or department...", 
                        fontSize = if (isChineseLanguage) MaterialTheme.typography.bodyMedium.fontSize else 13.sp,
                        maxLines = 1
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = if (isChineseLanguage) "清除" else "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { /* 處理搜索 */ }
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            
            // 員工狀態過濾器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = searchQuery.isEmpty(),
                    onClick = { searchQuery = "" },
                    label = { Text(if (isChineseLanguage) "全部" else "All", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
                
                FilterChip(
                    selected = searchQuery.equals("在職", ignoreCase = true) || 
                             searchQuery.equals("active", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "在職" else "active" },
                    label = { Text(if (isChineseLanguage) "在職" else "Active", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
                
                FilterChip(
                    selected = searchQuery.equals("休假", ignoreCase = true) || 
                             searchQuery.equals("leave", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "休假" else "leave" },
                    label = { Text(if (isChineseLanguage) "休假" else "On Leave", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
            }
            
            // 員工列表標題
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "員工列表" else "Staff List",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = if (isChineseLanguage) "共 ${filteredStaff.size} 位員工" else "${filteredStaff.size} Staff",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 員工列表
            if (filteredStaff.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (isChineseLanguage) "未找到匹配的員工" else "No matching staff found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredStaff) { staff ->
                        StaffListItem(
                            staff = staff,
                            isChineseLanguage = isChineseLanguage,
                            onClick = {
                                selectedStaff = staff
                                showDetailsDialog = true
                                editedNotes = if (isChineseLanguage) staff.notesZh else staff.notesEn
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 員工詳情對話框
    if (showDetailsDialog) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isChineseLanguage) "員工詳情" else "Staff Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    selectedStaff?.let { staff ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 員工狀態標籤
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (staff.status) {
                                            StaffStatus.ACTIVE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            StaffStatus.ON_LEAVE -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (staff.status) {
                                        StaffStatus.ACTIVE -> Icons.Default.Check
                                        StaffStatus.ON_LEAVE -> Icons.Default.Home
                                    },
                                    contentDescription = null,
                                    tint = when (staff.status) {
                                        StaffStatus.ACTIVE -> Color(0xFF4CAF50)
                                        StaffStatus.ON_LEAVE -> Color(0xFFFF9800)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = staff.getName(isChineseLanguage),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = if (isChineseLanguage) 
                                        "${staff.getPosition(isChineseLanguage)}, ${staff.getDepartment(isChineseLanguage)}" 
                                    else 
                                        "${staff.getPosition(isChineseLanguage)}, ${staff.getDepartment(isChineseLanguage)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // 聯繫方式
                        Text(
                            text = if (isChineseLanguage) "聯繫方式" else "Contact Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = staff.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = staff.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 工作信息
                        Text(
                            text = if (isChineseLanguage) "工作信息" else "Employment Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = if (isChineseLanguage) 
                                    "入職日期: ${staff.getFormattedHireDate(isChineseLanguage)}" 
                                else 
                                    "Hire Date: ${staff.getFormattedHireDate(isChineseLanguage)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 備註信息
                        Text(
                            text = if (isChineseLanguage) "備註信息" else "Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OutlinedTextField(
                            value = editedNotes,
                            onValueChange = { editedNotes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .height(120.dp),
                            placeholder = { 
                                Text(
                                    if (isChineseLanguage) "輸入備註信息..." else "Enter notes..."
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDetailsDialog = false }
                        ) {
                            Text(if (isChineseLanguage) "取消" else "Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { updateNotes() },
                            enabled = editedNotes.isNotBlank()
                        ) {
                            Text(if (isChineseLanguage) "更新備註" else "Update Notes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffListItem(
    staff: Staff,
    isChineseLanguage: Boolean,
    onClick: () -> Unit
) {
    val isDarkTheme = ThemeManager.isDarkTheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) 
                            else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 員工狀態圖標
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (staff.status) {
                            StaffStatus.ACTIVE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            StaffStatus.ON_LEAVE -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (staff.status) {
                        StaffStatus.ACTIVE -> Icons.Default.Check
                        StaffStatus.ON_LEAVE -> Icons.Default.Home
                    },
                    contentDescription = null,
                    tint = when (staff.status) {
                        StaffStatus.ACTIVE -> Color(0xFF4CAF50)
                        StaffStatus.ON_LEAVE -> Color(0xFFFF9800)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 員工信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = staff.getName(isChineseLanguage),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChineseLanguage) "編號: ${staff.id}" else "ID: ${staff.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 員工狀態標籤
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (staff.status) {
                                    StaffStatus.ACTIVE -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    StaffStatus.ON_LEAVE -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (staff.status) {
                                StaffStatus.ACTIVE -> if (isChineseLanguage) "在職" else "Active"
                                StaffStatus.ON_LEAVE -> if (isChineseLanguage) "休假" else "On Leave"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (staff.status) {
                                StaffStatus.ACTIVE -> Color(0xFF4CAF50)
                                StaffStatus.ON_LEAVE -> Color(0xFFFF9800)
                            },
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isChineseLanguage) 
                        "${staff.getPosition(isChineseLanguage)}, ${staff.getDepartment(isChineseLanguage)}" 
                    else 
                        "${staff.getPosition(isChineseLanguage)}, ${staff.getDepartment(isChineseLanguage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 查看詳情按鈕
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = if (isChineseLanguage) "查看詳情" else "View Details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 