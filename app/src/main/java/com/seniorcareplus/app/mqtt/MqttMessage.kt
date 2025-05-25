package com.seniorcareplus.app.mqtt

import com.google.gson.annotations.SerializedName

/**
 * MQTT消息基類，所有MQTT消息都繼承自此類
 */
sealed class MqttMessage {
    @SerializedName("content")
    var content: String = ""

    @SerializedName("gateway id")
    var gatewayId: Long = 0
}

/**
 * 位置數據消息
 */
data class LocationMessage(
    @SerializedName("node")
    val node: String = "",
    
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("position")
    val position: Position? = null,
    
    @SerializedName("time")
    val time: String = "",
    
    @SerializedName("battery level")
    val batteryLevel: Int = 0,
    
    @SerializedName("serial no")
    val serialNo: Int = 0
) : MqttMessage()

/**
 * 位置座標數據
 */
data class Position(
    @SerializedName("x")
    val x: Float = 0f,
    
    @SerializedName("y")
    val y: Float = 0f,
    
    @SerializedName("z")
    val z: Float = 0f,
    
    @SerializedName("quality")
    val quality: Int = 0
)

/**
 * 健康數據消息
 */
data class HealthMessage(
    @SerializedName("MAC")
    val mac: String = "",
    
    @SerializedName("id")
    val id: String? = null,  // 添加ID字段支持識別用戶
    
    @SerializedName("name")
    val name: String? = null,  // 添加名稱字段
    
    @SerializedName("SOS")
    val sos: Int = 0,
    
    @SerializedName("hr")
    val heartRate: Int = 0,
    
    @SerializedName("SpO2")
    val spo2: Int = 0,
    
    @SerializedName("bp syst")
    val bpSystolic: Int = 0,
    
    @SerializedName("bp diast")
    val bpDiastolic: Int = 0,
    
    @SerializedName("skin temp")
    val skinTemp: Float = 0f,
    
    @SerializedName("room temp")
    val roomTemp: Float = 0f,
    
    @SerializedName("steps")
    val steps: Int = 0,
    
    @SerializedName("sleep time")
    val sleepTime: String = "",
    
    @SerializedName("wake time")
    val wakeTime: String = "",
    
    @SerializedName("light sleep (min)")
    val lightSleepMin: Int = 0,
    
    @SerializedName("deep sleep (min)")
    val deepSleepMin: Int = 0,
    
    @SerializedName("time")
    val time: String? = null,  // 添加時間字段
    
    @SerializedName("move")
    val move: Int = 0,
    
    @SerializedName("wear")
    val wear: Int = 0,
    
    @SerializedName("battery level")
    val batteryLevel: Int = 0,
    
    @SerializedName("serial no")
    val serialNo: Int = 0
) : MqttMessage()

/**
 * 體溫數據消息 - 專用於體溫監測的消息格式
 */
data class TemperatureMessage(
    @SerializedName("node")
    val node: String = "",
    
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("temperature")
    val temperature: TemperatureData = TemperatureData(),
    
    @SerializedName("time")
    val time: String = "",
    
    @SerializedName("serial no")
    val serialNo: Int = 0
) : MqttMessage()

/**
 * 體溫數據結構
 */
data class TemperatureData(
    @SerializedName("value")
    val value: Float = 0f,
    
    @SerializedName("unit")
    val unit: String = "celsius",
    
    @SerializedName("is_abnormal")
    val isAbnormal: Boolean = false,
    
    @SerializedName("room_temp")
    val roomTemp: Float = 0f
)

/**
 * 尿布狀態消息
 */
data class DiaperMessage(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("temp")
    val temp: Float = 0f,
    
    @SerializedName("humi")
    val humidity: Float = 0f,
    
    @SerializedName("unit")
    val unit: String = "",
    
    @SerializedName("is_success")
    val isSuccess: Boolean = false,
    
    @SerializedName("button")
    val button: Int = 0,
    
    @SerializedName("mssg idx")
    val messageIndex: Int = 0,
    
    @SerializedName("ack")
    val ack: Int = 0,
    
    @SerializedName("battery level")
    val batteryLevel: Int = 0,
    
    @SerializedName("serial no")
    val serialNo: Int = 0
) : MqttMessage()
