package com.seniorcareplus.app.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.seniorcareplus.app.MyApplication
import com.seniorcareplus.app.database.AppDatabase
import com.seniorcareplus.app.models.UserProfile
import java.util.Date

/**
 * 用戶管理類，處理用戶認證相關功能
 */
object UserManager {
    // 定義靜態標記，用於檢查是否已初始化
    private var isInitialized = false
    // 帳號類型常數
    const val ACCOUNT_TYPE_PATIENT = 1    // 院友
    const val ACCOUNT_TYPE_FAMILY = 2     // 家屬
    const val ACCOUNT_TYPE_STAFF = 3      // 員工
    const val ACCOUNT_TYPE_ADMIN = 4      // 管理人員
    const val ACCOUNT_TYPE_DEVELOPER = 5  // 開發人員
    
    // 性別常數
    const val GENDER_UNSPECIFIED = 0
    const val GENDER_MALE = 1
    const val GENDER_FEMALE = 2
    const val GENDER_OTHER = 3
    // 預設管理員帳號
    private const val DEFAULT_ADMIN_USERNAME = "admin"
    private const val DEFAULT_ADMIN_PASSWORD = "00000000"
    private const val DEFAULT_ADMIN_EMAIL = "admin@example.com"
    
    // SharedPreferences 存儲鍵
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_CURRENT_USERNAME = "current_username"
    private const val KEY_CURRENT_EMAIL = "current_email"
    private const val KEY_CURRENT_CHINESE_NAME = "current_chinese_name"
    private const val KEY_CURRENT_ENGLISH_NAME = "current_english_name"
    private const val KEY_CURRENT_ACCOUNT_TYPE = "current_account_type"
    private const val KEY_CURRENT_BIRTHDAY = "current_birthday"
    private const val KEY_CURRENT_GENDER = "current_gender"
    private const val KEY_CURRENT_PHONE = "current_phone"
    private const val KEY_CURRENT_ADDRESS = "current_address"
    private const val KEY_CURRENT_PROFILE_PHOTO = "current_profile_photo"
    private const val KEY_LAST_LOGIN_TIME = "last_login_time"
    private const val KEY_AUTO_LOGOUT = "auto_logout"
    
    // 記住我功能的存儲鍵
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_SAVED_USERNAME = "saved_username"
    private const val KEY_SAVED_PASSWORD = "saved_password" // 注意：實際應用中應該加密儲存密碼
    
    // 獲取SQLite數據庫實例
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(MyApplication.instance)
    }
    
    // 獲取SharedPreferences
    private fun getPrefs(): SharedPreferences {
        val context = try {
            MyApplication.instance
        } catch (e: Exception) {
            Log.e("UserManager", "獲取Application實例失敗: ${e.message}")
            return MyApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 初始化使用者狀態
     * 當應用啟動時自動檢查登錄狀態
     */
    fun initialize() {
        // 檢查是否有保存的用戶資訊，即使沒有標記登錄狀態
        val prefs = getPrefs()
        val username = prefs.getString(KEY_CURRENT_USERNAME, null)
        
        if (username != null && username.isNotEmpty()) {
            // 如果有儲存使用者名稱，將登錄狀態設為真
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            Log.d("UserManager", "啟動時發現使用者資訊，自動恢復登錄狀態: $username")
        } else if (hasRememberedCredentials()) {
            // 如果沒有登錄資訊但有保存的登錄憑證，嘗試自動登錄
            val rememberedUsername = getSavedUsername()
            val password = getSavedPassword()
            if (rememberedUsername.isNotEmpty() && password.isNotEmpty()) {
                Log.d("UserManager", "嘗試使用已儲存的憑證自動登錄: $rememberedUsername")
                login(rememberedUsername, password, true)
            }
        } else {
            Log.d("UserManager", "無使用者登錄資訊，需要重新登錄")
        }
    }
    
    /**
     * 驗證登錄凭證
     * @param username 用戶名
     * @param password 密碼
     * @param rememberMe 是否記住登錄憑證
     * @return 如果憑證有效返回true，否則返回false
     */
    fun login(username: String, password: String, rememberMe: Boolean = false): Boolean {
        // 檢查是否為預設管理員帳號
        val isAdminAccount = username == DEFAULT_ADMIN_USERNAME && password == DEFAULT_ADMIN_PASSWORD
        
        if (isAdminAccount) {
            // 管理員登錄成功，保存登錄狀態
            saveLoginState(username, DEFAULT_ADMIN_EMAIL)
            
            // 如果選擇記住我，保存管理員登錄憑證
            if (rememberMe) {
                saveRememberMeCredentials(username, password)
            } else {
                clearRememberMeCredentials()
            }
            
            Log.d("UserManager", "管理員登錄成功: $username, 記住我: $rememberMe")
            return true
        }
        
        // 從SQLite數據庫驗證用戶
        val isValidUser = database.validateUser(username, password)
        
        if (isValidUser) {
            // 從數據庫獲取用戶郵箱
            val email = database.getUserEmail(username)
            // 保存登錄狀態
            saveLoginState(username, email)
            
            // 如果選擇記住我，保存登錄憑證
            if (rememberMe) {
                saveRememberMeCredentials(username, password)
            } else {
                clearRememberMeCredentials()
            }
            
            Log.d("UserManager", "一般用戶登錄成功: $username, 記住我: $rememberMe")
        }
        
        return isValidUser
    }
    
    /**
     * 保存「記住我」的登錄憑證
     * @param username 用戶名
     * @param password 密碼
     */
    private fun saveRememberMeCredentials(username: String, password: String) {
        // 注意：實際應用中應該加密儲存密碼
        getPrefs().edit()
            .putBoolean(KEY_REMEMBER_ME, true)
            .putString(KEY_SAVED_USERNAME, username)
            .putString(KEY_SAVED_PASSWORD, password)
            .apply()
        
        Log.d("UserManager", "已保存登錄憑證：$username")
    }
    
    /**
     * 清除「記住我」的登錄憑證
     */
    private fun clearRememberMeCredentials() {
        getPrefs().edit()
            .putBoolean(KEY_REMEMBER_ME, false)
            .remove(KEY_SAVED_USERNAME)
            .remove(KEY_SAVED_PASSWORD)
            .apply()
        
        Log.d("UserManager", "已清除登錄憑證")
    }
    
    /**
     * 檢查是否已保存「記住我」的登錄憑證
     * @return 如果已保存返回true，否則返回false
     */
    fun hasRememberedCredentials(): Boolean {
        return getPrefs().getBoolean(KEY_REMEMBER_ME, false)
    }
    
    /**
     * 獲取已保存的用戶名
     * @return 保存的用戶名，如果未保存則返回空字符串
     */
    fun getSavedUsername(): String {
        return getPrefs().getString(KEY_SAVED_USERNAME, "") ?: ""
    }
    
    /**
     * 獲取已保存的密碼
     * @return 保存的密碼，如果未保存則返回空字符串
     */
    fun getSavedPassword(): String {
        // 注意：實際應用中應該解密存儲的密碼
        return getPrefs().getString(KEY_SAVED_PASSWORD, "") ?: ""
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
        try {
            // 先從資料庫中取得該用戶的個人資訊
            val userProfile = getUserProfile(username)
            
            val editor = getPrefs().edit()
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.putString(KEY_CURRENT_USERNAME, username)
            editor.putString(KEY_CURRENT_EMAIL, email)
            // 即使值为null或空字符串也要保存，确保空值能正确覆盖旧值
            editor.putString(KEY_CURRENT_CHINESE_NAME, userProfile?.chineseName ?: "")
            editor.putString(KEY_CURRENT_ENGLISH_NAME, userProfile?.englishName ?: "")
            editor.putInt(KEY_CURRENT_ACCOUNT_TYPE, userProfile?.accountType ?: 1)
            editor.putString(KEY_CURRENT_BIRTHDAY, userProfile?.birthday ?: "")
            editor.putInt(KEY_CURRENT_GENDER, userProfile?.gender ?: 0)
            editor.putString(KEY_CURRENT_PHONE, userProfile?.phoneNumber ?: "")
            editor.putString(KEY_CURRENT_ADDRESS, userProfile?.address ?: "")
            editor.putString(KEY_CURRENT_PROFILE_PHOTO, userProfile?.profilePhotoUri ?: "")
            // 儲存最後登錄時間和自動登出設定
            editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            // 預設為不自動登出
            editor.putBoolean(KEY_AUTO_LOGOUT, false)
            
            // 使用commit而不是apply確保立即儲存
            val result = editor.commit()
            
            Log.d("UserManager", "登錄狀態儲存${if (result) "成功" else "失敗"}: $username")
        } catch (e: Exception) {
            Log.e("UserManager", "儲存登錄狀態時發生錯誤: ${e.message}")
        }
    }
    
    /**
     * 檢查用戶是否已登錄
     * @return 如果用戶已登錄返回true，否則返回false
     */
    fun isLoggedIn(): Boolean {
        try {
            val prefs = getPrefs()
            
            // 取得用戶名是判斷登錄狀態的主要依據
            val username = prefs.getString(KEY_CURRENT_USERNAME, null)
            
            // 如果有用戶名，就視為登錄狀態
            if (username != null && username.isNotEmpty()) {
                // 確保登錄狀態標記為真
                if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                    prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).commit() // 立即儲存
                    Log.d("UserManager", "登錄狀態標記不一致，已自動修正為已登錄")
                }
                return true
            }
            
            // 如果沒有用戶名但登錄狀態為真，則清除登錄狀態
            if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).commit()
                Log.d("UserManager", "登錄狀態標記不一致，已自動修正為未登錄")
            }
            
            return false
        } catch (e: Exception) {
            Log.e("UserManager", "檢查登錄狀態時發生錯誤: ${e.message}")
            return false
        }
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
     * 設置是否在應用關閉時自動登出
     * @param autoLogout 是否自動登出
     */
    fun setAutoLogout(autoLogout: Boolean) {
        getPrefs().edit()
            .putBoolean(KEY_AUTO_LOGOUT, autoLogout)
            .apply()
        
        Log.d("UserManager", "設置自動登出: $autoLogout")
    }
    
    /**
     * 檢查是否設置了自動登出
     * @return 是否自動登出
     */
    fun isAutoLogoutEnabled(): Boolean {
        return getPrefs().getBoolean(KEY_AUTO_LOGOUT, false)
    }
    
    /**
     * 登出用戶
     * @param clearRememberMe 是否同時清除「記住我」的憑證，默認不清除
     */
    fun logout(clearRememberMe: Boolean = false) {
        try {
            val editor = getPrefs().edit()
            editor.putBoolean(KEY_IS_LOGGED_IN, false)
            editor.remove(KEY_CURRENT_USERNAME)
            editor.remove(KEY_CURRENT_EMAIL)
            editor.remove(KEY_CURRENT_CHINESE_NAME)
            editor.remove(KEY_CURRENT_ENGLISH_NAME)
            editor.remove(KEY_CURRENT_ACCOUNT_TYPE)
            editor.remove(KEY_CURRENT_BIRTHDAY)
            editor.remove(KEY_CURRENT_GENDER)
            editor.remove(KEY_CURRENT_PHONE)
            editor.remove(KEY_CURRENT_ADDRESS)
            editor.remove(KEY_CURRENT_PROFILE_PHOTO)
            editor.remove(KEY_LAST_LOGIN_TIME)
            
            // 如果需要，同時清除記住我功能的憑證
            if (clearRememberMe) {
                editor.putBoolean(KEY_REMEMBER_ME, false)
                editor.remove(KEY_SAVED_USERNAME)
                editor.remove(KEY_SAVED_PASSWORD)
                
                Log.d("UserManager", "用戶已登出，已清除記住我憑證")
            } else {
                Log.d("UserManager", "用戶已登出，保留記住我憑證")
            }
            
            // 使用commit而不是apply確保立即儲存
            val result = editor.commit()
            Log.d("UserManager", "登出操作${if (result) "成功" else "失敗"}")
        } catch (e: Exception) {
            Log.e("UserManager", "登出時發生錯誤: ${e.message}")
        }
    }
    
    /**
     * 獨取當前登入用戶的個人資料
     * @return 用戶個人資料對象，如果未登入則返回null
     */
    fun getCurrentUserProfile(): UserProfile? {
        if (!isLoggedIn()) {
            return null
        }
        
        val prefs = getPrefs()
        val username = prefs.getString(KEY_CURRENT_USERNAME, null) ?: return null
        val email = prefs.getString(KEY_CURRENT_EMAIL, null)
        val chineseName = prefs.getString(KEY_CURRENT_CHINESE_NAME, null)
        val englishName = prefs.getString(KEY_CURRENT_ENGLISH_NAME, null)
        val accountType = prefs.getInt(KEY_CURRENT_ACCOUNT_TYPE, ACCOUNT_TYPE_PATIENT)
        val birthday = prefs.getString(KEY_CURRENT_BIRTHDAY, null)
        val gender = prefs.getInt(KEY_CURRENT_GENDER, GENDER_UNSPECIFIED)
        val phone = prefs.getString(KEY_CURRENT_PHONE, null)
        val address = prefs.getString(KEY_CURRENT_ADDRESS, null)
        val profilePhoto = prefs.getString(KEY_CURRENT_PROFILE_PHOTO, null)
        
        return UserProfile(
            username = username,
            chineseName = chineseName,
            englishName = englishName,
            email = email,
            birthday = birthday,
            gender = gender,
            phoneNumber = phone,
            address = address,
            accountType = accountType,
            profilePhotoUri = profilePhoto
        )
    }
    
    /**
     * 獨取指定用戶的個人資料
     * @param username 用戶名
     * @return 用戶個人資料對象，如果用戶不存在則返回null
     */
    fun getUserProfile(username: String): UserProfile? {
        return database.getUserProfile(username)
    }
    
    /**
     * 更新用戶個人資料
     * @param profile 用戶個人資料對象
     * @return 更新是否成功
     */
    fun updateUserProfile(profile: UserProfile): Boolean {
        val result = database.updateUserProfile(profile)
        
        // 如果是当前登入用戶，同時更新SharedPreferences
        if (result && getCurrentUsername() == profile.username) {
            getPrefs().edit()
                .putString(KEY_CURRENT_EMAIL, profile.email ?: "")
                .putInt(KEY_CURRENT_ACCOUNT_TYPE, profile.accountType)
                .putString(KEY_CURRENT_BIRTHDAY, profile.birthday ?: "")
                .putInt(KEY_CURRENT_GENDER, profile.gender)
                .putString(KEY_CURRENT_CHINESE_NAME, profile.chineseName ?: "")
                .putString(KEY_CURRENT_ENGLISH_NAME, profile.englishName ?: "")
                .putString(KEY_CURRENT_PHONE, profile.phoneNumber ?: "")
                .putString(KEY_CURRENT_ADDRESS, profile.address ?: "")
                .putString(KEY_CURRENT_PROFILE_PHOTO, profile.profilePhotoUri ?: "")
                .apply()
        }
        
        return result
    }
    
    /**
     * 獲取帳號類型的方法
     * @param accountType 帳號類型
     * @param isChinese 是否返回中文名稱
     * @return 帳號類型名稱字串
     */
    fun getAccountTypeName(accountType: Int, isChinese: Boolean = true): String {
        return when (accountType) {
            ACCOUNT_TYPE_PATIENT -> if (isChinese) "院友" else "Patient"
            ACCOUNT_TYPE_FAMILY -> if (isChinese) "家屬" else "Family"
            ACCOUNT_TYPE_STAFF -> if (isChinese) "員工" else "Staff"
            ACCOUNT_TYPE_ADMIN -> if (isChinese) "管理人員" else "Admin"
            ACCOUNT_TYPE_DEVELOPER -> if (isChinese) "開發人員" else "Developer"
            else -> if (isChinese) "未知" else "Unknown"
        }
    }
    
    /**
     * 獲取性別名稱的方法
     * @param gender 性別代碼
     * @param isChinese 是否返回中文名稱
     * @return 性別名稱字串
     */
    fun getGenderName(gender: Int, isChinese: Boolean = true): String {
        return when (gender) {
            GENDER_MALE -> if (isChinese) "男" else "Male"
            GENDER_FEMALE -> if (isChinese) "女" else "Female"
            GENDER_OTHER -> if (isChinese) "其他" else "Other"
            else -> if (isChinese) "未設定" else "Unspecified"
        }
    }
    
    /**
     * 檢查使用者是否有權限變更指定用戶的帳號類型
     * 只有管理員和開發者可以更改帳號類型，且一般沒有人能提升成開發者
     */
    fun canChangeAccountType(targetUsername: String, newAccountType: Int): Boolean {
        val currentProfile = getCurrentUserProfile() ?: return false
        
        // 如果不是管理員或開發者，無權限修改
        if (currentProfile.accountType < ACCOUNT_TYPE_ADMIN) {
            return false
        }
        
        // 檢查目標用戶的當前帳號類型
        val targetProfile = getUserProfile(targetUsername)
        
        // 開發者帳號無法被修改，且通常只有開發者可以創建開發者帳號
        if (targetProfile?.accountType == ACCOUNT_TYPE_DEVELOPER || 
            (newAccountType == ACCOUNT_TYPE_DEVELOPER && currentProfile.accountType < ACCOUNT_TYPE_DEVELOPER)) {
            return false
        }
        
        // 管理員只能修改到不高於自身的等級
        if (currentProfile.accountType == ACCOUNT_TYPE_ADMIN && newAccountType > ACCOUNT_TYPE_ADMIN) {
            return false
        }
        
        return true
    }
    
    /**
     * 修改用戶密碼
     * @param currentPassword 當前密碼
     * @param newPassword 新密碼
     * @return 修改結果：0=成功，1=用戶不存在或未登入，2=當前密碼錯誤
     */
    fun changePassword(currentPassword: String, newPassword: String): Int {
        // 檢查用戶是否已登入
        val currentUsername = getCurrentUsername() ?: return 1
        
        // 使用資料庫更新密碼
        return database.updatePassword(currentUsername, currentPassword, newPassword)
    }
    
    /**
     * 請求重置密碼
     * @param username 用戶名
     * @param email 電子郵件（用於驗證用戶身份）
     * @return 重置結果：0=成功，1=用戶不存在，2=電子郵件不匹配，3=其他錯誤
     */
    fun requestPasswordReset(username: String, email: String): Int {
        // 檢查用戶是否存在
        if (!isUserExists(username)) {
            Log.d("UserManager", "重置密碼請求失敗：用戶不存在, $username")
            return 1
        }
        
        // 檢查電子郵件是否匹配
        val userEmail = database.getUserEmail(username)
        if (userEmail == null || userEmail.isEmpty() || userEmail != email) {
            Log.d("UserManager", "重置密碼請求失敗：電子郵件不匹配, 提供: $email, 實際: $userEmail")
            return 2
        }
        
        // 生成臨時密碼
        val tempPassword = generateTemporaryPassword()
        
        // 在實際應用中，這裡應該向用戶發送電子郵件
        // 由於應用目前是演示，我們直接更新密碼
        Log.d("UserManager", "生成臨時密碼: $tempPassword，用戶名: $username")
        
        try {
            // 直接重置密碼（跳過當前密碼驗證）
            val result = database.resetPassword(username, tempPassword)
            if (result) {
                Log.d("UserManager", "密碼重置成功: $username")
                return 0
            } else {
                Log.e("UserManager", "密碼重置失敗: $username")
                return 3
            }
        } catch (e: Exception) {
            Log.e("UserManager", "密碼重置過程出錯: ${e.message}", e)
            return 3
        }
    }
    
    // 儲存當前驗證碼
    private var currentVerificationCode: String? = null
    
    /**
     * 驗證用戶電子郵件
     * @param username 用戶名
     * @param email 電子郵件
     * @return 驗證結果：0=成功，1=用戶不存在，2=電子郵件不匹配
     */
    fun verifyUserEmail(username: String, email: String): Int {
        // 檢查用戶是否存在
        if (!isUserExists(username)) {
            Log.d("UserManager", "驗證失敗：用戶不存在, $username")
            return 1
        }
        
        // 獲取用戶的電子郵件
        val userEmail = database.getUserEmail(username)
        Log.d("UserManager", "驗證電子郵件 - 用戶: $username, 提供: $email, 數據庫: $userEmail")
        
        // 為了測試目的，如果用戶存在於數據庫中，就認為驗證成功
        // 這樣任何已註冊的帳戶都可以重置密碼
        if (isUserExists(username)) {
            // 如果是admin用戶且提供的郵箱是預設的測試郵箱，直接通過
            if (username == "admin" && email == "admin@example.com") {
                Log.d("UserManager", "測試帳號驗證成功: $username")
                return 0
            }
            
            // 如果数据库中有此用户的邮箱记录且与提供的邮箱匹配，通过验证
            if (userEmail != null && userEmail.isNotEmpty() && userEmail == email) {
                Log.d("UserManager", "郵箱匹配驗證成功: $username")
                return 0
            }
            
            // 如果此用户在数据库中没有邮箱记录或邮箱为空，也允许通过
            // 这是为了测试和演示目的
            if (userEmail == null || userEmail.isEmpty()) {
                Log.d("UserManager", "用戶無郵箱記錄，但允許重置: $username")
                return 0
            }
        }
        
        // 如果用戶提供的郵箱與數據庫中的不匹配，則驗證失敗
        Log.d("UserManager", "驗證失敗：電子郵件不匹配, 提供: $email, 實際: $userEmail")
        return 2
    }
    
    /**
     * 生成4位驗證碼
     * @return 4位數字驗證碼
     */
    fun generateVerificationCode(): String {
        // 生成4位隨機數字
        val code = (1000..9999).random().toString()
        currentVerificationCode = code
        Log.d("UserManager", "生成驗證碼: $code")
        return code
    }
    
    /**
     * 獲取當前驗證碼
     * @return 當前驗證碼，如果未生成則返回null
     */
    fun getCurrentVerificationCode(): String? {
        return currentVerificationCode
    }
    
    /**
     * 驗證用戶提供的驗證碼
     * @param code 用戶提供的驗證碼
     * @return 驗證結果：true=成功，false=失敗
     */
    fun verifyCode(code: String): Boolean {
        val result = code == currentVerificationCode
        Log.d("UserManager", "驗證碼驗證${if (result) "成功" else "失敗"}: 提供: $code, 實際: $currentVerificationCode")
        return result
    }
    
    /**
     * 重設用戶密碼（在驗證碼驗證成功後）
     * @param username 用戶名
     * @param newPassword 新密碼
     * @return 重設結果：true=成功，false=失敗
     */
    fun resetPassword(username: String, newPassword: String): Boolean {
        // 重設密碼
        val result = database.resetPassword(username, newPassword)
        
        if (result) {
            // 清除驗證碼
            currentVerificationCode = null
            Log.d("UserManager", "密碼重設成功: $username")
        } else {
            Log.e("UserManager", "密碼重設失敗: $username")
        }
        
        return result
    }
    
    /**
     * 生成臨時密碼
     * @return 隨機生成的8位臨時密碼
     */
    private fun generateTemporaryPassword(): String {
        // 定義密碼字符集
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        
        // 生成8位隨機密碼
        return (1..8)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
    
    /**
     * 驗證用戶密碼
     * @param username 用戶名
     * @param password 密碼
     * @return 密碼是否正確
     */
    fun verifyPassword(username: String, password: String): Boolean {
        // 檢查是否為預設管理員帳號
        if (username == DEFAULT_ADMIN_USERNAME) {
            return password == DEFAULT_ADMIN_PASSWORD
        }
        
        // 從數據庫驗證用戶密碼
        return database.validateUser(username, password)
    }
} 