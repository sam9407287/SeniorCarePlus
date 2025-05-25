package com.seniorcareplus.app.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * MQTT心率數據模型，用於解析從MQTT接收的JSON數據
 */
data class MqttHealthData(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("id")
    val userId: String,
    
    @SerializedName("name")
    val userName: String,
    
    @SerializedName("gateway_id")
    val gatewayId: String,
    
    @SerializedName("heart_rate")
    val heartRate: Int,
    
    @SerializedName("temperature")
    val temperature: Double,
    
    @SerializedName("time")
    val timeString: String,
    
    @SerializedName("timestamp")
    val timestampMillis: Long
) {
    /**
     * 轉換為HeartRateData對象
     */
    fun toHeartRateData(): HeartRateData {
        return HeartRateData(
            patientId = userId,
            patientName = userName,
            heartRate = heartRate,
            timestamp = timeString,
            isAbnormal = heartRate > 100 || heartRate < 60,
            createdAt = timestampMillis
        )
    }
}

/**
 * 心率數據模型，用於存儲和顯示心率信息
 */
data class HeartRateData(
    val id: Long = 0,
    val patientId: String,
    val patientName: String,
    val heartRate: Int,
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
     * 判斷心率是否異常
     */
    fun getHeartRateStatus(): HeartRateStatus {
        return when {
            heartRate > 100 -> HeartRateStatus.HIGH
            heartRate < 60 -> HeartRateStatus.LOW
            else -> HeartRateStatus.NORMAL
        }
    }
    
    /**
     * 獲取心率狀態的中文描述
     */
    fun getStatusDescription(isChineseLanguage: Boolean = true): String {
        return when (getHeartRateStatus()) {
            HeartRateStatus.HIGH -> if (isChineseLanguage) "心率過高" else "High Heart Rate"
            HeartRateStatus.LOW -> if (isChineseLanguage) "心率過低" else "Low Heart Rate"
            HeartRateStatus.NORMAL -> if (isChineseLanguage) "正常" else "Normal"
        }
    }
}

/**
 * 心率狀態枚舉
 */
enum class HeartRateStatus {
    NORMAL,  // 正常 (60-100)
    HIGH,    // 心率過高 (>100)
    LOW      // 心率過低 (<60)
}

/**
 * 心率數據組，用於存儲特定患者的心率記錄
 */
data class PatientHeartRateGroup(
    val patientId: String,
    val patientName: String,
    val records: List<HeartRateData>,
    val latestHeartRate: Int? = records.maxByOrNull { it.getLocalDateTime() }?.heartRate,
    val latestTimestamp: String? = records.maxByOrNull { it.getLocalDateTime() }?.timestamp,
    val hasAbnormalRecords: Boolean = records.any { it.isAbnormal }
) 