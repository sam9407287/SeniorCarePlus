package com.example.myapplication.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.MyApplication
import com.example.myapplication.database.AppDatabase

/**
 * 用戶管理類，處理用戶認證相關功能
 */
object UserManager {
    // 預設管理員帳號
    private const val DEFAULT_ADMIN_USERNAME = "admin"
    private const val DEFAULT_ADMIN_PASSWORD = "00000000"
    private const val DEFAULT_ADMIN_EMAIL = "admin@example.com"
    
    // SharedPreferences 存儲鍵
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_CURRENT_USERNAME = "current_username"
    private const val KEY_CURRENT_EMAIL = "current_email"
    
    // 獲取SQLite數據庫實例
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(MyApplication.instance)
    }
    
    // 獲取SharedPreferences
    private fun getPrefs(): SharedPreferences {
        return MyApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 驗證登錄凭證
     * @param username 用戶名
     * @param password 密碼
     * @return 如果憑證有效返回true，否則返回false
     */
    fun login(username: String, password: String): Boolean {
        // 檢查是否為預設管理員帳號
        val isAdminAccount = username == DEFAULT_ADMIN_USERNAME && password == DEFAULT_ADMIN_PASSWORD
        
        if (isAdminAccount) {
            // 管理員登錄成功，保存登錄狀態
            saveLoginState(username, DEFAULT_ADMIN_EMAIL)
            Log.d("UserManager", "管理員登錄成功: $username")
            return true
        }
        
        // 從SQLite數據庫驗證用戶
        val isValidUser = database.validateUser(username, password)
        
        if (isValidUser) {
            // 從數據庫獲取用戶郵箱
            val email = database.getUserEmail(username)
            // 保存登錄狀態
            saveLoginState(username, email)
            Log.d("UserManager", "一般用戶登錄成功: $username")
        }
        
        return isValidUser
    }
    
    /**
     * 註冊新用戶
     * @param username 用戶名
     * @param password 密碼
     * @param email 電子郵件（可選）
     * @return 如果註冊成功返回true，否則返回false
     */
    fun register(username: String, password: String, email: String? = null): Boolean {
        // 檢查用戶名是否為預設管理員名稱
        if (username == DEFAULT_ADMIN_USERNAME) {
            Log.d("UserManager", "註冊失敗，用戶名已被系統保留: $username")
            return false
        }
        
        // 使用數據庫註冊用戶
        return database.registerUser(username, password, email)
    }
    
    /**
     * 檢查用戶名是否已存在
     */
    fun isUserExists(username: String): Boolean {
        // 檢查是否為預設管理員帳號
        if (username == DEFAULT_ADMIN_USERNAME) {
            return true
        }
        
        // 從數據庫檢查用戶是否存在
        return database.isUserExists(username)
    }
    
    /**
     * 保存登錄狀態到SharedPreferences
     */
    private fun saveLoginState(username: String, email: String?) {
        getPrefs().edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USERNAME, username)
            .putString(KEY_CURRENT_EMAIL, email)
            .apply()
    }
    
    /**
     * 檢查用戶是否已登錄
     * @return 如果用戶已登錄返回true，否則返回false
     */
    fun isLoggedIn(): Boolean {
        return getPrefs().getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * 獲取當前登錄用戶名
     * @return 當前登錄的用戶名，如果未登錄則返回null
     */
    fun getCurrentUsername(): String? {
        return if (isLoggedIn()) {
            getPrefs().getString(KEY_CURRENT_USERNAME, null)
        } else {
            null
        }
    }
    
    /**
     * 獲取當前登錄用戶郵箱
     * @return 當前登錄的用戶郵箱，如果未登錄則返回null
     */
    fun getCurrentEmail(): String? {
        return if (isLoggedIn()) {
            getPrefs().getString(KEY_CURRENT_EMAIL, null)
        } else {
            null
        }
    }
    
    /**
     * 登出用戶
     */
    fun logout() {
        getPrefs().edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_CURRENT_USERNAME)
            .remove(KEY_CURRENT_EMAIL)
            .apply()
        
        Log.d("UserManager", "用戶已登出")
    }
} 