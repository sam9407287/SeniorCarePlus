package com.seniorcareplus.app.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorcareplus.app.MyApplication
import com.seniorcareplus.app.reminder.ReminderManager
import com.seniorcareplus.app.ui.theme.ThemeManager
import com.seniorcareplus.app.auth.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ReminderViewModel : ViewModel() {
    private val _reminders = mutableStateListOf<ReminderItem>()
    val reminders: List<ReminderItem> get() = _reminders
    
    // 當前選中的提醒項目（用於顯示提醒對話框）
    private val _currentReminder = mutableStateOf<ReminderItem?>(null)
    val currentReminder get() = _currentReminder.value
    
    // 是否顯示提醒對話框
    private val _showReminderAlert = mutableStateOf(false)
    val showReminderAlert get() = _showReminderAlert.value
    
    // 是否顯示全屏提醒對話框
    private val _showFullScreenAlert = mutableStateOf(false)
    val showFullScreenAlert get() = _showFullScreenAlert.value
    
    private val sharedPreferences: SharedPreferences by lazy {
        MyApplication.instance.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
    }
    
    private val reminderManager: ReminderManager by lazy {
        ReminderManager(MyApplication.instance)
    }
    
    // 確保 ViewModel 被創建時立即加載數據
    // 主題切換時的監聽器，只清除當前顯示的提醒對話框
    private val themeChangeListener: () -> Unit = {
        // 只清除對話框狀態，不影響後續提醒處理
        if (_showReminderAlert.value || _showFullScreenAlert.value) {
            Log.d("ReminderViewModel", "主題即將切換，關閉當前提醒對話框")
            _showReminderAlert.value = false
            _showFullScreenAlert.value = false
            
            // 留下當前提醒的引用，如果需要稍後再次顯示
            val currentId = _currentReminder.value?.id
            if (currentId != null) {
                Log.d("ReminderViewModel", "保存當前提醒ID: $currentId 以便於切換後可能需要再次顯示")
            }
        }
    }
    
    // 登入狀態變更監聽器
    private val loginStateObserver: () -> Unit = {
        Log.d("ReminderViewModel", "登入狀態已變更，重新加載提醒列表")
        loadReminders()
    }
    
    init {
        Log.d("ReminderViewModel", "初始化 ViewModel")
        // 註冊主題切換監聽器
        ThemeManager.addThemeChangeListener(themeChangeListener)
        // 這裡應該註冊登入狀態變更監聽器，但UserManager暫時沒有提供這個功能
        // 因此我們只能在init時加載數據
        loadReminders()
    }
    
    /**
     * 繼承 ViewModel 的 onCleared 方法，在 ViewModel 銷毀時註銷監聽器
     */
    override fun onCleared() {
        super.onCleared()
        ThemeManager.removeThemeChangeListener(themeChangeListener)
        Log.d("ReminderViewModel", "ViewModel 已銷毀，移除主題切換監聽器")
    }
    
    /**
     * 獲取當前用戶的提醒數據存儲鍵
     * 已登入時使用用戶名作為標識，未登入時返回null
     */
    private fun getCurrentUserKey(): String? {
        val currentUsername = UserManager.getCurrentUsername()
        return if (currentUsername != null) {
            "reminders_$currentUsername"
        } else {
            null
        }
    }
    
    private fun loadReminders() {
        Log.d("ReminderViewModel", "開始加載提醒數據")
        
        // 清空當前提醒列表
        _reminders.clear()
        
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不加載提醒數據")
            return
        }
        
        // 獲取當前用戶的存儲鍵
        val userKey = getCurrentUserKey()
        if (userKey == null) {
            Log.d("ReminderViewModel", "無法獲取當前用戶的存儲鍵")
            return
        }
        
        try {
            val remindersJson = sharedPreferences.getString(userKey, null)
            
            if (remindersJson == null) {
                Log.d("ReminderViewModel", "沒有找到已保存的提醒數據，加載默認提醒")
                addDefaultReminders()
                return
            }
            
            Log.d("ReminderViewModel", "加載的JSON數據: $remindersJson")
            val jsonArray = JSONArray(remindersJson)
            
            for (i in 0 until jsonArray.length()) {
                val reminderJson = jsonArray.getJSONObject(i)
                
                // Parse days list
                val daysJsonArray = reminderJson.getJSONArray("days")
                val days = mutableListOf<String>()
                for (j in 0 until daysJsonArray.length()) {
                    days.add(daysJsonArray.getString(j))
                }
                
                // Parse reminder type
                val typeString = reminderJson.getString("type")
                val type = ReminderType.valueOf(typeString)
                
                // 加載啟用/禁用狀態，如果沒有則默認為啟用
                val isEnabled = if (reminderJson.has("isEnabled")) {
                    reminderJson.getBoolean("isEnabled")
                } else {
                    true
                }
                
                val reminder = ReminderItem(
                    id = reminderJson.getInt("id"),
                    title = reminderJson.getString("title"),
                    time = reminderJson.getString("time"),
                    days = days,
                    type = type,
                    isEnabled = isEnabled
                )
                
                _reminders.add(reminder)
            }
            
            Log.d("ReminderViewModel", "已加載 ${_reminders.size} 個提醒項目")
            
            // 應用啟動時重新設置所有啟用的提醒鬧鈴
            rescheduleAllEnabledReminders()
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "加載提醒數據時出錯: ${e.message}")
            // 如果加載出錯且提醒列表為空，則添加默認提醒
            if (_reminders.isEmpty()) {
                addDefaultReminders()
            }
        }
    }
    
    private fun saveReminders() {
        Log.d("ReminderViewModel", "開始保存提醒數據")
        
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不保存提醒數據")
            return
        }
        
        // 獲取當前用戶的存儲鍵
        val userKey = getCurrentUserKey()
        if (userKey == null) {
            Log.d("ReminderViewModel", "無法獲取當前用戶的存儲鍵")
            return
        }
        
        try {
            val jsonArray = JSONArray()
            
            for (reminder in _reminders) {
                val reminderJson = JSONObject().apply {
                    put("id", reminder.id)
                    put("title", reminder.title)
                    put("time", reminder.time)
                    
                    // Convert days list to JSON array
                    val daysJsonArray = JSONArray()
                    for (day in reminder.days) {
                        daysJsonArray.put(day)
                    }
                    put("days", daysJsonArray)
                    
                    // Store enum as string
                    put("type", reminder.type.name)
                    
                    // 保存啟用/禁用狀態
                    put("isEnabled", reminder.isEnabled)
                }
                
                jsonArray.put(reminderJson)
            }
            
            val jsonString = jsonArray.toString()
            Log.d("ReminderViewModel", "保存的JSON數據: $jsonString")
            
            // 使用 commit() 而不是 apply() 確保立即寫入
            val result = sharedPreferences.edit()
                .putString(userKey, jsonString)
                .commit()
                
            Log.d("ReminderViewModel", "保存結果: $result")
            
            Log.d("ReminderViewModel", "已保存 ${_reminders.size} 個提醒項目")
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "保存提醒數據時出錯: ${e.message}")
        }
    }
    
    /**
     * 重新設置所有啟用的提醒鬧鐘
     * 當應用啟動時調用，確保重啟後提醒依然有效
     */
    private fun rescheduleAllEnabledReminders() {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不設置提醒鬧鐘")
            return
        }
        
        try {
            Log.d("ReminderViewModel", "開始重新設置所有啟用的提醒鬧鐘")
            
            // 先取消所有提醒以避免重複
            _reminders.forEach { reminder ->
                reminderManager.cancelReminder(reminder)
                Log.d("ReminderViewModel", "取消提醒: ID=${reminder.id}, 標題=${reminder.title}")
            }
            
            // 然後重新設置所有啟用的提醒
            val enabledReminders = _reminders.filter { it.isEnabled }
            Log.d("ReminderViewModel", "重新設置 ${enabledReminders.size} 個啟用的提醒")
            
            enabledReminders.forEach { reminder ->
                reminderManager.scheduleReminder(reminder)
                Log.d("ReminderViewModel", "已重新設置提醒: ID=${reminder.id}, 標題=${reminder.title}")
            }
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "重新設置提醒失敗: ${e.message}")
        }
    }
    
    fun addReminder(reminder: ReminderItem) {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不添加提醒")
            return
        }
        
        _reminders.add(reminder)
        saveReminders()
        // 設置提醒鬧鐘
        Log.d("ReminderViewModel", "添加新提醒: ID=${reminder.id}, 標題=${reminder.title}, 時間=${reminder.time}, 類型=${reminder.type.name}")
        reminderManager.scheduleReminder(reminder)
    }
    
    fun updateReminder(updatedReminder: ReminderItem) {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不更新提醒")
            return
        }
        
        val index = _reminders.indexOfFirst { it.id == updatedReminder.id }
        if (index != -1) {
            val oldReminder = _reminders[index]
            _reminders[index] = updatedReminder
            saveReminders()
            
            // 更新提醒鬧鐘
            reminderManager.updateReminder(oldReminder, updatedReminder)
        }
    }
    
    fun deleteReminder(id: Int) {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不刪除提醒")
            return
        }
        
        val index = _reminders.indexOfFirst { it.id == id }
        if (index != -1) {
            val reminder = _reminders[index]
            _reminders.removeAt(index)
            saveReminders()
            
            // 取消提醒鬧鐘
            reminderManager.cancelReminder(reminder)
        }
    }
    
    fun getNextId(): Int {
        return _reminders.maxOfOrNull { it.id }?.plus(1) ?: 1
    }
    
    private fun addDefaultReminders() {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不添加默認提醒")
            return
        }
        
        val defaultReminders = listOf(
            ReminderItem(
                id = 1,
                title = "早晨服藥",
                time = "08:00",
                days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                type = ReminderType.MEDICATION,
                isEnabled = true
            ),
            ReminderItem(
                id = 2,
                title = "喝水提醒",
                time = "10:30",
                days = listOf("週一", "週二", "週三", "週四", "週五"),
                type = ReminderType.WATER,
                isEnabled = true
            ),
            ReminderItem(
                id = 3,
                title = "測量心率",
                time = "14:00",
                days = listOf("週一", "週三", "週五"),
                type = ReminderType.HEART_RATE,
                isEnabled = true
            ),
            ReminderItem(
                id = 4,
                title = "晚餐時間",
                time = "18:30",
                days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                type = ReminderType.MEAL,
                isEnabled = true
            ),
            ReminderItem(
                id = 5,
                title = "晚上服藥",
                time = "21:00",
                days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                type = ReminderType.MEDICATION,
                isEnabled = true
            )
        )
        
        _reminders.addAll(defaultReminders)
        saveReminders()
        
        // 為默認提醒設置鬧鐘
        defaultReminders.forEach { reminder ->
            reminderManager.scheduleReminder(reminder)
        }
    }
    
    /**
     * 設置當前提醒並顯示提醒對話框
     */
    fun showReminderAlert(reminderId: Int) {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不顯示提醒")
            return
        }
        
        val reminder = _reminders.find { it.id == reminderId }
        if (reminder != null) { // 仅检查提醒是否存在，移除isEnabled检查
            _currentReminder.value = reminder
            _showFullScreenAlert.value = true  // 使用全屏提醒對話框
            Log.d("ReminderViewModel", "顯示全屏提醒對話框: ${reminder.title}")
        } else {
            Log.d("ReminderViewModel", "未找到ID为 $reminderId 的提醒或提醒已禁用")
        }
    }
    
    /**
     * 設置當前提醒並顯示提醒對話框，但關閉後不跳轉到首頁
     * 用於從通知打開的提醒，確保關閉後返回到原本的頁面
     */
    fun showReminderAlertWithoutRedirect(reminderId: Int) {
        showReminderAlertWithoutRedirect(reminderId, null)
    }
    
    /**
     * 設置當前提醒並顯示提醒對話框，但關閉後不跳轉到首頁
     * 用於從通知打開的提醒，確保關閉後返回到原本的頁面
     * 且接受提醒类型参数，确保显示正确的提醒类型
     */
    fun showReminderAlertWithoutRedirect(reminderId: Int, reminderTypeStr: String?) {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不顯示提醒")
            return
        }
        
        var reminder = _reminders.find { it.id == reminderId }
        
        // 转换字符串类型为枚举类型（如果有）
        val reminderType = try {
            if (!reminderTypeStr.isNullOrEmpty()) ReminderType.valueOf(reminderTypeStr) else null
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "无法转换提醒类型: $reminderTypeStr", e)
            null
        }
        
        if (reminder == null && reminderType != null) {
            // 如果找不到提醒但有提供类型，创建一个新的提醒对象
            Log.d("ReminderViewModel", "创建临时提醒对象，类型: $reminderType, ID: $reminderId")
            
            // 创建一个新的默认时间字符串
            val defaultTime = "12:00"
            
            // 创建一个新的周列表
            val defaultDays = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日")
            
            reminder = ReminderItem(
                id = reminderId,
                title = getDefaultTitleForType(reminderType),
                time = defaultTime,
                days = defaultDays,
                type = reminderType,
                isEnabled = true
            )
        }
        
        if (reminder != null && reminderType != null && reminder.type != reminderType) {
            // 更新类型（如果提供了新类型）
            Log.d("ReminderViewModel", "更新提醒类型从 ${reminder.type} 到 $reminderType")
            
            // 创建新的ReminderItem实例替换旧的
            reminder = ReminderItem(
                id = reminder.id,
                title = reminder.title,
                time = reminder.time,
                days = reminder.days,
                type = reminderType,
                isEnabled = reminder.isEnabled
            )
        }
        
        if (reminder != null) {
            _currentReminder.value = reminder
            _showFullScreenAlert.value = true
            Log.d("ReminderViewModel", "顯示不跳轉的全屏提醒對話框: ${reminder.title}, 类型: ${reminder.type}")
        } else {
            Log.d("ReminderViewModel", "未找到ID为 $reminderId 的提醒且未提供有效类型")
        }
    }
    
    /**
     * 根据提醒类型获取默认标题
     */
    private fun getDefaultTitleForType(type: ReminderType): String {
        return when(type) {
            ReminderType.MEDICATION -> if (isChineseLanguage()) "服藥提醒" else "Medication Reminder"
            ReminderType.WATER -> if (isChineseLanguage()) "喝水提醒" else "Water Reminder"
            ReminderType.MEAL -> if (isChineseLanguage()) "用餐提醒" else "Meal Reminder"
            ReminderType.HEART_RATE -> if (isChineseLanguage()) "心率提醒" else "Heart Rate Reminder"
            ReminderType.TEMPERATURE -> if (isChineseLanguage()) "體溫提醒" else "Temperature Reminder"
            else -> if (isChineseLanguage()) "自定義提醒" else "Custom Reminder"
        }
    }
    
    /**
     * 检查当前是否为中文语言
     */
    private fun isChineseLanguage(): Boolean {
        return try {
            com.seniorcareplus.app.ui.theme.LanguageManager.isChineseLanguage
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "检查语言时出错", e)
            false
        }
    }
    
    /**
     * 切換提醒的啟用/禁用狀態
     */
    fun toggleReminder(reminderId: Int, isEnabled: Boolean) {
        // 檢查是否已登入
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderViewModel", "用戶未登入，不切換提醒狀態")
            return
        }
        
        val index = _reminders.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val reminder = _reminders[index]
            // 創建一個新的提醒項目，更新啟用狀態
            val updatedReminder = reminder.copy(isEnabled = isEnabled)
            _reminders[index] = updatedReminder
            
            // 保存更新後的提醒列表
            saveReminders()
            
            // 根據啟用狀態設置或取消鬧鐘
            if (isEnabled) {
                // 啟用提醒，設置鬧鐘
                reminderManager.scheduleReminder(updatedReminder)
            } else {
                // 禁用提醒，取消鬧鐘
                reminderManager.cancelReminder(updatedReminder)
            }
        }
    }
    
    /**
     * 隱藏提醒對話框
     */
    fun hideReminderAlert() {
        Log.d("ReminderViewModel", "隱藏提醒對話框")
        // 確保先設置為 false，然後才清除當前提醒
        _showReminderAlert.value = false
        _showFullScreenAlert.value = false
        
        // 立即清除當前提醒，確保狀態完全重置
        _currentReminder.value = null
        Log.d("ReminderViewModel", "已清除當前提醒")
    }
    
    /**
     * 稍後提醒（5分鐘後再次提醒）
     */
    fun snoozeReminder() {
        currentReminder?.let { reminder ->
            // 這裡可以實現稍後提醒的邏輯
            // 例如5分鐘後再次顯示提醒
            Log.d("ReminderViewModel", "Snoozing reminder: ${reminder.id}")
        }
        hideReminderAlert()
    }
    
    /**
     * 當登入狀態變更時，重新加載提醒數據
     * 可從外部呼叫此方法來響應登入/登出事件
     */
    fun onLoginStateChanged() {
        loadReminders()
    }
}
