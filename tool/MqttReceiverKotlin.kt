import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.json.JSONObject
import java.io.File
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

/**
 * MQTT接收器 - Kotlin版本
 *
 * 此腳本用於接收MQTT消息，並顯示接收到的數據。
 * 它可以用來驗證MQTT發送器發出的消息能否被正確接收。
 *
 * 使用方法:
 * 1. 安裝所需依賴庫:
 *    - org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5
 *    - org.json:json:20211205
 *
 * 2. 編譯並運行該腳本:
 *    kotlinc -cp ".:paho-mqtt-client.jar:json.jar" MqttReceiverKotlin.kt -include-runtime -d MqttReceiverKotlin.jar
 *    java -cp ".:MqttReceiverKotlin.jar:paho-mqtt-client.jar:json.jar" MqttReceiverKotlinKt
 *
 * 3. 或者使用kotlinc-jvm直接運行:
 *    kotlinc-jvm -script MqttReceiverKotlin.kts
 */

// MQTT連接配置
val brokerUrl = "tcp://localhost:1883"
val clientId = "mqtt-receiver-kotlin-${System.currentTimeMillis()}"
val persistence = MemoryPersistence()

// 記錄接收到的消息
var messageCount = 0
val recentMessages = mutableListOf<Map<String, Any>>()
const val MAX_RECENT_MESSAGES = 10

// 格式化時間的工具
val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

// 為了保持程序運行，直到使用者中斷
val latch = CountDownLatch(1)

/**
 * 主函數
 */
fun main() {
    println("MQTT接收器 (Kotlin版本)")
    println("正在連接到MQTT伺服器: $brokerUrl")
    
    try {
        // 創建客戶端
        val client = MqttClient(brokerUrl, clientId, persistence)
        
        // 設置連接選項
        val connOpts = MqttConnectOptions().apply {
            isCleanSession = true
            connectionTimeout = 60
            keepAliveInterval = 60
        }
        
        // 設置回調
        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                println("連接丟失: ${cause?.message}")
            }
            
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                handleMessage(topic, message)
            }
            
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // 不需要處理
            }
        })
        
        // 連接到伺服器
        client.connect(connOpts)
        println("已連接到MQTT伺服器")
        
        // 訂閱主題
        val topics = arrayOf(
            "GW+_Loca",       // 位置數據
            "GW+_Message",    // 消息數據
            "GW+_Health",     // 健康數據
            "GW+_Ack",        // 確認消息
            "UWB_Gateway",    // Gateway主題
            "#"               // 所有主題
        )
        
        topics.forEach { topic ->
            client.subscribe(topic)
            println("已訂閱主題: $topic")
        }
        
        println("接收器已啟動，等待消息...(按Ctrl+C停止)")
        
        // 捕獲Ctrl+C信號
        Runtime.getRuntime().addShutdownHook(Thread {
            println("\n用戶中斷，停止接收器...")
            
            // 顯示接收摘要
            println("\n接收摘要:")
            println("共接收到 $messageCount 條消息")
            if (recentMessages.isNotEmpty()) {
                println("最近 ${recentMessages.size} 條消息:")
                recentMessages.forEachIndexed { index, msg ->
                    println("${index+1}. [${msg["timestamp"]}] 主題: ${msg["topic"]}")
                }
            }
            
            println("接收器已停止")
            
            try {
                client.disconnect()
                println("已斷開MQTT連接")
            } catch (e: Exception) {
                println("斷開連接時發生錯誤: ${e.message}")
            }
            
            latch.countDown()
        })
        
        // 等待直到程序被中斷
        latch.await()
        
    } catch (e: MqttException) {
        println("MQTT錯誤: ${e.message} (${e.reasonCode})")
        exitProcess(1)
    } catch (e: Exception) {
        println("發生錯誤: ${e.message}")
        exitProcess(1)
    }
}

/**
 * 處理接收到的MQTT消息
 */
fun handleMessage(topic: String?, message: MqttMessage?) {
    if (topic == null || message == null) return
    
    // 獲取當前時間戳
    val timestamp = LocalDateTime.now().format(timeFormatter)
    
    // 增加消息計數
    messageCount++
    
    // 消息負載
    val payload = String(message.payload)
    
    // 嘗試解析JSON
    var jsonData: JSONObject? = null
    var parsed = false
    try {
        jsonData = JSONObject(payload)
        parsed = true
    } catch (e: Exception) {
        // 非JSON格式，忽略錯誤
    }
    
    // 記錄消息
    val messageInfo = mapOf(
        "timestamp" to timestamp,
        "topic" to topic,
        "payload" to (if (parsed) jsonData.toString(2) else payload),
        "parsed" to parsed
    )
    
    // 添加到最近消息列表
    recentMessages.add(messageInfo)
    if (recentMessages.size > MAX_RECENT_MESSAGES) {
        recentMessages.removeAt(0)
    }
    
    // 輸出消息摘要
    println("\n[$timestamp] 收到消息 #$messageCount:")
    println("主題: $topic")
    
    // 根據是否成功解析JSON顯示不同的信息
    if (parsed && jsonData != null) {
        // 提取關鍵信息用於簡明顯示
        val content = jsonData.optString("content", "未知內容")
        val nodeType = jsonData.optString("node", "未知節點")
        val nodeId = jsonData.optString("id", "未知ID")
        
        println("內容類型: $content")
        println("節點類型: $nodeType")
        println("節點ID: $nodeId")
        
        // 如果是位置數據，顯示位置信息
        if (content == "location" && jsonData.has("position")) {
            val pos = jsonData.getJSONObject("position")
            println("位置: X=${pos.optDouble("x")}, Y=${pos.optDouble("y")}, Z=${pos.optDouble("z")}, 品質=${pos.optInt("quality")}")
        }
        
        // 如果是健康數據，顯示關鍵健康指標
        else if (content == "300B") {
            println("心率: ${jsonData.optInt("hr", -1)} bpm")
            println("血氧: ${jsonData.optInt("SpO2", -1)}%")
            println("血壓: ${jsonData.optInt("bp syst", -1)}/${jsonData.optInt("bp diast", -1)} mmHg")
            println("體溫: ${jsonData.optDouble("skin temp", -1.0)}°C")
        }
        
        // 如果是尿布數據，顯示關鍵指標
        else if (content.contains("diaper")) {
            println("溫度: ${jsonData.optDouble("temp", -1.0)}°C")
            println("濕度: ${jsonData.optDouble("humi", -1.0)}%")
            println("按鈕狀態: ${jsonData.optInt("button", -1)}")
        }
        
        println("完整數據: \n${jsonData.toString(2)}")
    } else {
        // 非JSON數據，直接顯示原始負載
        println("原始數據: $payload")
    }
    
    println("-".repeat(80))
}