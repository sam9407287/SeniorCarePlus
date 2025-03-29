package com.seniorcareplus.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.seniorcareplus.app.database.AppDatabase

/**
 * 用戶管理類，處理用戶相關的全局功能
 */
object UserManager {
    private var sharedPreferences: SharedPreferences? = null
    private const val PREF_NAME = "user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    
    // 用於監聽登入狀態變化的回調列表
    private val loginStateObservers = mutableListOf<((String?) -> Unit)>()
    
    /**
     * 初始化用戶管理器
     */
    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            Log.d("UserManager", "初始化完成")
        }
    }
    
    /**
     * 設置用戶為已登入狀態
     */
    fun setLoggedIn(context: Context, username: String, email: String? = null) {
        val editor = sharedPreferences?.edit()
        editor?.putBoolean(KEY_IS_LOGGED_IN, true)
        editor?.putString(KEY_USERNAME, username)
        editor?.putString(KEY_EMAIL, email)
        editor?.apply()
        
        // 通知所有觀察者
        notifyLoginStateChanged(username)
        
        Log.d("UserManager", "用戶已登入: $username")
    }
    
    /**
     * 用戶登出並清除登入狀態
     * @param context 可選參數，如果提供則會清除該用戶的體溫數據
     */
    @JvmOverloads
    fun logout(context: Context? = null) {
        val editor = sharedPreferences?.edit()
        val username = getCurrentUsername()
        
        editor?.putBoolean(KEY_IS_LOGGED_IN, false)
        editor?.remove(KEY_USERNAME)
        editor?.remove(KEY_EMAIL)
        editor?.apply()
        
        // 如果提供了Context，則清除該用戶的體溫數據
        if (context != null && username != null) {
            // 在後台線程中執行以避免主線程阻塞
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    val db = AppDatabase.getInstance(context)
                    val deletedCount = db.deleteAllTemperatureRecords(username)
                    Log.d("UserManager", "已清除用戶 $username 的 $deletedCount 條體溫記錄")
                } catch (e: Exception) {
                    Log.e("UserManager", "清除體溫記錄時出錯: ${e.message}")
                }
            }
        }
        
        // 通知所有觀察者
        notifyLoginStateChanged(null)
        
        Log.d("UserManager", "用戶已登出")
    }
    
    /**
     * 檢查用戶是否已登入
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
    }
    
    /**
     * 獲取當前登入的用戶名
     */
    fun getCurrentUsername(): String? {
        return if (isLoggedIn()) {
            sharedPreferences?.getString(KEY_USERNAME, null)
        } else {
            null
        }
    }
    
    /**
     * 獲取當前登入用戶的郵箱
     */
    fun getCurrentEmail(): String? {
        return if (isLoggedIn()) {
            sharedPreferences?.getString(KEY_EMAIL, null)
        } else {
            null
        }
    }
    
    /**
     * 添加登入狀態變化的觀察者
     */
    fun observeLoginState(observer: (String?) -> Unit) {
        loginStateObservers.add(observer)
        // 立即通知當前狀態
        observer(if (isLoggedIn()) getCurrentUsername() else null)
    }
    
    /**
     * 移除登入狀態觀察者
     */
    fun removeLoginStateObserver(observer: (String?) -> Unit) {
        loginStateObservers.remove(observer)
    }
    
    /**
     * 通知所有觀察者登入狀態發生變化
     */
    private fun notifyLoginStateChanged(username: String?) {
        loginStateObservers.forEach { observer ->
            observer(username)
        }
    }
} 