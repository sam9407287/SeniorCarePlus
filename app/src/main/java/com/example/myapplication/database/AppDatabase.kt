package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication.models.UserProfile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 應用程序數據庫助手類，管理SQLite數據庫操作
 */
class AppDatabase private constructor(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "senior_care.db"
        private const val DATABASE_VERSION = 3 // 升級版本號以支援中英文姓名
        
        // 用戶表
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_CREATED_AT = "created_at"
        
        // 新增個人資訊欄位
        private const val COLUMN_CHINESE_NAME = "chinese_name"
        private const val COLUMN_ENGLISH_NAME = "english_name"
        private const val COLUMN_BIRTHDAY = "birthday"
        private const val COLUMN_GENDER = "gender" // 0=未設定，1=男，2=女，3=其他
        private const val COLUMN_PHONE = "phone_number"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_ACCOUNT_TYPE = "account_type" // 1=院友，2=家屬，3=員工，4=管理人員，5=開發人員
        private const val COLUMN_USER_PROFILE_PHOTO = "profile_photo"
        
        // 體溫數據表
        private const val TABLE_TEMPERATURE = "temperature_records"
        private const val COLUMN_TEMP_ID = "id"
        private const val COLUMN_TEMP_USERNAME = "username"  // 關聯到用戶名
        private const val COLUMN_TEMP_VALUE = "temperature"
        private const val COLUMN_TEMP_TIMESTAMP = "timestamp"
        private const val COLUMN_TEMP_IS_ABNORMAL = "is_abnormal"
        
        // 單例模式
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        // 創建用戶表
        val createUserTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_EMAIL TEXT,
                $COLUMN_CHINESE_NAME TEXT,
                $COLUMN_ENGLISH_NAME TEXT,
                $COLUMN_BIRTHDAY TEXT,
                $COLUMN_GENDER INTEGER DEFAULT 0,
                $COLUMN_PHONE TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_ACCOUNT_TYPE INTEGER DEFAULT 1,
                $COLUMN_USER_PROFILE_PHOTO TEXT,
                $COLUMN_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        
        // 創建體溫數據表
        val createTemperatureTableQuery = """
            CREATE TABLE $TABLE_TEMPERATURE (
                $COLUMN_TEMP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEMP_USERNAME TEXT NOT NULL,
                $COLUMN_TEMP_VALUE REAL NOT NULL,
                $COLUMN_TEMP_TIMESTAMP TEXT NOT NULL,
                $COLUMN_TEMP_IS_ABNORMAL INTEGER DEFAULT 0,
                FOREIGN KEY ($COLUMN_TEMP_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
            )
        """.trimIndent()
        
        db.execSQL(createUserTableQuery)
        db.execSQL(createTemperatureTableQuery)
        Log.d("AppDatabase", "數據庫表已創建: $TABLE_USERS, $TABLE_TEMPERATURE")
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 版本1到版本2的升級 - 增加個人資訊欄位
            try {
                // 先檢查欄位是否已經存在
                val cursor = db.rawQuery("PRAGMA table_info($TABLE_USERS)", null)
                val columnNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    columnNames.add(columnName)
                }
                cursor.close()
                
                // 依序新增缺少的欄位
                if (!columnNames.contains(COLUMN_BIRTHDAY)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_BIRTHDAY TEXT")
                }
                if (!columnNames.contains(COLUMN_GENDER)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_GENDER INTEGER DEFAULT 0")
                }
                if (!columnNames.contains(COLUMN_PHONE)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_PHONE TEXT")
                }
                if (!columnNames.contains(COLUMN_ADDRESS)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_ADDRESS TEXT")
                }
                if (!columnNames.contains(COLUMN_ACCOUNT_TYPE)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_ACCOUNT_TYPE INTEGER DEFAULT 1")
                }
                if (!columnNames.contains(COLUMN_USER_PROFILE_PHOTO)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_PROFILE_PHOTO TEXT")
                }
                
                Log.d("AppDatabase", "數據庫已從版本1升級到版本2")
            } catch (e: Exception) {
                // 如果發生錯誤，則使用回退策略 - 重建資料表
                Log.e("AppDatabase", "資料庫升級失敗，執行回退方案: ${e.message}")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_TEMPERATURE")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
                onCreate(db)
            }
        }
        
        if (oldVersion < 3) {
            // 版本2到版本3的升級 - 增加中英文姓名欄位
            try {
                // 先檢查欄位是否已經存在
                val cursor = db.rawQuery("PRAGMA table_info($TABLE_USERS)", null)
                val columnNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    columnNames.add(columnName)
                }
                cursor.close()
                
                // 依序新增缺少的欄位
                if (!columnNames.contains(COLUMN_CHINESE_NAME)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_CHINESE_NAME TEXT")
                }
                if (!columnNames.contains(COLUMN_ENGLISH_NAME)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_ENGLISH_NAME TEXT")
                }
                
                Log.d("AppDatabase", "數據庫已從版本2升級到版本3")
            } catch (e: Exception) {
                // 如果發生錯誤，則使用回退策略 - 重建資料表
                Log.e("AppDatabase", "資料庫升級到版本3失敗，執行回退方案: ${e.message}")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_TEMPERATURE")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
                onCreate(db)
            }
        }
        
        Log.d("AppDatabase", "資料庫升級成功: 從版本 $oldVersion 到 $newVersion")
    }
    
    /**
     * 註冊新用戶
     * @return 如果註冊成功返回true，否則返回false
     */
    fun registerUser(username: String, password: String, email: String?): Boolean {
        // 檢查用戶名是否已存在
        if (isUserExists(username)) {
            Log.d("AppDatabase", "註冊失敗，用戶名已存在: $username")
            return false
        }
        
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            email?.let { put(COLUMN_EMAIL, it) }
        }
        
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        
        val success = result != -1L
        Log.d("AppDatabase", "用戶註冊${if (success) "成功" else "失敗"}: $username")
        return success
    }
    
    /**
     * 檢查用戶名是否已存在
     */
    fun isUserExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        
        return exists
    }
    
    /**
     * 驗證用戶登錄
     */
    fun validateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_PASSWORD),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        
        var valid = false
        if (cursor.moveToFirst()) {
            val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            valid = storedPassword == password
        }
        
        cursor.close()
        db.close()
        
        Log.d("AppDatabase", "用戶認證${if (valid) "成功" else "失敗"}: $username")
        return valid
    }
    
    /**
     * 獲取用戶郵箱
     */
    fun getUserEmail(username: String): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_EMAIL),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        
        var email: String? = null
        if (cursor.moveToFirst()) {
            val emailIndex = cursor.getColumnIndex(COLUMN_EMAIL)
            if (emailIndex != -1) {
                email = cursor.getString(emailIndex)
            }
        }
        
        cursor.close()
        db.close()
        
        return email
    }
    
    /**
     * 獨取用戶的個人資料
     * @param username 用戶名
     * @return 用戶的個人資料對象，如果用戶不存在則返回null
     */
    fun getUserProfile(username: String): UserProfile? {
        val db = this.readableDatabase
        
        // 指定要查詢的欄位
        val columns = arrayOf(
            COLUMN_USERNAME, 
            COLUMN_EMAIL, 
            COLUMN_CHINESE_NAME,
            COLUMN_ENGLISH_NAME,
            COLUMN_BIRTHDAY, 
            COLUMN_GENDER, 
            COLUMN_PHONE, 
            COLUMN_ADDRESS, 
            COLUMN_ACCOUNT_TYPE, 
            COLUMN_USER_PROFILE_PHOTO
        )
        
        val cursor = db.query(
            TABLE_USERS,
            columns,
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        
        var userProfile: UserProfile? = null
        if (cursor.moveToFirst()) {
            // 獨取各欄位的值
            val email = getStringOrNull(cursor, COLUMN_EMAIL)
            val chineseName = getStringOrNull(cursor, COLUMN_CHINESE_NAME)
            val englishName = getStringOrNull(cursor, COLUMN_ENGLISH_NAME)
            val birthday = getStringOrNull(cursor, COLUMN_BIRTHDAY)
            val gender = getIntOrDefault(cursor, COLUMN_GENDER, 0)
            val phone = getStringOrNull(cursor, COLUMN_PHONE)
            val address = getStringOrNull(cursor, COLUMN_ADDRESS)
            val accountType = getIntOrDefault(cursor, COLUMN_ACCOUNT_TYPE, 1)
            val profilePhoto = getStringOrNull(cursor, COLUMN_USER_PROFILE_PHOTO)
            
            userProfile = UserProfile(
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
        
        cursor.close()
        db.close()
        
        return userProfile
    }
    
    /**
     * 更新用戶的個人資料
     * @param profile 用戶的個人資料對象
     * @return 更新是否成功
     */
    fun updateUserProfile(profile: UserProfile): Boolean {
        val db = this.writableDatabase
        
        // 初始化內容值
        val values = ContentValues().apply {
            // 郵箱可以為空，但如果有則更新
            profile.email?.let { put(COLUMN_EMAIL, it) }
            profile.chineseName?.let { put(COLUMN_CHINESE_NAME, it) }
            profile.englishName?.let { put(COLUMN_ENGLISH_NAME, it) }
            profile.birthday?.let { put(COLUMN_BIRTHDAY, it) }
            put(COLUMN_GENDER, profile.gender)
            profile.phoneNumber?.let { put(COLUMN_PHONE, it) }
            profile.address?.let { put(COLUMN_ADDRESS, it) }
            put(COLUMN_ACCOUNT_TYPE, profile.accountType)
            profile.profilePhotoUri?.let { put(COLUMN_USER_PROFILE_PHOTO, it) }
        }
        
        // 更新用戶資料
        val rowsAffected = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USERNAME = ?",
            arrayOf(profile.username)
        )
        
        db.close()
        
        val success = rowsAffected > 0
        if (success) {
            Log.d("AppDatabase", "用戶資料更新成功: ${profile.username}")
        } else {
            Log.d("AppDatabase", "用戶資料更新失敗: ${profile.username}")
        }
        
        return success
    }
    
    // 幫助方法: 獨取指定欄位的字串值，如果是null則返回null
    private fun getStringOrNull(cursor: Cursor, columnName: String): String? {
        val columnIndex = cursor.getColumnIndex(columnName)
        if (columnIndex == -1 || cursor.isNull(columnIndex)) {
            return null
        }
        return cursor.getString(columnIndex)
    }
    
    // 幫助方法: 獨取指定欄位的整數值，如果是null則返回預設值
    private fun getIntOrDefault(cursor: Cursor, columnName: String, defaultValue: Int): Int {
        val columnIndex = cursor.getColumnIndex(columnName)
        if (columnIndex == -1 || cursor.isNull(columnIndex)) {
            return defaultValue
        }
        return cursor.getInt(columnIndex)
    }
    
    /**
     * 保存體溫記錄
     * @return 新記錄的ID，如果保存失敗則返回-1
     */
    fun saveTemperatureRecord(username: String, temperature: Float, timestamp: LocalDateTime): Long {
        val db = this.writableDatabase
        val isAbnormal = temperature > 37.5f || temperature < 36.0f
        
        val values = ContentValues().apply {
            put(COLUMN_TEMP_USERNAME, username)
            put(COLUMN_TEMP_VALUE, temperature)
            put(COLUMN_TEMP_TIMESTAMP, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            put(COLUMN_TEMP_IS_ABNORMAL, if (isAbnormal) 1 else 0)
        }
        
        val result = db.insert(TABLE_TEMPERATURE, null, values)
        db.close()
        
        if (result != -1L) {
            Log.d("AppDatabase", "體溫記錄保存成功: ID=$result, 用戶=$username, 溫度=$temperature")
        } else {
            Log.e("AppDatabase", "體溫記錄保存失敗: 用戶=$username, 溫度=$temperature")
        }
        
        return result
    }
    
    /**
     * 獲取用戶的體溫記錄
     * @param username 用戶名
     * @param limit 最多返回的記錄數，默認為100
     * @return 體溫記錄列表，按時間降序排列
     */
    fun getTemperatureRecords(username: String, limit: Int = 100): List<TemperatureRecord> {
        val records = mutableListOf<TemperatureRecord>()
        val db = this.readableDatabase
        
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                TABLE_TEMPERATURE,
                arrayOf(COLUMN_TEMP_ID, COLUMN_TEMP_VALUE, COLUMN_TEMP_TIMESTAMP, COLUMN_TEMP_IS_ABNORMAL),
                "$COLUMN_TEMP_USERNAME = ?",
                arrayOf(username),
                null,
                null,
                "$COLUMN_TEMP_TIMESTAMP DESC",
                limit.toString()
            )
            
            val idIndex = cursor.getColumnIndex(COLUMN_TEMP_ID)
            val valueIndex = cursor.getColumnIndex(COLUMN_TEMP_VALUE)
            val timestampIndex = cursor.getColumnIndex(COLUMN_TEMP_TIMESTAMP)
            val isAbnormalIndex = cursor.getColumnIndex(COLUMN_TEMP_IS_ABNORMAL)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val value = cursor.getFloat(valueIndex)
                val timestampStr = cursor.getString(timestampIndex)
                val isAbnormal = cursor.getInt(isAbnormalIndex) == 1
                
                val timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                
                records.add(
                    TemperatureRecord(
                        id = id,
                        username = username,
                        temperature = value,
                        timestamp = timestamp,
                        isAbnormal = isAbnormal
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AppDatabase", "獲取體溫記錄時出錯: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        
        Log.d("AppDatabase", "獲取到${records.size}條體溫記錄，用戶=$username")
        return records
    }
    
    /**
     * 刪除用戶的所有體溫記錄
     * @return 被刪除的記錄數
     */
    fun deleteAllTemperatureRecords(username: String): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_TEMPERATURE,
            "$COLUMN_TEMP_USERNAME = ?",
            arrayOf(username)
        )
        db.close()
        
        Log.d("AppDatabase", "刪除了${result}條體溫記錄，用戶=$username")
        return result
    }
    
    /**
     * 體溫記錄數據類
     */
    data class TemperatureRecord(
        val id: Long,
        val username: String,
        val temperature: Float,
        val timestamp: LocalDateTime,
        val isAbnormal: Boolean
    )
} 