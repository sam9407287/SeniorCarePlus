@file:Suppress("UNUSED_IMPORT", "NAME_SHADOWING", "UnusedImport")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.seniorcareplus.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.seniorcareplus.app.ui.screens.ButtonScreen
import com.seniorcareplus.app.ui.screens.DiaperScreen
import com.seniorcareplus.app.ui.screens.HeartRateScreen
import com.seniorcareplus.app.ui.screens.HomeScreen
import com.seniorcareplus.app.ui.screens.MapScreen
import com.seniorcareplus.app.ui.screens.MonitorScreen
import com.seniorcareplus.app.ui.screens.RegionScreen
import com.seniorcareplus.app.ui.screens.TemperatureScreen
import com.seniorcareplus.app.ui.screens.TimerScreen
import com.seniorcareplus.app.ui.screens.NotificationScreen
import com.seniorcareplus.app.ui.screens.EmergencyButtonScreen
import com.seniorcareplus.app.ui.screens.DiaperMonitorScreen
import com.seniorcareplus.app.ui.screens.HeartRateMonitorScreen
import com.seniorcareplus.app.ui.screens.TemperatureMonitorScreen
import com.seniorcareplus.app.ui.screens.SettingsScreen
import com.seniorcareplus.app.ui.screens.EquipmentManagementScreen
import com.seniorcareplus.app.ui.theme.SeniorCarePlusTheme
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.ui.theme.LanguageManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorcareplus.app.reminder.ReminderAlertDialog
import com.seniorcareplus.app.reminder.ReminderFullScreenDialog
import com.seniorcareplus.app.ui.screens.ReminderViewModel
import com.seniorcareplus.app.ui.screens.ResidentManagementScreen
import com.seniorcareplus.app.ui.screens.StaffManagementScreen
import com.seniorcareplus.app.ui.screens.AboutUsScreen
import com.seniorcareplus.app.ui.screens.IssueReportScreen
import com.seniorcareplus.app.ui.screens.LoginScreen
import com.seniorcareplus.app.ui.screens.MailboxScreen
import com.seniorcareplus.app.ui.screens.ProfileScreen
import com.seniorcareplus.app.ui.screens.ProfileEditScreen
import com.seniorcareplus.app.ui.screens.RegisterScreen
import com.seniorcareplus.app.ui.screens.ForgotPasswordScreen
import com.seniorcareplus.app.auth.UserManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.seniorcareplus.app.ui.screens.VerificationCodeScreen
import com.seniorcareplus.app.ui.screens.ResetPasswordScreen
import com.seniorcareplus.app.ui.screens.ChangePasswordScreen
import com.seniorcareplus.app.utils.PermissionUtils
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    companion object {
        // 使用可空類型，避免初始化問題
        var sharedReminderViewModel: ReminderViewModel? = null
        
        // 添加可以更新登錄狀態的函數
        var updateLoginState: (() -> Unit)? = null
    }
    
    // 注册通知权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "通知权限已授予")
        } else {
            Log.d("MainActivity", "通知权限被拒绝")
            // 当用户拒绝权限时，提示用户并提供前往设置页面的选项
            val isChineseLanguage = LanguageManager.isChineseLanguage
            val message = if (isChineseLanguage) 
                "通知權限被拒絕，部分功能將無法正常工作。請在應用設置中開啟通知權限。" 
            else 
                "Notification permission was denied. Some features may not work properly. Please enable notifications in app settings."
            val buttonText = if (isChineseLanguage) "前往設置" else "Go to Settings"
            val cancelText = if (isChineseLanguage) "取消" else "Cancel"
            
            // 使用Handler确保在UI线程上显示对话框
            Handler(Looper.getMainLooper()).post {
                android.app.AlertDialog.Builder(this)
                    .setMessage(message)
                    .setPositiveButton(buttonText) { _, _ ->
                        // 打开应用设置页面
                        PermissionUtils.openAppSettings(this)
                    }
                    .setNegativeButton(cancelText, null)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在启动后切换到普通主题
        setTheme(R.style.Theme_SeniorCarePlus)
        
        super.onCreate(savedInstanceState)
        
        // 检查并请求通知权限
        PermissionUtils.checkAndRequestNotificationPermission(this, requestPermissionLauncher)
        
        // 全局共享的 ReminderViewModel
        // 全局只保留一个实例，方便通知显示提醒对话框
        sharedReminderViewModel = ViewModelProvider(this)[ReminderViewModel::class.java]
        
        // 检查是否有从通知打开的提醒
        val openReminderDialog = intent.getBooleanExtra("OPEN_REMINDER_DIALOG", false)
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        
        // 如果从通知打开，直接显示提醒对话框
        if (openReminderDialog && reminderId != -1) {
            Log.d("MainActivity", "启动时有提醒需要显示: ID=$reminderId")
            sharedReminderViewModel?.showReminderAlert(reminderId)
            
            // 标记为已处理
            ProcessedReminders.markAsProcessed(reminderId)
        }
        
        setContent {
            // 使用我們的自定義主題，isDarkTheme會從ThemeManager中獲取
            SeniorCarePlusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 檢查是否有從通知打開的提醒
                    val openReminderDialog = intent.getBooleanExtra("OPEN_REMINDER_DIALOG", false)
                    val reminderId = intent.getIntExtra("REMINDER_ID", -1)
                    
                    Log.d("MainActivity", "onCreate: openReminderDialog=$openReminderDialog, reminderId=$reminderId")
                    
                    // 處理完 intent 後清除這些標記，防止在切換主題時重複觸發
                    if (openReminderDialog && reminderId != -1) {
                        intent.removeExtra("OPEN_REMINDER_DIALOG")
                        intent.removeExtra("REMINDER_ID")
                    }
                    
                    MainAppContent(openReminderDialog, reminderId)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // 設置新的Intent
        setIntent(intent)
        
        // 檢查是否有從通知打開的提醒
        val openReminderDialog = intent.getBooleanExtra("OPEN_REMINDER_DIALOG", false)
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        val timestamp = intent.getLongExtra("TIMESTAMP", 0)
        val fromNotification = intent.getBooleanExtra("FROM_NOTIFICATION", false)
        
        Log.d("MainActivity", "onNewIntent: openReminderDialog=$openReminderDialog, reminderId=$reminderId, timestamp=$timestamp, fromNotification=$fromNotification")
        
        // 如果提醒有效，直接显示，不管是否处理过
        if (openReminderDialog && reminderId != -1 && sharedReminderViewModel != null) {
            // 標記提醒為已處理
            ProcessedReminders.markAsProcessed(reminderId)
            
            // 获取提醒类型（如果有）
            val reminderType = intent.getStringExtra("REMINDER_TYPE")
            Log.d("MainActivity", "onNewIntent 處理提醒: ID=$reminderId, 类型=$reminderType")
            
            // 直接更新 ViewModel 狀態，显示对话框
            if (fromNotification) {
                // 如果是从通知打开的，添加标记使对话框关闭後不跳转到首页
                sharedReminderViewModel?.showReminderAlertWithoutRedirect(reminderId, reminderType)
                Log.d("MainActivity", "是從通知打開的，設置不跳轉到首頁")
            } else {
                sharedReminderViewModel?.showReminderAlert(reminderId)
            }
            
            // 清除 intent 標記，防止重複觸發
            intent.removeExtra("OPEN_REMINDER_DIALOG")
            intent.removeExtra("REMINDER_ID")
            intent.removeExtra("TIMESTAMP")
            intent.removeExtra("FROM_NOTIFICATION")
        }
    }
}

@Composable
fun SeniorCareTopBar(
    onUserIconClick: () -> Unit = {}, 
    onNotificationClick: () -> Unit = {},
    isLoggedIn: Boolean = UserManager.isLoggedIn()  // 添加參數，預設值是當前UserManager的狀態
) {
    // 使用MaterialTheme的顏色而不是硬編碼的顏色
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側用戶圖標
            IconButton(
                onClick = onUserIconClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = if (isChineseLanguage) "用戶菜單" else "User Menu",
                    tint = if (isLoggedIn) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // 居中標題
            Text(
                text = if (isChineseLanguage) "長者照護系統" else "SENIOR CARE PLUS",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 右側通知圖標
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = if (isChineseLanguage) "通知" else "Notifications",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 保存已在應用生命週期內處理過的提醒ID和觸發時間
 */
object ProcessedReminders {
    // 將提醒ID和觸發時間一起存儲，以便區分不同時間的相同提醒
    private val processedReminderMap = mutableMapOf<Int, Long>()
    
    // 非常短的過期時間，只在幾秒內防止重複觸發，帶來更好的用戶體驗
    private const val REMINDER_EXPIRE_DURATION = 5 * 1000L // 5秒鋪蹟數
    
    // 在主題切換後是否重置處理狀態
    private var themeChangeResetEnabled = true
    
    // 註冊主題切換監聽器
    init {
        ThemeManager.addThemeChangeListener {
            if (themeChangeResetEnabled) {
                // 主題切換時重置處理狀態，允許提醒繼續觸發
                Log.d("ProcessedReminders", "主題切換，重置提醒處理狀態")
                resetAll()
            }
        }
    }
    
    /**
     * 檢查提醒是否已被處理且未過期
     * @param reminderId 提醒ID
     * @return 如果已處理且未過期返回 true
     */
    fun isProcessed(reminderId: Int): Boolean {
        val lastProcessedTime = processedReminderMap[reminderId] ?: return false
        val currentTime = System.currentTimeMillis()
        
        // 檢查會跑了多少時間，如果超過了自動過期時間，代表提醒可以再次觸發
        val timePassed = currentTime - lastProcessedTime
        val expired = timePassed > REMINDER_EXPIRE_DURATION
        
        if (expired) {
            // 如果已過期，則移除記錄
            processedReminderMap.remove(reminderId)
            return false
        }
        
        // 如果未過期，則返回已處理
        return true
    }
    
    /**
     * 標記提醒為已處理
     * @param reminderId 要標記的提醒ID
     */
    fun markAsProcessed(reminderId: Int) {
        processedReminderMap[reminderId] = System.currentTimeMillis()
    }
    
    /**
     * 重置所有處理狀態
     */
    /**
     * 重置所有處理狀態
     */
    fun resetAll() {
        processedReminderMap.clear()
        Log.d("ProcessedReminders", "已重置所有提醒處理狀態")
    }
    
    /**
     * 設置主題切換重置功能的啟用狀態
     * @param enabled 是否啟用主題切換重置
     */
    fun setThemeChangeResetEnabled(enabled: Boolean) {
        themeChangeResetEnabled = enabled
        Log.d("ProcessedReminders", "設置主題切換重置功能: $enabled")
    }
    
    /**
     * 清除過期的提醒記錄
     */
    fun clearExpired() {
        val currentTime = System.currentTimeMillis()
        val iterator = processedReminderMap.iterator()
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (currentTime - entry.value > REMINDER_EXPIRE_DURATION) {
                iterator.remove()
            }
        }
    }
}

@Composable
fun MainAppContent(openReminderDialog: Boolean = false, reminderId: Int = -1) {
    val navController = rememberNavController()
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // 管理頁面右側邊欄
    var showRightDrawer by remember { mutableStateOf(false) }
    
    // 檢查語言設置
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 使用MaterialTheme的顏色而不是硬編碼的顏色
    val navigationBarColor = if (ThemeManager.isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    // 獲取提醒 ViewModel
    val localReminderViewModel: ReminderViewModel = viewModel()
    
    // 將局部 ViewModel 存儲在非 Composable 的地方，使其可以在 onNewIntent 中訪問
    MainActivity.sharedReminderViewModel = localReminderViewModel
    
    // 使用 remember 記錄已處理的提醒ID，防止在重組時重複觸發
    val processedReminderRef = remember { mutableStateOf(-1) }
    
    // 跟踪是否顯示帳戶選項對話框
    var showAccountOptionsDialog by remember { mutableStateOf(false) }
    
    // 創建登錄狀態的響應式變量，並添加更新觸發器
    val loginStateKey = remember { mutableStateOf(0) } // 觸發重組的計數器
    val isLoggedIn = remember(loginStateKey.value) { mutableStateOf(UserManager.isLoggedIn()) }
    val currentUsername = remember(loginStateKey.value) { mutableStateOf(UserManager.getCurrentUsername() ?: "") }
    
    // 在組合效果中監聽登錄狀態的變化
    LaunchedEffect(loginStateKey.value) {
        // 每次觸發時重新檢查登錄狀態
        val loggedIn = UserManager.isLoggedIn()
        isLoggedIn.value = loggedIn
        currentUsername.value = UserManager.getCurrentUsername() ?: ""
        
        Log.d("MainActivity", "LaunchedEffect: 已登錄=$loggedIn, 用戶名=${currentUsername.value}")
        
        // 如果未登入，確保帳戶選項對話框被關閉
        if (!loggedIn) {
            showAccountOptionsDialog = false
        }
    }
    
    // 應用啟動時也檢查登錄狀態
    LaunchedEffect(Unit) {
        // 立即更新登錄狀態和用戶名
        isLoggedIn.value = UserManager.isLoggedIn()
        currentUsername.value = UserManager.getCurrentUsername() ?: ""
        
        Log.d("MainActivity", "应用启动: 已登錄=${isLoggedIn.value}, 用戶名=${currentUsername.value}")
    }
    
    // 設置更新登錄狀態的函數
    DisposableEffect(Unit) {
        MainActivity.updateLoginState = {
            // 增加計數器觸發狀態重新計算
            loginStateKey.value += 1
            
            // 立即更新登錄狀態和用戶名
            isLoggedIn.value = UserManager.isLoggedIn()
            currentUsername.value = UserManager.getCurrentUsername() ?: ""
            
            Log.d("MainActivity", "登錄狀態已更新: 已登錄=${isLoggedIn.value}, 用戶名=${currentUsername.value}")
        }
        
        // 使用正確的方式返回清理函數
        onDispose {
            MainActivity.updateLoginState = null
        }
    }
    
    // 如果從通知打開，且該提醒在應用生命週期內沒有被處理過，才顯示提醒對話框
    if (openReminderDialog && reminderId != -1) {
        // 標記為已處理（應用級別標記，不受重新組合影響）
        ProcessedReminders.markAsProcessed(reminderId)
        // 顯示提醒對話框
        Log.d("MainAppContent", "顯示提醒對話框，ID: $reminderId")
        localReminderViewModel.showReminderAlert(reminderId)
    }
    
    // 顯示提醒對話框
    if (localReminderViewModel.showReminderAlert) {
        localReminderViewModel.currentReminder?.let { reminder ->
            ReminderAlertDialog(
                reminder = reminder,
                onDismiss = { localReminderViewModel.hideReminderAlert() },
                onSnooze = { localReminderViewModel.snoozeReminder() }
            )
        }
    }
    
    // 顯示全屏提醒對話框
    val showFullScreenDialog = remember { mutableStateOf(localReminderViewModel.showFullScreenAlert) }
    
    // 監聽 ViewModel 中的狀態變化
    LaunchedEffect(localReminderViewModel.showFullScreenAlert) {
        showFullScreenDialog.value = localReminderViewModel.showFullScreenAlert
    }
    
    if (showFullScreenDialog.value) {
        localReminderViewModel.currentReminder?.let { reminder ->
            ReminderFullScreenDialog(
                reminder = reminder,
                onDismiss = { 
                    // 先更新本地狀態
                    showFullScreenDialog.value = false
                    // 然後通知 ViewModel
                    localReminderViewModel.hideReminderAlert() 
                }
            )
        }
    }
    
    // 如果對話框應該顯示，則顯示帳戶選項對話框
    if (showAccountOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showAccountOptionsDialog = false },
            title = { Text(if (isChineseLanguage) "帳戶選項" else "Account Options") },
            text = {
                Column {
                    Text(
                        if (isChineseLanguage) 
                            "當前帳戶: ${UserManager.getCurrentUsername() ?: ""}" 
                        else 
                            "Current Account: ${UserManager.getCurrentUsername() ?: ""}"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 關閉帳戶選項對話框
                        showAccountOptionsDialog = false
                        
                        // 登出並清除數據
                        UserManager.logout()
                        
                        // 強制更新UI，確保用戶名被清除
                        currentUsername.value = ""
                        
                        // 通知ReminderViewModel更新提醒數據
                        MainActivity.sharedReminderViewModel?.onLoginStateChanged()
                        
                        // 使用狀態更新機制更新登錄狀態
                        MainActivity.updateLoginState?.invoke()
                        
                        // 登出後直接导航到登录页面
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        if (isChineseLanguage) "登出" else "Logout",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // 關閉對話框並導航到登入頁面以切換帳戶
                        showAccountOptionsDialog = false
                        navController.navigate("login") {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(if (isChineseLanguage) "切換帳戶" else "Switch Account")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    // 定義底部導航欄的項目
    val bottomNavItems = listOf(
        BottomNavItem(
            name = if (isChineseLanguage) "主頁" else "Home",
            route = "home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "監控" else "Monitor",
            route = "monitor",
            icon = Icons.AutoMirrored.Filled.ShowChart
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "地圖" else "Map",
            route = "map",
            icon = Icons.Default.Map
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "定時" else "Timer",
            route = "timer",
            icon = Icons.Default.DateRange
        ),
        BottomNavItem(
            name = if (isChineseLanguage) "更多" else "More",
            route = "more",
            icon = Icons.Default.MoreHoriz
        )
    )
    
    // 定義左側邊欄項目
    val leftDrawerItems = listOf(
        DrawerItem(
            if (isChineseLanguage) "個人郵箱" else "Personal Email", 
            if (isChineseLanguage) "登錄您的郵箱賬號" else "Log in to your email account", 
            Icons.Default.Email
        ),
        DrawerItem(
            if (isChineseLanguage) "個人資料" else "Personal Profile", 
            if (isChineseLanguage) "查看和編輯您的資料" else "View and edit your profile", 
            Icons.Default.Person
        ),
        DrawerItem(
            if (isChineseLanguage) {
                if (isLoggedIn.value) "登出帳戶" else "帳戶登入"
            } else {
                if (isLoggedIn.value) "Logout" else "Login"
            }, 
            if (isChineseLanguage) {
                if (isLoggedIn.value) {
                    "目前登入: ${currentUsername.value}"
                } else "帳號登入與登出"
            } else {
                if (isLoggedIn.value) {
                    "Current user: ${currentUsername.value}"
                } else "Account login and logout"
            }, 
            if (isLoggedIn.value) Icons.Default.ExitToApp else Icons.Default.AccountCircle
        ),
        DrawerItem(
            if (isChineseLanguage) "設置" else "Settings", 
            if (isChineseLanguage) "應用程序設置" else "Application settings", 
            Icons.Default.Settings
        ),
        DrawerItem(
            if (isChineseLanguage) "關於我們" else "About Us", 
            if (isChineseLanguage) "了解更多信息" else "Learn more about us", 
            Icons.Default.Info
        )
    )
    
    // 定義右側邊欄項目 - 管理頁面
    val adminItems = listOf(
        AdminItem(
            if (isChineseLanguage) "院友管理" else "Patient Management", 
            "patient_admin", 
            Icons.Default.People
        ),
        AdminItem(
            if (isChineseLanguage) "員工管理" else "Staff Management", 
            "staff_admin", 
            Icons.Default.Work
        ),
        AdminItem(
            if (isChineseLanguage) "設備管理" else "Equipment Management", 
            "equipment_admin", 
            Icons.Default.Build
        ),
        AdminItem(
            if (isChineseLanguage) "問題回報" else "Issue Report", 
            "settings_admin", 
            Icons.Default.Settings
        )
    )
    
    // 構建UI界面
    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        drawerContent = {
            ModalDrawerSheet(
                // 减小左侧侧边栏宽度
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    // 添加安全区域的内边距，避免与系统UI重叠
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 24.dp),
                // 增加圆角
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                // 添加阴影效果
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    if (isChineseLanguage) "長者照護系統" else "SENIOR CARE PLUS",
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(start = 8.dp)  // 添加左边距使文本左对齐
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,  // 更改为titleLarge，比headlineMedium小
                    textAlign = TextAlign.Start  // 修改为左对齐
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                leftDrawerItems.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Column {
                                Text(item.title, style = MaterialTheme.typography.bodyLarge)
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                leftDrawerState.close()
                            }
                            // 處理側邊欄項目點擊
                            if (index == 0) { // 個人郵箱項目的索引
                                // 導航到個人郵箱頁面
                                navController.navigate("mailbox") {
                                    // 防止創建多個實例
                                    launchSingleTop = true
                                }
                            } else if (index == 1) { // 個人資料項目的索引
                                // 導航到個人資料頁面
                                navController.navigate("profile") {
                                    // 防止創建多個實例
                                    launchSingleTop = true
                                }
                            } else if (index == 2) { // 登入/登出項目的索引
                                // 確保每次點擊時都取得最新的登錄狀態
                                val currentlyLoggedIn = UserManager.isLoggedIn()
                                
                                if (currentlyLoggedIn) {
                                    // 如果確實已登入，才顯示帳戶選項
                                    showAccountOptionsDialog = true
                                } else {
                                    // 強制更新狀態，確保 UI 同步
                                    MainActivity.updateLoginState?.invoke()
                                    
                                    // 導航到登入頁面
                                    navController.navigate("login") {
                                        launchSingleTop = true
                                    }
                                }
                            } else if (index == 3) { // 設置項目的索引
                                // 導航到設定頁面
                                navController.navigate("settings") {
                                    // 清空之前的settings路由，確保每次都是新進入
                                    popUpTo("settings") {
                                        inclusive = true
                                    }
                                    // 防止創建多個實例
                                    launchSingleTop = true
                                }
                            } else if (index == 4) { // 關於我們項目的索引
                                // 導航到關於我們頁面
                                navController.navigate("about_us") {
                                    // 清空之前的about_us路由，確保每次都是新進入
                                    popUpTo("about_us") {
                                        inclusive = true
                                    }
                                    // 防止創建多個實例
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 主界面內容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars) // 添加狀態欄填充
            ) {
                // 获取当前路由以判断是否为登录相关页面
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // 定义登录相关页面的路由列表
                val authRoutes = listOf(
                    "login", 
                    "register", 
                    "forgot_password", 
                    "verification_code/{username}/{email}",
                    "reset_password/{username}/{email}/{code}",
                    "change_password"
                )
                
                // 检查当前是否在登录相关页面
                val isAuthScreen = currentRoute?.let { route ->
                    authRoutes.any { auth ->
                        if (auth.contains("{")) {
                            // 对于带参数的路由，只检查前缀部分
                            route.startsWith(auth.substring(0, auth.indexOf("{")))
                        } else {
                            route == auth
                        }
                    }
                } ?: false
                
                // 首次处理导航的标志，确保只在需要时重定向到登录页
                val initialNavigationDone = remember { mutableStateOf(false) }
                
                // 只在用户已登录且首次导航时，尝试导航到首页
                LaunchedEffect(key1 = isLoggedIn.value, key2 = currentRoute) {
                    if (!initialNavigationDone.value) {
                        if (isLoggedIn.value && currentRoute == "login") {
                            // 如果已登录但在登录页，导航到首页
                            Log.d("MainActivity", "已登录，从登录页导航到首页")
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else if (!isLoggedIn.value && currentRoute != null && !isAuthScreen && currentRoute != "login") {
                            // 如果未登录且不在登录相关页面，导航到登录页
                            Log.d("MainActivity", "未登录，导航到登录页，当前页面: $currentRoute")
                            navController.navigate("login") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                        // 标记已完成第一次导航
                        initialNavigationDone.value = true
                    }
                }
                
                // 根據當前路由決定點擊用戶圖標的行為
                // 在登录相关页面不显示顶部导航栏
                if (!isAuthScreen) {
                    SeniorCareTopBar(
                        onUserIconClick = {
                            // 在所有頁面都打開側欄，不再做返回操作
                            scope.launch {
                                if (!leftDrawerState.isOpen) {
                                    leftDrawerState.open()
                                } else {
                                    leftDrawerState.close()
                                }
                            }
                        },
                        onNotificationClick = {
                            // 導航到通知頁面，不清空整個導航堆棧
                            navController.navigate("notifications") {
                                // 保留導航堆棧中的現有目的地
                                // 防止創建多個實例
                                launchSingleTop = true
                                // 保存和恢復狀態
                                restoreState = true
                            }
                        },
                        isLoggedIn = isLoggedIn.value
                    )
                }
                
                Scaffold(
                    // 只在非登录相关页面显示底部导航栏
                    bottomBar = {
                        if (!isAuthScreen) {
                            NavigationBar {
                                val navBackStackEntryForBottomNav by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntryForBottomNav?.destination
                                
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.name) },
                                        label = { Text(item.name) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            // 如果未登录，只能导航到登录页
                                            if (!isLoggedIn.value && currentRoute != "login") {
                                                navController.navigate("login") {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        inclusive = true
                                                    }
                                                }
                                                return@NavigationBarItem
                                            }
                                            
                                            if (item.route == "more") {
                                                // 點擊"更多"顯示右側邊欄
                                                showRightDrawer = true
                                            } else {
                                                // 針對首頁按鈕做特殊處理
                                                if (item.route == "home") {
                                                    // 完全清空導航堆棧到主頁
                                                    navController.navigate("home") {
                                                        popUpTo("home") {
                                                            inclusive = true
                                                        }
                                                    }
                                                } 
                                                // 針對監控按鈕做特殊處理
                                                else if (item.route == "monitor" && currentRoute in listOf("temperature_monitor", "heart_rate_monitor", "diaper_monitor")) {
                                                    // 如果已經在監控子頁面，點擊監控按鈕應該返回主監控頁面
                                                    navController.navigate("monitor") {
                                                        // 清空監控相關頁面
                                                        popUpTo("monitor") {
                                                            inclusive = true
                                                        }
                                                    }
                                                }
                                                else {
                                                    navController.navigate(item.route) {
                                                        // 完全清空導航堆棧，不保存任何狀態
                                                        // 這樣可以確保用戶通過底部導航切換頁面時，不會保留帶有返回按鈕的頁面的歷史記錄
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            inclusive = true
                                                            saveState = false  // 不保存狀態，確保完全重置
                                                        }
                                                        // 防止重複點擊
                                                        launchSingleTop = true
                                                        // 恢復狀態
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    // 根据当前页面类型决定是否使用内边距
                    val contentModifier = if (isAuthScreen) {
                        // 登录相关页面不需要底部内边距
                        Modifier
                    } else {
                        // 其他页面需要考虑底部导航栏的内边距
                        Modifier.padding(innerPadding)
                    }
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = contentModifier
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("monitor") { MonitorScreen(navController) }
                        composable("map") { MapScreen(navController) }
                        composable("timer") { 
                            // 直接使用完整的TimerScreen實現
                            TimerScreen(navController) 
                        }
                        
                        // 登入頁面
                        composable("login") { LoginScreen(navController) }
                        
                        // 註冊頁面
                        composable("register") { RegisterScreen(navController) }
                        
                        // 忘記密碼頁面
                        composable("forgot_password") { ForgotPasswordScreen(navController) }
                        
                        // 驗證碼頁面
                        composable(
                            "verification_code/{username}/{email}",
                            arguments = listOf(
                                navArgument("username") { type = NavType.StringType },
                                navArgument("email") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val username = entry.arguments?.getString("username") ?: ""
                            val email = entry.arguments?.getString("email") ?: ""
                            VerificationCodeScreen(navController, username, email)
                        }
                        
                        // 重設密碼頁面
                        composable(
                            "reset_password/{username}/{email}",
                            arguments = listOf(
                                navArgument("username") { type = NavType.StringType },
                                navArgument("email") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val username = entry.arguments?.getString("username") ?: ""
                            val email = entry.arguments?.getString("email") ?: ""
                            ResetPasswordScreen(navController, username, email)
                        }
                        
                        // 修改密碼頁面
                        composable("change_password") { ChangePasswordScreen(navController) }
                        
                        // 添加新功能頁面的導航路由
                        composable("region") { RegionScreen(navController) }
                        composable("temperature") { TemperatureScreen(navController) }
                        composable("temperature_monitor") { TemperatureMonitorScreen(navController) }
                        composable("diaper") { DiaperScreen(navController) }
                        composable("diaper_monitor") { DiaperMonitorScreen(navController) }
                        composable("button") { ButtonScreen(navController) }
                        composable("emergency_button") { EmergencyButtonScreen(navController) }
                        composable("heart_rate") { HeartRateScreen(navController) }
                        composable("heart_rate_monitor") { HeartRateMonitorScreen(navController) }
                        
                        // 通知頁面
                        composable("notifications") { NotificationScreen(navController) }
                        
                        // 設定頁面
                        composable("settings") { SettingsScreen(navController) }
                        
                        // 問題回報頁面
                        composable("issue_report") { IssueReportScreen(navController) }
                        
                        // 關於我們頁面
                        composable("about_us") { AboutUsScreen(navController) }
                        
                        // 個人郵箱頁面
                        composable("mailbox") { MailboxScreen(navController) }
                        
                        // 個人資料頁面
                        composable("profile") { ProfileScreen(navController) }
                        // 個人資料編輯頁面
                        composable("profile_edit") { ProfileEditScreen(navController) }
                        
                        // 管理頁面
                        composable("patient_admin") { ResidentManagementScreen(navController) }
                        composable("staff_admin") { StaffManagementScreen(navController) }
                        composable("equipment_admin") { EquipmentManagementScreen(navController) }
                        composable("settings_admin") { IssueReportScreen(navController) }
                    }
                }
            }
            
            // 右側邊欄 - 管理功能菜單
            if (showRightDrawer) {
                AdminDrawer(
                    adminItems = adminItems,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            // 防止創建多個實例
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // 防止重複點擊
                            launchSingleTop = true
                            // 恢復狀態
                            restoreState = true
                        }
                        showRightDrawer = false
                    },
                    onClose = { showRightDrawer = false }
                )
            }
        }
    }
}

@Composable
fun AdminDrawer(
    adminItems: List<AdminItem>,
    onItemClick: (String) -> Unit,
    onClose: () -> Unit
) {
    // 檢查是否為深色模式
    val isDarkTheme = ThemeManager.isDarkTheme
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    // 半透明背景，點擊關閉抽屉
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x44000000))
            .clickable { onClose() }
    ) {
        // 右側菜單內容
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                // 添加安全区域的内边距，避免与系统UI重叠
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(bottom = 24.dp)
                // 右侧边栏宽度
                .fillMaxWidth(0.45f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    clip = true
                )
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(if (isDarkTheme) MaterialTheme.colorScheme.surface else Color(0xFFCDCDCD))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                .padding(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
                // 阻止點擊事件傳遞到下層
                .clickable(enabled = false, onClick = {})
        ) {
            // 菜單項目
            adminItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)  // 減少垂直間距
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE0E0E0))
                        .clickable { onItemClick(item.route) }
                        .padding(vertical = 16.dp, horizontal = 12.dp),  // 添加水平內邊距，減少垂直內邊距
                    contentAlignment = Alignment.Center
                ) {
                    if (isChineseLanguage) {
                        // 中文不需要换行
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            fontSize = 16.sp
                        )
                    } else {
                        // 英文需要分行显示
                        val words = item.title.split(" ")
                        if (words.size > 1) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 第一行显示单词
                                Text(
                                    text = words[0],
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp
                                )
                                // 第二行显示剩余的单词
                                Text(
                                    text = words.subList(1, words.size).joinToString(" "),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            // 只有一个单词的情况，如"Settings"
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPageTemplate(title: String, navController: androidx.navigation.NavController) {
    val isChineseLanguage = LanguageManager.isChineseLanguage
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 可根据不同页面添加相应内容
                Text(
                    text = if (isChineseLanguage) "管理功能頁面" else "Management Function Page",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // 删除返回按钮
            }
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// 側邊欄項目數據類
data class DrawerItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// 管理功能項目數據類
data class AdminItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)