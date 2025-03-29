package com.seniorcareplus.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.seniorcareplus.app.MainActivity
import com.seniorcareplus.app.R
import com.seniorcareplus.app.auth.UserManager
import com.seniorcareplus.app.ui.theme.LanguageManager

class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_ID = "extra_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Received reminder broadcast")
        
        // 檢查用戶是否登錄，只有登錄狀態才送出通知
        if (!UserManager.isLoggedIn()) {
            Log.d("ReminderReceiver", "用戶未登錄，不發送通知")
            return
        }
        
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "提醒"
        val type = intent.getStringExtra(EXTRA_TYPE) ?: ""
        val id = intent.getIntExtra(EXTRA_ID, 0)
        
        Log.d("ReminderReceiver", "接收到提醒，標題: $title, 類型: $type, ID: $id")
        
        // 在鎖屏狀態下也能顯示全屏提醒對話框
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            // 移除 FLAG_ACTIVITY_CLEAR_TOP 和 FLAG_ACTIVITY_CLEAR_TASK 以確保保留取上繼続聚焦
            // 只保留 FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_SINGLE_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_REMINDER_DIALOG", true)
            putExtra("REMINDER_ID", id)
            // 添加提醒类型信息，确保显示正确的提醒类型
            putExtra("REMINDER_TYPE", type)
            // 添加标记，表示这是从通知打开的
            putExtra("FROM_NOTIFICATION", true)
            // 添加时间戳确保Intent被视为新的
            putExtra("TIMESTAMP", System.currentTimeMillis())
        }
        
        // 创建PendingIntent用于通知
        val pendingIntent = PendingIntent.getActivity(
            context, 
            id, 
            notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建通知
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            // 先尝试启动全屏Activity
            context.startActivity(notificationIntent)
            Log.d("ReminderReceiver", "成功启动全屏提醒对话框")
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "启动全屏提醒对话框失败: ${e.message}")
            // 如果失败，确保至少通知被发送
        }
        
        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "提醒通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "顯示定時提醒通知"
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // 获取适合的图标
        val smallIcon = when(type) {
            "MEDICATION" -> R.drawable.ic_launcher_foreground // 理想情况下应使用专门的药物图标
            "WATER" -> R.drawable.ic_launcher_foreground // 水图标
            "MEAL" -> R.drawable.ic_launcher_foreground // 餐饮图标
            "HEART_RATE" -> R.drawable.ic_launcher_foreground // 心率图标
            "TEMPERATURE" -> R.drawable.ic_launcher_foreground // 温度图标
            else -> R.drawable.ic_launcher_foreground
        }
        
        // 获取适合的内容文本
        val contentText = when(type) {
            "MEDICATION" -> if (LanguageManager.isChineseLanguage) "現在是服藥時間" else "It's time to take your medication"
            "WATER" -> if (LanguageManager.isChineseLanguage) "請記得喝水" else "Remember to drink water"
            "MEAL" -> if (LanguageManager.isChineseLanguage) "現在是用餐時間" else "It's time for your meal"
            "HEART_RATE" -> if (LanguageManager.isChineseLanguage) "請測量您的心率" else "Please check your heart rate"
            "TEMPERATURE" -> if (LanguageManager.isChineseLanguage) "請測量您的體溫" else "Please check your temperature"
            else -> if (LanguageManager.isChineseLanguage) "您設置的提醒時間到了" else "Your reminder time has arrived"
        }
        
        // 構建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(smallIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 設置為鬧鈴類別
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在鎖屏上也顯示完整通知
            .setFullScreenIntent(pendingIntent, true) // 使用全屏意圖
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // 添加声音
            .setVibrate(longArrayOf(0, 500, 200, 500)) // 添加振动模式
            .build()
        
        // 顯示通知
        notificationManager.notify(id, notification)
    }
}
