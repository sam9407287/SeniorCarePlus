package com.seniorcareplus.app.ui.screens

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
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.ThemeManager

// 院友健康状态枚举
enum class ResidentHealthStatus {
    GOOD,     // 良好
    ATTENTION,// 需注意
    CRITICAL  // 危急
}

// 院友数据类
data class Resident(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val age: Int,
    val roomNumber: String,
    val genderZh: String,
    val genderEn: String,
    val healthStatus: ResidentHealthStatus,
    val emergencyContactZh: String,
    val emergencyContactEn: String,
    val phoneNumber: String,
    val careNoteZh: String,
    val careNoteEn: String
) {
    // 获取当前语言下的名称
    fun getName(isChinese: Boolean): String = if (isChinese) nameZh else nameEn
    
    // 获取当前语言下的性别
    fun getGender(isChinese: Boolean): String = if (isChinese) genderZh else genderEn
    
    // 获取当前语言下的紧急联系人
    fun getEmergencyContact(isChinese: Boolean): String = if (isChinese) emergencyContactZh else emergencyContactEn
    
    // 获取当前语言下的照护注意事项
    fun getCareNote(isChinese: Boolean): String = if (isChinese) careNoteZh else careNoteEn
    
    // 获取搜索用的所有文本
    fun getSearchableText(): String {
        return "$nameZh $nameEn $id $roomNumber $emergencyContactZh $emergencyContactEn $phoneNumber"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentManagementScreen(navController: NavController) {
    // 獲取當前語言和主題狀態
    val isChineseLanguage = LanguageManager.isChineseLanguage
    val isDarkTheme = ThemeManager.isDarkTheme
    
    // 查詢關鍵詞狀態
    var searchQuery by remember { mutableStateOf("") }
    
    // 選中的院友狀態
    var selectedResident by remember { mutableStateOf<Resident?>(null) }
    
    // 查看院友詳情對話框狀態
    var showDetailsDialog by remember { mutableStateOf(false) }
    
    // 編輯照護注意事項狀態
    var editedCareNote by remember { mutableStateOf("") }
    
    // 院友列表數據
    val residentList = remember {
        mutableStateListOf(
            Resident(
                id = "R001",
                nameZh = "王大明",
                nameEn = "Wang Daming",
                age = 78,
                roomNumber = "201",
                genderZh = "男",
                genderEn = "Male",
                healthStatus = ResidentHealthStatus.GOOD,
                emergencyContactZh = "王小明 (兒子)",
                emergencyContactEn = "Wang Xiaoming (Son)",
                phoneNumber = "0912-345-678",
                careNoteZh = "有輕微高血壓，每日需測量血壓兩次。喜歡散步。",
                careNoteEn = "Has mild hypertension, needs blood pressure measurement twice daily. Enjoys walking."
            ),
            Resident(
                id = "R002",
                nameZh = "李小華",
                nameEn = "Li Xiaohua",
                age = 85,
                roomNumber = "202",
                genderZh = "女",
                genderEn = "Female",
                healthStatus = ResidentHealthStatus.ATTENTION,
                emergencyContactZh = "李大為 (女婿)",
                emergencyContactEn = "Li Dawei (Son-in-law)",
                phoneNumber = "0923-456-789",
                careNoteZh = "糖尿病患者，需要監控血糖值。行動緩慢需要協助。喜歡聽收音機。",
                careNoteEn = "Diabetic, needs blood sugar monitoring. Moves slowly and needs assistance. Enjoys listening to radio."
            ),
            Resident(
                id = "R003",
                nameZh = "張小鳳",
                nameEn = "Zhang Xiaofeng",
                age = 80,
                roomNumber = "203",
                genderZh = "女",
                genderEn = "Female",
                healthStatus = ResidentHealthStatus.GOOD,
                emergencyContactZh = "張天明 (兒子)",
                emergencyContactEn = "Zhang Tianming (Son)",
                phoneNumber = "0934-567-890",
                careNoteZh = "聽力稍弱，說話需放慢速度並稍微提高音量。喜歡下棋和園藝活動。",
                careNoteEn = "Slightly hard of hearing, speak slowly and slightly louder. Enjoys chess and gardening activities."
            ),
            Resident(
                id = "R004",
                nameZh = "孫大龍",
                nameEn = "Sun Dalong",
                age = 90,
                roomNumber = "204",
                genderZh = "男",
                genderEn = "Male",
                healthStatus = ResidentHealthStatus.CRITICAL,
                emergencyContactZh = "孫小龍 (孫子)",
                emergencyContactEn = "Sun Xiaolong (Grandson)",
                phoneNumber = "0945-678-901",
                careNoteZh = "心臟病史，需密切觀察。每日需服藥三次，進食需要協助。喜歡聽古典音樂。",
                careNoteEn = "History of heart disease, needs close monitoring. Requires medication three times daily, needs assistance with meals. Enjoys classical music."
            ),
            Resident(
                id = "R005",
                nameZh = "趙一一",
                nameEn = "Zhao Yiyi",
                age = 75,
                roomNumber = "205",
                genderZh = "女",
                genderEn = "Female",
                healthStatus = ResidentHealthStatus.GOOD,
                emergencyContactZh = "趙文明 (兒子)",
                emergencyContactEn = "Zhao Wenming (Son)",
                phoneNumber = "0956-789-012",
                careNoteZh = "行動自如，較為獨立。有輕微關節炎，冬季需多注意保暖。喜歡繪畫和閱讀。",
                careNoteEn = "Mobile and relatively independent. Has mild arthritis, needs to stay warm in winter. Enjoys painting and reading."
            ),
            Resident(
                id = "R006",
                nameZh = "陳大山",
                nameEn = "Chen Dashan",
                age = 82,
                roomNumber = "206",
                genderZh = "男",
                genderEn = "Male",
                healthStatus = ResidentHealthStatus.ATTENTION,
                emergencyContactZh = "陳小山 (兒子)",
                emergencyContactEn = "Chen Xiaoshan (Son)",
                phoneNumber = "0967-890-123",
                careNoteZh = "記憶力減退，需要定期提醒服藥和日常活動。喜歡看電視和下棋。",
                careNoteEn = "Memory decline, needs regular reminders for medication and daily activities. Enjoys watching TV and playing chess."
            )
        )
    }
    
    // 更新篩選院友列表邏輯
    val filteredResidents = remember(searchQuery, residentList) {
        if (searchQuery.isBlank()) {
            residentList
        } else {
            residentList.filter { resident -> 
                // 双语搜索：搜索所有文本，包括中英文字段
                val matchAllText = resident.getSearchableText().contains(searchQuery, ignoreCase = true)
                
                // 特殊关键词搜索
                val healthyKeywords = listOf("良好", "健康", "good", "healthy")
                val attentionKeywords = listOf("注意", "attention", "needs attention", "需注意")
                val criticalKeywords = listOf("危急", "嚴重", "严重", "critical", "serious")
                
                val matchHealthyKeyword = resident.healthStatus == ResidentHealthStatus.GOOD && 
                    healthyKeywords.any { keyword -> searchQuery.contains(keyword, ignoreCase = true) }
                
                val matchAttentionKeyword = resident.healthStatus == ResidentHealthStatus.ATTENTION && 
                    attentionKeywords.any { keyword -> searchQuery.contains(keyword, ignoreCase = true) }
                
                val matchCriticalKeyword = resident.healthStatus == ResidentHealthStatus.CRITICAL && 
                    criticalKeywords.any { keyword -> searchQuery.contains(keyword, ignoreCase = true) }
                
                matchAllText || matchHealthyKeyword || matchAttentionKeyword || matchCriticalKeyword
            }
        }
    }
    
    // 更新照護注意事項
    fun updateCareNote() {
        selectedResident?.let { resident ->
            // 更新院友的照護注意事項
            val index = residentList.indexOf(resident)
            if (index != -1 && editedCareNote.isNotBlank()) {
                if (isChineseLanguage) {
                    residentList[index] = resident.copy(careNoteZh = editedCareNote)
                } else {
                    residentList[index] = resident.copy(careNoteEn = editedCareNote)
                }
                // 隱藏詳情對話框
                showDetailsDialog = false
                // 重置輸入
                editedCareNote = ""
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
            // 顶部栏和返回按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 移除返回按钮
                
                Text(
                    text = if (isChineseLanguage) "院友管理" else "Resident Management",
                    fontSize = if (isChineseLanguage) 30.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // 深色模式切换按钮
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
                        text = if (isChineseLanguage) "搜索院友姓名、編號或房間號..." else "Search by resident name, ID or room number...", 
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
            
            // 院友健康狀態過濾器
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
                    selected = searchQuery.equals("良好", ignoreCase = true) || 
                             searchQuery.equals("健康", ignoreCase = true) || 
                             searchQuery.equals("good", ignoreCase = true) ||
                             searchQuery.equals("healthy", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "良好" else "good" },
                    label = { Text(if (isChineseLanguage) "良好" else "Good", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
                
                FilterChip(
                    selected = searchQuery.equals("注意", ignoreCase = true) || 
                             searchQuery.equals("needs attention", ignoreCase = true) ||
                             searchQuery.equals("attention", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "注意" else "attention" },
                    label = { Text(if (isChineseLanguage) "注意" else "Attention", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
                
                FilterChip(
                    selected = searchQuery.equals("危急", ignoreCase = true) || 
                             searchQuery.equals("嚴重", ignoreCase = true) || 
                             searchQuery.equals("严重", ignoreCase = true) || 
                             searchQuery.equals("critical", ignoreCase = true),
                    onClick = { searchQuery = if (isChineseLanguage) "危急" else "critical" },
                    label = { Text(if (isChineseLanguage) "危急" else "Critical", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
            }
            
            // 院友列表標題
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isChineseLanguage) "院友列表" else "Resident List",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = if (isChineseLanguage) "共 ${filteredResidents.size} 位院友" else "${filteredResidents.size} Residents",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 院友列表
            if (filteredResidents.isEmpty()) {
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
                            text = if (isChineseLanguage) "未找到匹配的院友" else "No matching residents found",
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
                    items(filteredResidents) { resident ->
                        ResidentListItem(
                            resident = resident,
                            isChineseLanguage = isChineseLanguage,
                            onClick = {
                                selectedResident = resident
                                showDetailsDialog = true
                                editedCareNote = if (isChineseLanguage) resident.careNoteZh else resident.careNoteEn
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 院友詳情對話框
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
                        text = if (isChineseLanguage) "院友詳情" else "Resident Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    selectedResident?.let { resident ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 健康狀態標籤
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (resident.healthStatus) {
                                            ResidentHealthStatus.GOOD -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            ResidentHealthStatus.ATTENTION -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                            ResidentHealthStatus.CRITICAL -> Color(0xFFE53935).copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (resident.healthStatus) {
                                        ResidentHealthStatus.GOOD -> Icons.Default.Favorite
                                        ResidentHealthStatus.ATTENTION -> Icons.Default.Warning
                                        ResidentHealthStatus.CRITICAL -> Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = when (resident.healthStatus) {
                                        ResidentHealthStatus.GOOD -> Color(0xFF4CAF50)
                                        ResidentHealthStatus.ATTENTION -> Color(0xFFFF9800)
                                        ResidentHealthStatus.CRITICAL -> Color(0xFFE53935)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = resident.getName(isChineseLanguage),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = if (isChineseLanguage) 
                                        "${resident.age} 歲, ${resident.getGender(isChineseLanguage)}, 房間 ${resident.roomNumber}" 
                                    else 
                                        "${resident.age} years, ${resident.getGender(isChineseLanguage)}, Room ${resident.roomNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // 緊急聯絡人
                        Text(
                            text = if (isChineseLanguage) "緊急聯絡人" else "Emergency Contact",
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
                                Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "${resident.getEmergencyContact(isChineseLanguage)}: ${resident.phoneNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 照護注意事項
                        Text(
                            text = if (isChineseLanguage) "照護注意事項" else "Care Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OutlinedTextField(
                            value = editedCareNote,
                            onValueChange = { editedCareNote = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .height(120.dp),
                            placeholder = { 
                                Text(
                                    if (isChineseLanguage) "輸入照護注意事項..." else "Enter care notes..."
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
                            onClick = { updateCareNote() },
                            enabled = editedCareNote.isNotBlank()
                        ) {
                            Text(if (isChineseLanguage) "更新照護注意事項" else "Update Care Notes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentListItem(
    resident: Resident,
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
            // 健康狀態圖標
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (resident.healthStatus) {
                            ResidentHealthStatus.GOOD -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            ResidentHealthStatus.ATTENTION -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            ResidentHealthStatus.CRITICAL -> Color(0xFFE53935).copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (resident.healthStatus) {
                        ResidentHealthStatus.GOOD -> Icons.Default.Favorite
                        ResidentHealthStatus.ATTENTION -> Icons.Default.Warning
                        ResidentHealthStatus.CRITICAL -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (resident.healthStatus) {
                        ResidentHealthStatus.GOOD -> Color(0xFF4CAF50)
                        ResidentHealthStatus.ATTENTION -> Color(0xFFFF9800)
                        ResidentHealthStatus.CRITICAL -> Color(0xFFE53935)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 院友信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = resident.getName(isChineseLanguage),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChineseLanguage) "編號: ${resident.id}" else "ID: ${resident.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 健康狀態標籤
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (resident.healthStatus) {
                                    ResidentHealthStatus.GOOD -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    ResidentHealthStatus.ATTENTION -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                    ResidentHealthStatus.CRITICAL -> Color(0xFFE53935).copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (resident.healthStatus) {
                                ResidentHealthStatus.GOOD -> if (isChineseLanguage) "良好" else "Good"
                                ResidentHealthStatus.ATTENTION -> if (isChineseLanguage) "需注意" else "Attention"
                                ResidentHealthStatus.CRITICAL -> if (isChineseLanguage) "危急" else "Critical"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (resident.healthStatus) {
                                ResidentHealthStatus.GOOD -> Color(0xFF4CAF50)
                                ResidentHealthStatus.ATTENTION -> Color(0xFFFF9800)
                                ResidentHealthStatus.CRITICAL -> Color(0xFFE53935)
                            },
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isChineseLanguage) "房間: ${resident.roomNumber}, ${resident.age} 歲" else "Room: ${resident.roomNumber}, ${resident.age} years",
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