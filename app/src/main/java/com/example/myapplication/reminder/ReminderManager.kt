package com.example.myapplication.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.myapplication.ui.screens.ReminderItem
import java.util.Calendar
import java.util.Locale

class ReminderManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * 設置提醒鬧鐘
     */
    fun scheduleReminder(reminder: ReminderItem) {
        if (!reminder.isEnabled) {
            Log.d("ReminderManager", "Skipping disabled reminder: ${reminder.id} - ${reminder.title}")
            return
        }
        
        Log.d("ReminderManager", "Scheduling reminder: ${reminder.id} - ${reminder.title}, 類型: ${reminder.type.name}")
        
        // 解析時間
        val (hour, minute) = parseTime(reminder.time)
        
        // 為每個選定的日期設置鬧鐘
        for (dayOfWeek in reminder.days) {
            val calendar = Calendar.getInstance()
            val dayIndex = getDayOfWeekIndex(dayOfWeek)
            
            // 設置時間
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // 設置星期幾
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysToAdd = (dayIndex - currentDayOfWeek + 7) % 7
            
            // 如果是今天且時間已過，則設置為下週同一天
            if (daysToAdd == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
            }
            
            // 創建 PendingIntent
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_TITLE, reminder.title)
                putExtra(ReminderReceiver.EXTRA_TYPE, reminder.type.name)
                putExtra(ReminderReceiver.EXTRA_ID, reminder.id)
                // 添加一個獨特的時間戳，確保每次都是不同的 Intent
                putExtra("timestamp", System.currentTimeMillis())
            }
            
            Log.d("ReminderManager", "Intent extras: 標題=${reminder.title}, 類型=${reminder.type.name}, ID=${reminder.id}")
            
            val requestCode = generateRequestCode(reminder.id, dayIndex)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // 設置鬧鐘
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(calendar.time)
            Log.d("ReminderManager", "Alarm set for $formattedTime, ID: $requestCode")
        }
    }
    
    /**
     * 取消提醒鬧鐘
     */
    fun cancelReminder(reminder: ReminderItem) {
        Log.d("ReminderManager", "Cancelling reminder: ${reminder.id} - ${reminder.title}, 類型: ${reminder.type.name}")
        
        // 為每個日期取消鬧鐘
        for (dayOfWeek in reminder.days) {
            val dayIndex = getDayOfWeekIndex(dayOfWeek)
            val requestCode = generateRequestCode(reminder.id, dayIndex)
            
            // 創建 Intent 但不設置 extras，因為我們只需要 requestCode 來取消鬧鐘
            val intent = Intent(context, ReminderReceiver::class.java)
            
            // 創建 PendingIntent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // 取消鬧鐘
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            Log.d("ReminderManager", "Alarm cancelled, ID: $requestCode")
        }
    }
    
    /**
     * 更新提醒鬧鐘
     */
    fun updateReminder(oldReminder: ReminderItem, newReminder: ReminderItem) {
        Log.d("ReminderManager", "更新提醒: 從 ${oldReminder.type.name} 到 ${newReminder.type.name}")
        
        // 先取消舊的提醒
        for (dayOfWeek in oldReminder.days) {
            val dayIndex = getDayOfWeekIndex(dayOfWeek)
            val requestCode = generateRequestCode(oldReminder.id, dayIndex)
            
            // 創建與舊提醒相同的 Intent
            val oldIntent = Intent(context, ReminderReceiver::class.java)
            val oldPendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                oldIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            // 如果舊的 PendingIntent 存在，則取消它
            if (oldPendingIntent != null) {
                alarmManager.cancel(oldPendingIntent)
                oldPendingIntent.cancel()
                Log.d("ReminderManager", "成功取消舊的提醒鬧鐘: $requestCode")
            }
        }
        
        // 設置新的提醒
        scheduleReminder(newReminder)
    }
    
    /**
     * 解析時間字符串 (格式: "HH:mm")
     */
    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        return if (parts.size == 2) {
            Pair(parts[0].toInt(), parts[1].toInt())
        } else {
            Pair(0, 0)
        }
    }
    
    /**
     * 獲取星期幾的索引值
     */
    private fun getDayOfWeekIndex(dayOfWeek: String): Int {
        return when (dayOfWeek) {
            "週日", "周日", "星期日", "Sunday" -> Calendar.SUNDAY
            "週一", "周一", "星期一", "Monday" -> Calendar.MONDAY
            "週二", "周二", "星期二", "Tuesday" -> Calendar.TUESDAY
            "週三", "周三", "星期三", "Wednesday" -> Calendar.WEDNESDAY
            "週四", "周四", "星期四", "Thursday" -> Calendar.THURSDAY
            "週五", "周五", "星期五", "Friday" -> Calendar.FRIDAY
            "週六", "周六", "星期六", "Saturday" -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
    }
    
    /**
     * 生成唯一的請求碼
     */
    private fun generateRequestCode(reminderId: Int, dayOfWeek: Int): Int {
        return reminderId * 10 + dayOfWeek
    }
}
