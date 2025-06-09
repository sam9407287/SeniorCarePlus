package com.seniorcareplus.app.mqtt

/**
 * MQTT相關常數
 */
object MqttConstants {
    // 遠端MQTT服務器配置 - 來自HTML設定文件
    // 使用WebSocket協議而非標準MQTT協議 (與HTML頁面一致)
    const val MQTT_REMOTE_SERVER_URI = "wss://067ec32ef1344d3bb20c4e53abdde99a.s1.eu.hivemq.cloud:8884/mqtt"
    const val MQTT_REMOTE_USER = "testweb1"
    const val MQTT_REMOTE_PASSWORD = "Aa000000"
    const val MQTT_REMOTE_TOPIC = "UWB/GW16B8_Loca"
    
    // 本地MQTT服務器 - 保留原有配置作為備用
    const val MQTT_LOCAL_SERVER_URI = "tcp://10.0.2.2:1883"
    
    // 預設使用遠端配置
    var MQTT_SERVER_URI = MQTT_REMOTE_SERVER_URI
    var USE_REMOTE_SERVER = true
    
    const val MQTT_CLIENT_ID_PREFIX = "SeniorCarePlusApp_"
    
    // 生成客戶端ID（非const，因為包含運行時值）
    fun generateClientId(): String {
        return MQTT_CLIENT_ID_PREFIX + System.currentTimeMillis()
    }
    
    // 主題
    const val TOPIC_ALL = "#"                        // 訂閱所有主題（測試用，生產環境應避免）
    const val TOPIC_LOCATION = "GW17F5_Loca"         // 位置數據主題
    const val TOPIC_LOCATION_ALT = "location"        // 備用位置數據主題
    const val TOPIC_LOCATION_ALT2 = "Loca"           // 另一個備用位置主題
    const val TOPIC_LOCATION_RAW = "raw/location"    // 原始位置數據主題
    const val TOPIC_HEALTH = "GW17F5_Health"         // 健康數據主題
    const val TOPIC_HEALTH_DATA = "health/data"      // 模擬器健康數據主題
    const val TOPIC_MESSAGE = "GW17F5_Message"       // 消息主題
    const val TOPIC_ACK = "GW17F5_Ack"               // 確認消息主題
    const val TOPIC_GATEWAY = "UWB_Gateway"          // 網關主題
    
    // QOS級別
    const val QOS_0 = 0 // 最多發送一次，不確認送達
    const val QOS_1 = 1 // 至少發送一次，確認送達
    const val QOS_2 = 2 // 只發送一次，確認送達
    
    // 連接選項
    const val CONNECTION_TIMEOUT = 30  // 增加連接超時時間，遠端服務器可能需要更長時間
    const val KEEP_ALIVE_INTERVAL = 60
    const val RECONNECT_DELAY = 5000L  // 重新連接延遲（毫秒）
    
    // 訊息類型
    const val CONTENT_TYPE_LOCATION = "location"
    const val CONTENT_TYPE_HEARTBEAT = "heartbeat"
    const val CONTENT_TYPE_HEALTH = "300B"
    const val CONTENT_TYPE_DIAPER = "diaper DV1"
    const val CONTENT_TYPE_TEMPERATURE = "temperature"  // 體溫數據類型
}
