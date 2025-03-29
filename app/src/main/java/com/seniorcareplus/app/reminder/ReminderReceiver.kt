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
        
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "提醒"
        val type = intent.getStringExtra(EXTRA_TYPE) ?: ""
        val id = intent.getIntExtra(EXTRA_ID, 0)
        
        Log.d("ReminderReceiver", "接收到提醒，標題: $title, 類型: $type, ID: $id")
        
        // 在鎖屏狀態下也能顯示全屏提醒對話框
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            // 使用適當的標記來確保應用程序能在鎖屏狀態下顯示
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_REMINDER_DIALOG", true)
            putExtra("REMINDER_ID", id)
        }
        
        try {
            // 直接啟動 Activity 並顯示全屏提醒對話框
            context.startActivity(notificationIntent)
            Log.d("ReminderReceiver", "成功啟動全屏提醒對話框")
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "啟動全屏提醒對話框失敗: ${e.message}")
        }
        
        // 同時仍然發送通知，以確保用戶不會錯過提醒
        val pendingIntent = PendingIntent.getActivity(
            context, 
            id, 
            notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 創建通知
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 創建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "提醒通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "顯示定時提醒通知"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // 構建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("您設置的${type}提醒時間到了")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 使用應用圖標作為通知圖標
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 設置為鬧鈴類別
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在鎖屏上也顯示完整通知
            .setFullScreenIntent(pendingIntent, true) // 使用全屏意圖
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // 顯示通知
        notificationManager.notify(id, notification)
    }
}
