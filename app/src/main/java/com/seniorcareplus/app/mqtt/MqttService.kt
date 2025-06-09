package com.seniorcareplus.app.mqtt

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import org.json.JSONException
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * MQTT服務，處理MQTT連接和消息接收
 */
class MqttService : Service() {
    private val TAG = "MqttService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = MqttBinder()
    private val gson = MqttGsonHelper.createGson()
    
    // MQTT客戶端
    private var mqttClient: MqttAsyncClient? = null
    
    // 消息流，用於向UI層發送消息
    private val _locationMessages = MutableSharedFlow<LocationMessage>(replay = 0)
    val locationMessages: SharedFlow<LocationMessage> = _locationMessages.asSharedFlow()
    
    private val _healthMessages = MutableSharedFlow<HealthMessage>(replay = 0)
    val healthMessages: SharedFlow<HealthMessage> = _healthMessages.asSharedFlow()
    
    private val _diaperMessages = MutableSharedFlow<DiaperMessage>(replay = 0)
    val diaperMessages: SharedFlow<DiaperMessage> = _diaperMessages.asSharedFlow()
    
    // 添加體溫專用消息流
    private val _temperatureMessages = MutableSharedFlow<TemperatureMessage>(replay = 0)
    val temperatureMessages: SharedFlow<TemperatureMessage> = _temperatureMessages.asSharedFlow()
    
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
        // 在後台線程啟動MQTT客戶端
        serviceScope.launch {
            setupMqttClient()
        }
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
            Log.d(TAG, "初始化MQTT客戶端: ${MqttConstants.MQTT_SERVER_URI}")
            
            // 創建MQTT異步客戶端以提高穩定性和性能
            // 使用WebSocket連接所需的屬性
            val mqttConnectProperties = Properties()
            mqttConnectProperties.setProperty("mqtt.websocket.uri.path", "/mqtt")
            
            mqttClient = MqttAsyncClient(
                MqttConstants.MQTT_SERVER_URI, 
                MqttConstants.generateClientId() + "_" + System.currentTimeMillis(), 
                MemoryPersistence()
            )
            
            mqttClient!!.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "MQTT連接丟失: ${cause?.message}")
                    cause?.printStackTrace()
                    
                    // 延遲重新連接以避免立即重連造成的問題
                    serviceScope.launch(Dispatchers.IO) {
                        delay(3000) // 3秒後重試
                        Log.d(TAG, "連接丟失後3秒重試MQTT連接")
                        connectMqtt()
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(TAG, "收到MQTT消息, 主題: $topic, 內容: ${message?.payload?.toString(Charsets.UTF_8)}")
                    
                    // 處理接收到的消息
                    val payload = message?.payload?.toString(Charsets.UTF_8) ?: return
                    processMessage(topic, payload)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "MQTT消息已送達: ${token?.message?.payload?.toString(Charsets.UTF_8)}")
                }
            })
            // 連接MQTT服務器
            connectMqtt()
        } catch (e: Exception) {
            Log.e(TAG, "MQTT客戶端設置失敗: ${e.message}")
            e.printStackTrace()
            // 延遲重試
            serviceScope.launch(Dispatchers.IO) {
                delay(5000) // 5秒後重試
                Log.d(TAG, "設置失敗後5秒重試MQTT客戶端設置")
                setupMqttClient()
            }
        }
    }
    
    /**
     * 連接MQTT服務器
     */
    private fun connectMqtt() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                if (mqttClient == null) {
                    setupMqttClient()
                    return@launch
                }

                // 不檢查網絡可訪問性，直接嘗試連接
                Log.d(TAG, "MQTT連接參數: 用戶名=${MqttConstants.MQTT_REMOTE_USER}, URI=${MqttConstants.MQTT_REMOTE_SERVER_URI}")
                
                val connOpts = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 60  // 增加至60秒
                    keepAliveInterval = 60  // 增加至60秒
                    maxInflight = 100
                    
                    // 設置MqttVersion以確保包容性
                    mqttVersion = MQTT_VERSION_3_1_1
                    
                    // 設置WebSocket特定標頭
                    val webSocketHeaders = Properties()
                    webSocketHeaders.setProperty("Sec-WebSocket-Protocol", "mqtt")
                    customWebSocketHeaders = webSocketHeaders
                    
                    if (MqttConstants.USE_REMOTE_SERVER) {
                        userName = MqttConstants.MQTT_REMOTE_USER
                        password = MqttConstants.MQTT_REMOTE_PASSWORD.toCharArray()
                        
                        try {
                            // 使用標準TLS 1.2設置
                            val sc = SSLContext.getInstance("TLSv1.2")
                            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                                    Log.d(TAG, "客戶端證書檢查: authType=$authType")
                                }
                                
                                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                                    Log.d(TAG, "服務器證書檢查: authType=$authType")
                                }
                                
                                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                            })
                            
                            sc.init(null, trustAllCerts, SecureRandom())
                            Log.d(TAG, "SSL信任管理器已配置")
                            this.socketFactory = sc.socketFactory
                        } catch (e: Exception) {
                            Log.e(TAG, "SSL設置失敗: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
                
                Log.d(TAG, "正在連接MQTT服務器(WebSocket): ${MqttConstants.MQTT_REMOTE_SERVER_URI}")
                mqttClient?.connect(connOpts, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "MQTT連接成功")
                        serviceScope.launch(Dispatchers.IO) {
                            try {
                                subscribeToTopics()
                            } catch (e: Exception) {
                                Log.e(TAG, "訂閱主題失敗: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(TAG, "MQTT連接失敗: ${exception?.message}")
                        exception?.printStackTrace()
                        // 延遲重試
                        serviceScope.launch(Dispatchers.IO) {
                            delay(5000) // 5秒後重試
                            Log.d(TAG, "5秒後重試MQTT連接")
                            connectMqtt()
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "MQTT連接過程發生異常: ${e.message}")
                e.printStackTrace()
                // 延遲重試
                serviceScope.launch(Dispatchers.IO) {
                    delay(5000) // 5秒後重試
                    Log.d(TAG, "例外處理後5秒重試MQTT連接")
                    connectMqtt()
                }
            }
        }
    }
    
    /**
     * 訂閱MQTT主題
     */
    private fun subscribeToTopics() {
        try {
            if (mqttClient == null) {
                Log.e(TAG, "無法訂閱主題: MQTT客戶端為null")
                setupMqttClient()
                return
            }
            
            if (mqttClient?.isConnected == true) {
                if (MqttConstants.USE_REMOTE_SERVER) {
                    // 訂閱遠端服務器主題
                    val topic = MqttConstants.MQTT_REMOTE_TOPIC
                    val qos = MqttConstants.QOS_1
                    Log.d(TAG, "訂閱遠端主題: $topic")
                    mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(TAG, "成功訂閱MQTT主題: $topic")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.e(TAG, "訂閱遠端主題失敗: ${exception?.message}")
                            exception?.printStackTrace()
                            
                            // 延遲後重試訂閱
                            serviceScope.launch(Dispatchers.IO) {
                                delay(3000) // 3秒後重試
                                Log.d(TAG, "3秒後重試訂閱MQTT主題")
                                subscribeToTopics()
                            }
                        }
                    })
                } else {
                    // 訂閱本地主題在這裡實現
                    Log.d(TAG, "使用本地主題訂閱")
                    // 可以運行本地主題的訂閱部分
                }
            } else {
                Log.e(TAG, "MQTT未連接，無法訂閱主題，將嘗試重新連接")
                connectMqtt()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MQTT主題訂閱時發生異常: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 處理MQTT消息內容
     */
    private fun processMessage(topic: String?, payload: String) {
        // 記錄原始消息內容用於調試
        Log.d(TAG, "接收到MQTT消息: Topic=$topic, Payload=$payload")
        try {
            Log.i(TAG, "正在處理消息，主題: $topic, 內容: $payload")
            
            // 檢查是否為遠端MQTT主題
            if (MqttConstants.USE_REMOTE_SERVER && topic != null && topic == MqttConstants.MQTT_REMOTE_TOPIC) {
                processRemoteMqttMessage(payload)
                return
            }
            
            // 首先嘗試解析模擬器的直接JSON格式（健康數據）
            if (topic != null && topic == MqttConstants.TOPIC_HEALTH_DATA) {
                try {
                    val gson = Gson()
                    val mqttHealthData = gson.fromJson(payload, com.seniorcareplus.app.models.MqttHealthData::class.java)
                    
                    // 轉換為HealthMessage格式
                    val healthMessage = HealthMessage(
                        id = mqttHealthData.userId,
                        name = mqttHealthData.userName,
                        heartRate = mqttHealthData.heartRate,
                        skinTemp = mqttHealthData.temperature.toFloat(),
                        time = mqttHealthData.timeString
                    ).apply {
                        content = MqttConstants.CONTENT_TYPE_HEALTH
                        gatewayId = mqttHealthData.gatewayId.removePrefix("gateway").toLongOrNull() ?: 0L
                    }
                    
                    Log.d(TAG, "成功解析模擬器健康數據: 用戶=${mqttHealthData.userName}, 心率=${mqttHealthData.heartRate}")
                    
                    serviceScope.launch {
                        _healthMessages.emit(healthMessage)
                    }
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "解析模擬器健康數據失敗: ${e.message}")
                }
            }
            
            // 嘗試解析現有的結構化JSON消息
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
                    
                    // 專用體溫數據
                    MqttConstants.CONTENT_TYPE_TEMPERATURE -> {
                        Log.d(TAG, "處理專用體溫數據消息")
                        val temperatureMessage = gson.fromJson(payload, TemperatureMessage::class.java)
                        Log.d(TAG, "體溫消息解析: id=${temperatureMessage.id}, 溫度=${temperatureMessage.temperature.value}°C")
                        
                        serviceScope.launch {
                            _temperatureMessages.emit(temperatureMessage)
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
    /**
     * 處理來自遠端MQTT伺服器的消息（格式不同）
     */
    private fun processRemoteMqttMessage(payload: String) {
        try {
            // 嘗試解析遠端MQTT消息格式
            val jsonObj = JSONObject(payload)
            
            // 檢查是否為位置數據
            if (jsonObj.has("content") && jsonObj.getString("content") == "location" && jsonObj.has("position")) {
                val id = if (jsonObj.has("id")) jsonObj.get("id").toString() else ""
                
                // 獲取位置數據
                val posObj = jsonObj.getJSONObject("position")
                val x = posObj.optDouble("x", 0.0).toFloat()
                val y = posObj.optDouble("y", 0.0).toFloat()
                val z = posObj.optDouble("z", 0.0).toFloat()
                
                // 創建位置對象
                val position = Position(x, y, z)
                
                // 創建標準LocationMessage對象
                val locationMessage = LocationMessage(
                    id = id,
                    node = "remote-node",  // 默認節點名稱
                    name = "Remote Device $id",  // 默認裝置名稱
                    position = position,
                    time = Date().toString()  // 使用當前時間
                ).apply {
                    content = MqttConstants.CONTENT_TYPE_LOCATION
                }
                
                Log.d(TAG, "成功解析遠端位置消息: id=$id, position=$position")
                
                serviceScope.launch {
                    _locationMessages.emit(locationMessage)
                }
            } else {
                Log.w(TAG, "遠端消息格式不符合預期: $payload")
            }
        } catch (e: JSONException) {
            Log.e(TAG, "解析遠端JSON消息失敗: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "處理遠端消息時出錯: ${e.message}")
        }
    }
    
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
