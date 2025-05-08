package com.seniorcareplus.app.ui.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seniorcareplus.app.mqtt.LocationMessage
import com.seniorcareplus.app.mqtt.MqttService
import com.seniorcareplus.app.ui.screens.LocationData
import com.seniorcareplus.app.ui.screens.LocationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline

/**
 * MapViewModel - 處理地圖相關數據和MQTT服務連接
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MapViewModel"
    
    // MQTT服務相關
    private var mqttService: MqttService? = null
    private var isBound = false
    
    // 位置數據
    private val _locationData = MutableStateFlow<List<LocationData>>(emptyList())
    val locationData = _locationData.asStateFlow()
    
    // 狀態標記
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()
    
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError = _connectionError.asStateFlow()
    
    // 初始化
    init {
        // 只顯示初始化靜態數據，不自動連接MQTT
        initializeStaticData()
    }
    
    // 服務連接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as? MqttService.MqttBinder
                if (binder == null) {
                    Log.e(TAG, "服務綁定失敗：非預期的Binder類型")
                    _connectionError.value = "無法連接到MQTT服務：服務無效"
                    return
                }
                
                mqttService = binder.getService()
                isBound = true
                _isConnecting.value = false
                Log.d(TAG, "MQTT服務已連接成功")
                
                // 開始監聽位置數據
                observeLocationMessages()
            } catch (e: Exception) {
                Log.e(TAG, "服務連接异常: ${e.message}")
                _connectionError.value = "連接失敗: ${e.message}"
                _isConnecting.value = false
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            mqttService = null
            isBound = false
            Log.d(TAG, "MQTT服務已斷開")
        }
    }
    
    /**
     * 綁定MQTT服務 - 安全版本
     */
    fun bindMqttService() {
        try {
            // 避免重復綁定
            if (isBound) {
                Log.d(TAG, "MQTT服務已綁定，跳過")
                return
            }
            
            _isConnecting.value = true
            _connectionError.value = null
            
            val intent = Intent(getApplication(), MqttService::class.java)
            
            // 啟動服務，確保在綁定之前存在
            try {
                getApplication<Application>().startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "啟動MQTT服務失敗，結將嘗試綁定: ${e.message}")
            }
            
            val bound = getApplication<Application>().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
            
            if (!bound) {
                Log.e(TAG, "MQTT服務綁定失敗")
                _connectionError.value = "無法連接到MQTT服務"
            } else {
                Log.d(TAG, "MQTT服務綁定請求已發送，等待連接...")
            }
            
            // 設定一個超時檢查
            viewModelScope.launch {
                kotlinx.coroutines.delay(5000) // 5秒超時
                if (_isConnecting.value && !isBound) {
                    Log.e(TAG, "MQTT連接逾時")
                    _connectionError.value = "MQTT連接逾時"
                    _isConnecting.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "綁定MQTT服務異常: ${e.message}")
            _connectionError.value = "MQTT連接錯誤: ${e.message}"
            _isConnecting.value = false
        }
    }
    
    /**
     * 解綁MQTT服務 - 安全版本
     */
    fun unbindMqttService() {
        try {
            if (isBound) {
                try {
                    getApplication<Application>().unbindService(serviceConnection)
                } catch (e: Exception) {
                    Log.e(TAG, "解綁MQTT服務失敗: ${e.message}")
                }
                mqttService = null
                isBound = false
                Log.d(TAG, "MQTT服務已成功解綁")
            }
        } catch (e: Exception) {
            Log.e(TAG, "解綁MQTT服務過程發生異常: ${e.message}")
        } finally {
            // 確保狀態重設
            _isConnecting.value = false
            mqttService = null
            isBound = false
        }
    }
    
    /**
     * 觀察MQTT位置消息
     */
    private fun observeLocationMessages() {
        mqttService?.let { service ->
            viewModelScope.launch {
                service.locationMessages.collect { message ->
                    updateLocationData(message)
                }
            }
        }
    }
    
    /**
     * 根據 MQTT消息更新位置數據
     */
    private fun updateLocationData(message: LocationMessage) {
        try {
            // 記錄收到的消息但避免完整內容可能導致的記錄問題
            Log.d(TAG, "收到位置消息: ID=${message.id}, 節點=${message.node}")
            
            // 網關 ID處理
            val gatewayId = message.gatewayId.toString().take(10) // 限制長度避免溢出
            
            // 如果沒有位置數據則安全返回
            if (message.position == null) {
                Log.e(TAG, "收到的位置數據無效：缺少position信息")
                return
            }
            
            // 安全地創建唯一ID
            val node = message.node.take(10) // 限制長度避免溢出
            val deviceId = "${node}_${message.id}"
            Log.d(TAG, "處理設備ID: $deviceId")
            
            // 檢查是否已有相同ID的數據
            val existingIndex = _locationData.value.indexOfFirst { 
                it.id == deviceId 
            }
            
            // 安全地取得坐標值
            val xRaw = message.position.x.coerceIn(-5f, 5f) // 大幅縮小合理範圍
            val yRaw = message.position.y.coerceIn(-5f, 5f) // 大幅縮小合理範圍
            
            // 產生控制移動方向的時間因子
            val timeBasedFactor = System.currentTimeMillis() / 1000.0 % 6.0
            val sinFactor = kotlin.math.sin(timeBasedFactor).toFloat() * 2f
            val cosFactor = kotlin.math.cos(timeBasedFactor).toFloat() * 2f
            
            // 根據設備ID調整位置變化範圍和移動方式
            val newXPos: Float
            val newYPos: Float
            
            when (message.id) {
                "E001" -> { // 張三 - 上下移動
                    // 基礎座標 (250, 250) 加上上下移動
                    newXPos = 190f // 固定水平位置
                    newYPos = 180f + (sinFactor * 30f) // 上下移動
                    Log.d(TAG, "張三移動: 上下移動 Y偏移=${sinFactor * 30f}")
                }
                "E002" -> { // 李四 - 左右移動
                    // 基礎座標 (600, 160) 加上左右移動
                    newXPos = 160f + (cosFactor * 40f) // 左右移動
                    newYPos = 160f // 固定垂直位置
                    Log.d(TAG, "李四移動: 左右移動 X偏移=${cosFactor * 40f}")
                }
                "E003" -> { // 王五 - 斜向移動
                    // 基礎座標 (550, 310) 加上斜向移動
                    newXPos = 550f + (cosFactor * 20f) // 左右引量
                    newYPos = 310f + (sinFactor * 20f) // 上下引量
                    Log.d(TAG, "王五移動: 斜向 X偏移=${cosFactor * 20f}, Y偏移=${sinFactor * 20f}")
                }
                "E004" -> { // 趙六 - 幾乎不動
                    // 基礎座標 (250, 310) 加上很小的隨機移動
                    newXPos = 250f + (xRaw * 3f) // 極小隨機移動
                    newYPos = 310f + (yRaw * 3f) // 極小隨機移動
                    Log.d(TAG, "趙六移動: 幾乎不動 X偏移=${xRaw * 3f}, Y偏移=${yRaw * 3f}")
                }
                "E005" -> { // 錢七 - 圓形路徑移動
                    // 基礎座標 (400, 400) 加上圓形路徑移動
                    newXPos = 400f + (cosFactor * 50f) // 圓形X分量
                    newYPos = 400f + (sinFactor * 50f) // 圓形Y分量
                    Log.d(TAG, "錢七移動: 圓形路徑 X=${newXPos}, Y=${newYPos}")
                }
                else -> { // 其他人 - 隨機移動
                    // 為其他設備提供預設位置
                    val defaultX = 350f
                    val defaultY = 350f
                    
                    newXPos = defaultX + (xRaw * 20f) // 中等位移範圍
                    newYPos = defaultY + (yRaw * 20f) // 中等位移範圍
                }
            }
            
            // 最終的位置坐標
            val xPos = newXPos
            val yPos = newYPos
            
            Log.d(TAG, "坐標轉換: [$xRaw, $yRaw] -> [$xPos, $yPos]")
            
            // 根據使用者ID確定位置類型和名稱
            val locationType = LocationType.ELDERLY  // 預設為長者
            
            // 安全地取得名稱
            val displayName = when(message.id) {
                "E001" -> "張三"
                "E002" -> "李四"
                "E003" -> "王五"
                "E004" -> "趙六"
                "E005" -> "錢七"
                else -> if (message.name.isNotEmpty()) { 
                    message.name.take(20) // 限制名稱長度 
                } else { 
                    "${node.take(5)} #${message.id}" 
                }
            }
            
            // 安全地創建新的位置數據
            val newLocationData = LocationData(
                id = deviceId,
                name = displayName,
                x = xPos,
                y = yPos,
                type = locationType,
                avatarIcon = getAvatarIcon(displayName)
            )
            
            // 更新UI狀態
            viewModelScope.launch {
                try {
                    if (existingIndex >= 0) {
                        // 更新現有數據
                        val updatedList = _locationData.value.toMutableList()
                        val oldData = updatedList.getOrNull(existingIndex) // 安全訪問
                        if (oldData != null) {
                            updatedList[existingIndex] = newLocationData
                            _locationData.update { updatedList }
                            Log.d(TAG, "更新現有資料: ${oldData.name}")
                        }
                    } else {
                        // 添加新數據
                        _locationData.update { it + newLocationData }
                        Log.d(TAG, "新增位置資料: ${newLocationData.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "更新位置數據失敗: ${e.message}")
                }
            }
        } catch (e: Exception) {
            // 捕捉所有異常以避免應用程序崩潰
            Log.e(TAG, "處理MQTT位置消息發生崩潰: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 根據名稱獲取頭像圖標
     */
    private fun getAvatarIcon(name: String) = when {
        name.contains("1") -> Icons.Default.Person
        name.contains("2") -> Icons.Default.PersonOutline
        else -> Icons.Default.Face
    }
    
    /**
     * 初始化靜態數據（將被MQTT數據動態更新）
     */
    private fun initializeStaticData() {
        // 示例錨點數據
        val anchors = listOf(
            LocationData(
                id = "U001",
                name = "錨點1",
                x = 100f,
                y = 100f,
                type = LocationType.UWB_ANCHOR
            ),
            LocationData(
                id = "U002",
                name = "錨點2",
                x = 100f,
                y = 900f,
                type = LocationType.UWB_ANCHOR
            ),
            LocationData(
                id = "U003",
                name = "錨點3",
                x = 900f,
                y = 100f,
                type = LocationType.UWB_ANCHOR
            ),
            LocationData(
                id = "U004",
                name = "錨點4",
                x = 900f,
                y = 900f,
                type = LocationType.UWB_ANCHOR
            )
        )
        
        // 只初始化錨點，不添加長者數據
        // 長者會在收到MQTT消息時動態添加
        
        // 更新初始數據（只有錨點）
        _locationData.value = anchors
        
        // 記錄日誌
        Log.d(TAG, "地圖初始化完成，無長者數據，等待MQTT消息")
    }
    
    /**
     * 清理ViewModel
     */
    override fun onCleared() {
        unbindMqttService()
        super.onCleared()
    }
}
