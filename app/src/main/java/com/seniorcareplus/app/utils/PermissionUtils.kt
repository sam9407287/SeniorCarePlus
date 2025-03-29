package com.seniorcareplus.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限工具类，用于简化权限申请和管理
 */
object PermissionUtils {
    
    private const val TAG = "PermissionUtils"
    
    /**
     * 检查是否有通知权限
     * @param context 上下文
     * @return 是否有通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 13以下版本默认有通知权限
            true
        }
    }
    
    /**
     * 请求通知权限
     * @param launcher 权限请求启动器
     */
    fun requestNotificationPermission(launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    /**
     * 打开应用详情页面，用于用户手动授予权限
     * @param activity 活动上下文
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
    
    /**
     * 检查并请求通知权限
     * @param activity 活动上下文
     * @param launcher 权限请求启动器
     * @return 是否已经拥有权限
     */
    fun checkAndRequestNotificationPermission(
        activity: Activity,
        launcher: ActivityResultLauncher<String>
    ): Boolean {
        return if (hasNotificationPermission(activity)) {
            Log.d(TAG, "已有通知权限")
            true
        } else {
            Log.d(TAG, "请求通知权限")
            requestNotificationPermission(launcher)
            false
        }
    }
} 