package com.seniorcareplus.app.mqtt

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.nio.charset.StandardCharsets

/**
 * MQTT服務，處理MQTT連接和消息接收
 */
class MqttService : Service() {
    private val TAG = "MqttService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = MqttBinder()
    private val gson = MqttGsonHelper.createGson()
    
    // MQTT客戶端
    private var mqttClient: MqttClient? = null
    
    // 消息流，用於向UI層發送消息
    private val _locationMessages = MutableSharedFlow<LocationMessage>(replay = 0)
    val locationMessages: SharedFlow<LocationMessage> = _locationMessages.asSharedFlow()
    
    private val _healthMessages = MutableSharedFlow<HealthMessage>(replay = 0)
    val healthMessages: SharedFlow<HealthMessage> = _healthMessages.asSharedFlow()
    
    private val _diaperMessages = MutableSharedFlow<DiaperMessage>(replay = 0)
    val diaperMessages: SharedFlow<DiaperMessage> = _diaperMessages.asSharedFlow()
    
    // 原始消息流 - 用於測試和調試
    private val _rawMessages = MutableSharedFlow<Pair<String, String>>(replay = 0)
    val rawMessages: SharedFlow<Pair<String, String>> = _rawMessages.asSharedFlow()
    
    inner class MqttBinder : Binder() {
        fun getService(): MqttService = this@MqttService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        setupMqttClient()
    }
    
    override fun onDestroy() {
        disconnectMqtt()
        super.onDestroy()
    }
    
    /**
     * 設置MQTT客戶端並連接
     */
    private fun setupMqttClient() {
        try {
            // 創建MQTT客戶端
            mqttClient = MqttClient(
                MqttConstants.MQTT_SERVER_URI,
                MqttConstants.generateClientId(),
                MemoryPersistence()
            )
            
            // 設置回調
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "MQTT連接丟失: ${cause?.message}")
                    // 嘗試重新連接
                    serviceScope.launch {
                        try {
                            Thread.sleep(MqttConstants.RECONNECT_DELAY)
                            connectMqtt()
                        } catch (e: Exception) {
                            Log.e(TAG, "重新連接失敗: ${e.message}")
                        }
                    }
                }
                
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let { mqttMessage ->
                        val payload = String(mqttMessage.payload, StandardCharsets.UTF_8)
                        Log.d(TAG, "收到MQTT消息，主題: $topic, 内容: $payload")
                        
                        try {
                            // 發送原始消息以供測試
                            serviceScope.launch {
                                topic?.let { _rawMessages.emit(it to payload) }
                            }
                            
                            processMessage(topic, payload)
                        } catch (e: Exception) {
                            Log.e(TAG, "處理消息時出錯: ${e.message}")
                        }
                    }
                }
                
                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "消息發送完成")
                }
            })
            
            // 連接MQTT服務器
            connectMqtt()
        } catch (e: Exception) {
            Log.e(TAG, "設置MQTT客戶端時出錯: ${e.message}")
        }
    }
    
    /**
     * 連接MQTT服務器
     */
    private fun connectMqtt() {
        try {
            // 設置連接選項
            val connOpts = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = MqttConstants.CONNECTION_TIMEOUT
                keepAliveInterval = MqttConstants.KEEP_ALIVE_INTERVAL
            }
            
            // 連接MQTT服務器
            mqttClient?.connect(connOpts)
            Log.d(TAG, "MQTT連接成功")
            
            // 訂閱主題
            subscribeToTopics()
        } catch (e: Exception) {
            Log.e(TAG, "MQTT連接失敗: ${e.message}")
        }
    }
    
    /**
     * 訂閱MQTT主題
     */
    private fun subscribeToTopics() {
        try {
            // 訂閱位置數據主題 - 主要和備用主題
            mqttClient?.subscribe(MqttConstants.TOPIC_LOCATION, MqttConstants.QOS_1)
            mqttClient?.subscribe(MqttConstants.TOPIC_LOCATION_ALT, MqttConstants.QOS_1)
            mqttClient?.subscribe(MqttConstants.TOPIC_LOCATION_ALT2, MqttConstants.QOS_1)
            mqttClient?.subscribe(MqttConstants.TOPIC_LOCATION_RAW, MqttConstants.QOS_1)
            
            // 訂閱健康數據主題
            mqttClient?.subscribe(MqttConstants.TOPIC_HEALTH, MqttConstants.QOS_1)
            // 訂閱消息主題
            mqttClient?.subscribe(MqttConstants.TOPIC_MESSAGE, MqttConstants.QOS_1)
            // 訂閱確認消息主題
            mqttClient?.subscribe(MqttConstants.TOPIC_ACK, MqttConstants.QOS_1)
            // 訂閱網關主題
            mqttClient?.subscribe(MqttConstants.TOPIC_GATEWAY, MqttConstants.QOS_1)
            
            // 開發測試階段可以訂閱所有主題
            mqttClient?.subscribe(MqttConstants.TOPIC_ALL, MqttConstants.QOS_1)
            
            Log.i(TAG, "所有MQTT主題訂閱成功，包括備用主題")
        } catch (e: Exception) {
            Log.e(TAG, "訂閱主題失敗: ${e.message}")
        }
    }
    
    /**
     * 處理接收到的MQTT消息
     */
    private fun processMessage(topic: String?, payload: String) {
        try {
            Log.i(TAG, "正在處理消息，主題: $topic, 內容: $payload")
            
            // 嘗試解析任何JSON訊息
            try {
                // 解析JSON消息
                val baseMessage = gson.fromJson(payload, com.seniorcareplus.app.mqtt.MqttMessage::class.java)
                Log.d(TAG, "成功解析消息: ${baseMessage.content}")
                
                when (baseMessage.content) {
                    // 位置數據
                    MqttConstants.CONTENT_TYPE_LOCATION -> {
                        Log.d(TAG, "處理位置數據消息")
                        val locationMessage = gson.fromJson(payload, LocationMessage::class.java)
                        Log.d(TAG, "位置消息解析: id=${locationMessage.id}, gatewayId=${locationMessage.gatewayId}, position=${locationMessage.position}")
                        
                        serviceScope.launch {
                            Log.d(TAG, "發送位置消息到UI")
                            _locationMessages.emit(locationMessage)
                        }
                    }
                    
                    // 健康數據
                    MqttConstants.CONTENT_TYPE_HEALTH -> {
                        Log.d(TAG, "處理健康數據消息")
                        val healthMessage = gson.fromJson(payload, HealthMessage::class.java)
                        serviceScope.launch {
                            _healthMessages.emit(healthMessage)
                        }
                    }
                    
                    // 尿布狀態數據
                    MqttConstants.CONTENT_TYPE_DIAPER -> {
                        Log.d(TAG, "處理尿布狀態消息")
                        val diaperMessage = gson.fromJson(payload, DiaperMessage::class.java)
                        serviceScope.launch {
                            _diaperMessages.emit(diaperMessage)
                        }
                    }
                    
                    // 其他消息類型可以根據需要添加
                    else -> {
                        Log.d(TAG, "收到未處理的消息類型: ${baseMessage.content}")
                        // 嘗試作為位置消息處理
                        try {
                            val locationMessage = gson.fromJson(payload, LocationMessage::class.java)
                            if (locationMessage.position != null) {
                                Log.d(TAG, "嘗試作為位置消息處理: ${locationMessage.position}")
                                serviceScope.launch {
                                    _locationMessages.emit(locationMessage)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "不是有效的位置消息: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "JSON解析失敗，嘗試作為原始文本處理: ${e.message}")
                
                // 如果消息是純文本包含位置信息，也可以嘗試提取
                if (payload.contains("position") || payload.contains("location")) {
                    Log.d(TAG, "可能包含位置信息的原始消息")
                    // 發送到UI原始消息流
                    serviceScope.launch {
                        topic?.let { _rawMessages.emit(it to payload) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "處理消息時出錯: ${e.message}, 消息内容: $payload")
        }
    }
    
    /**
     * 斷開MQTT連接
     */
    private fun disconnectMqtt() {
        try {
            mqttClient?.disconnect()
            Log.d(TAG, "MQTT斷開連接")
        } catch (e: Exception) {
            Log.e(TAG, "MQTT斷開連接失敗: ${e.message}")
        } finally {
            mqttClient = null
        }
    }
    
    /**
     * 發送MQTT消息
     */
    fun publishMessage(topic: String, message: String, qos: Int = MqttConstants.QOS_1) {
        try {
            val mqttMessage = org.eclipse.paho.client.mqttv3.MqttMessage(message.toByteArray(StandardCharsets.UTF_8))
            mqttMessage.qos = qos
            mqttClient?.publish(topic, mqttMessage)
            Log.d(TAG, "發送MQTT消息成功，主題: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "發送MQTT消息失敗: ${e.message}")
        }
    }
}
