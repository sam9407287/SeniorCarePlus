package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 應用程序數據庫助手類，管理SQLite數據庫操作
 */
class AppDatabase private constructor(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "senior_care.db"
        private const val DATABASE_VERSION = 1
        
        // 用戶表
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_CREATED_AT = "created_at"
        
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
        // 簡單的升級策略 - 刪除舊表並創建新表
        // 注意: 在實際應用中應當進行數據遷移而非直接刪除
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TEMPERATURE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
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