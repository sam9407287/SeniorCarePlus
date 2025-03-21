package com.example.myapplication.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ReminderViewModel : ViewModel() {
    private val _reminders = mutableStateListOf<ReminderItem>()
    val reminders: List<ReminderItem> get() = _reminders
    
    private val sharedPreferences: SharedPreferences by lazy {
        MyApplication.instance.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
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
                
                val reminder = ReminderItem(
                    id = reminderJson.getInt("id"),
                    title = reminderJson.getString("title"),
                    time = reminderJson.getString("time"),
                    days = days,
                    type = type
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
    }
    
    fun updateReminder(updatedReminder: ReminderItem) {
        val index = _reminders.indexOfFirst { it.id == updatedReminder.id }
        if (index != -1) {
            _reminders[index] = updatedReminder
            saveReminders()
        }
    }
    
    fun deleteReminder(id: Int) {
        val index = _reminders.indexOfFirst { it.id == id }
        if (index != -1) {
            _reminders.removeAt(index)
            saveReminders()
        }
    }
    
    fun getNextId(): Int {
        return _reminders.maxOfOrNull { it.id }?.plus(1) ?: 1
    }
    
    private fun addDefaultReminders() {
        _reminders.addAll(
            listOf(
                ReminderItem(
                    id = 1,
                    title = "早晨服藥",
                    time = "08:00",
                    days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                    type = ReminderType.MEDICATION
                ),
                ReminderItem(
                    id = 2,
                    title = "喝水提醒",
                    time = "10:30",
                    days = listOf("週一", "週二", "週三", "週四", "週五"),
                    type = ReminderType.WATER
                ),
                ReminderItem(
                    id = 3,
                    title = "測量心率",
                    time = "14:00",
                    days = listOf("週一", "週三", "週五"),
                    type = ReminderType.HEART_RATE
                ),
                ReminderItem(
                    id = 4,
                    title = "晚餐時間",
                    time = "18:30",
                    days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                    type = ReminderType.MEAL
                ),
                ReminderItem(
                    id = 5,
                    title = "晚上服藥",
                    time = "21:00",
                    days = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日"),
                    type = ReminderType.MEDICATION
                )
            )
        )
        saveReminders()
    }
}
