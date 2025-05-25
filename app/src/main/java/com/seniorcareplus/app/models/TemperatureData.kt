package com.seniorcareplus.app.models

// Room imports commented out until dependencies are properly added
// import androidx.room.Entity
// import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 體溫數據模型，用於存儲和顯示體溫信息
 */
// Removed Room annotations temporarily
// @Entity(tableName = "temperature_records")
data class TemperatureData(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: String,
    val patientName: String,
    val temperature: Float,
    val roomTemperature: Float,
    val timestamp: String,
    val isAbnormal: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 將時間戳字符串轉換為LocalDateTime對象
     */
    fun getLocalDateTime(): LocalDateTime {
        return try {
            // 嘗試解析MQTT時間格式 (例如：2025-140 10:51:01.30)
            val formatter = DateTimeFormatter.ofPattern("yyyy-DDD HH:mm:ss.SS")
            LocalDateTime.parse(timestamp, formatter)
        } catch (e: Exception) {
            try {
                // 備用格式 (例如：2025-05-20 10:51:01)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                LocalDateTime.parse(timestamp, formatter)
            } catch (e: Exception) {
                // 如果無法解析，返回當前時間
                LocalDateTime.now()
            }
        }
    }
    
    /**
     * 格式化顯示用的時間
     */
    fun getFormattedTime(): String {
        val dateTime = getLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        return dateTime.format(formatter)
    }
    
    /**
     * 判斷體溫是否異常
     */
    fun getTemperatureStatus(): TemperatureStatus {
        return when {
            temperature > 37.5f -> TemperatureStatus.HIGH
            temperature < 36.0f -> TemperatureStatus.LOW
            else -> TemperatureStatus.NORMAL
        }
    }
}

/**
 * 體溫狀態枚舉
 */
enum class TemperatureStatus {
    NORMAL,  // 正常
    HIGH,    // 高溫
    LOW      // 低溫
}

/**
 * 體溫數據組，用於存儲特定患者的體溫記錄
 */
data class PatientTemperatureGroup(
    val patientId: String,
    val patientName: String,
    val records: List<TemperatureData>,
    val latestTemperature: Float? = records.maxByOrNull { it.getLocalDateTime() }?.temperature,
    val latestTimestamp: String? = records.maxByOrNull { it.getLocalDateTime() }?.timestamp,
    val hasAbnormalRecords: Boolean = records.any { it.isAbnormal }
)
