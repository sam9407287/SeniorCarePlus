package com.example.myapplication.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MyApplication
import com.example.myapplication.reminder.ReminderManager
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
    init {
        Log.d("ReminderViewModel", "初始化 ViewModel")
        loadReminders()
    }
    

    
    private fun loadReminders() {
        Log.d("ReminderViewModel", "開始加載提醒數據")
        try {
            val remindersJson = sharedPreferences.getString("reminders", null)
            
            if (remindersJson == null) {
                Log.d("ReminderViewModel", "沒有找到已保存的提醒數據，加載默認提醒")
                addDefaultReminders()
                return
            }
            
            Log.d("ReminderViewModel", "加載的JSON數據: $remindersJson")
            val jsonArray = JSONArray(remindersJson)
            
            _reminders.clear()
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
            
            Log.d("ReminderViewModel", "Loaded ${_reminders.size} reminders from cache")
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "Error loading reminders: ${e.message}")
            // If there's an error loading, we'll start with default reminders
            if (_reminders.isEmpty()) {
                addDefaultReminders()
            }
        }
    }
    
    private fun saveReminders() {
        Log.d("ReminderViewModel", "開始保存提醒數據")
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
                .putString("reminders", jsonString)
                .commit()
                
            Log.d("ReminderViewModel", "保存結果: $result")
            
            Log.d("ReminderViewModel", "Saved ${_reminders.size} reminders to cache")
        } catch (e: Exception) {
            Log.e("ReminderViewModel", "Error saving reminders: ${e.message}")
        }
    }
    
    fun addReminder(reminder: ReminderItem) {
        _reminders.add(reminder)
        saveReminders()
        // 設置提醒鬧鐘
        reminderManager.scheduleReminder(reminder)
    }
    
    fun updateReminder(updatedReminder: ReminderItem) {
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
        val reminder = _reminders.find { it.id == reminderId }
        if (reminder != null && reminder.isEnabled) { // 只有啟用的提醒才會顯示提醒對話框
            _currentReminder.value = reminder
            _showFullScreenAlert.value = true  // 使用全屏提醒對話框
            Log.d("ReminderViewModel", "顯示全屏提醒對話框: ${reminder.title}")
        }
    }
    
    /**
     * 切換提醒的啟用/禁用狀態
     */
    fun toggleReminder(reminderId: Int, isEnabled: Boolean) {
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
}
