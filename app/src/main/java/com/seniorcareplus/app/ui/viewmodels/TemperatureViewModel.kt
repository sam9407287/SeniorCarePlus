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
import com.seniorcareplus.app.models.TemperatureData
import com.seniorcareplus.app.models.PatientTemperatureGroup
import com.seniorcareplus.app.mqtt.HealthMessage
import com.seniorcareplus.app.mqtt.MqttService
import com.seniorcareplus.app.mqtt.TemperatureMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.SharedPreferences
// Temporarily comment out serialization imports until dependencies are properly set up
// import kotlinx.serialization.encodeToString
// import kotlinx.serialization.decodeFromString
// import kotlinx.serialization.json.Json
import com.seniorcareplus.app.models.TemperatureStatus

/**
 * 體溫監測視圖模型，負責處理MQTT體溫數據的接收和儲存
 */
class TemperatureViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "TemperatureViewModel"
    
    // 服務綁定相關
    private var mqttService: MqttService? = null
    private var isBound = false
    
    // 體溫數據流
    private val _temperatureGroups = MutableStateFlow<List<PatientTemperatureGroup>>(emptyList())
    val temperatureGroups: StateFlow<List<PatientTemperatureGroup>> = _temperatureGroups.asStateFlow()
    
    // 對外暴露的病患列表 (Pair<patientId, patientName>)
    val patientList: StateFlow<List<Pair<String, String>>> =
        _temperatureGroups
            .map { groups ->
                groups.map { it.patientId to it.patientName }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    
    // 選中的病患ID
    private val _selectedPatientId = MutableStateFlow<String?>(null)
    val selectedPatientId: StateFlow<String?> = _selectedPatientId.asStateFlow()
    
    // 過濾設置
    private val _showOnlyAbnormal = MutableStateFlow(false)
    val showOnlyAbnormal: StateFlow<Boolean> = _showOnlyAbnormal.asStateFlow()
    
    private val _timeRangeInDays = MutableStateFlow(7)
    val timeRangeInDays: StateFlow<Int> = _timeRangeInDays.asStateFlow()
    
    // 溫度過濾類型: 0=全部, 1=只顯示高溫, 2=只顯示低溫
    private val _temperatureFilterType = MutableStateFlow(0)
    val temperatureFilterType: StateFlow<Int> = _temperatureFilterType.asStateFlow()
    
    // 存儲數據
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "temperature_data", Context.MODE_PRIVATE
    )
    // Temporarily use Gson instead of kotlinx.serialization
    private val gson = com.google.gson.Gson()
    
    // 調試用：是否打印數據日誌
    private val enableDebugLogs = true
    
    // 服務連接
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as? MqttService.MqttBinder
            if (binder != null) {
                mqttService = binder.getService()
                isBound = true
                Log.d(TAG, "MQTT服務已連接")
                observeHealthMessages()
                observeTemperatureMessages()
            }
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            mqttService = null
            isBound = false
            Log.d(TAG, "MQTT服務已斷開連接")
        }
    }
    
    init {
        loadCachedData()
        bindMqttService()
    }
    
    /**
     * 綁定MQTT服務
     */
    fun bindMqttService() {
        if (!isBound) {
            try {
                val intent = Intent(getApplication(), MqttService::class.java)
                getApplication<Application>().bindService(
                    intent, 
                    connection, 
                    Context.BIND_AUTO_CREATE
                )
            } catch (e: Exception) {
                Log.e(TAG, "綁定MQTT服務時出錯: ${e.message}")
            }
        }
    }
    
    /**
     * 解除MQTT服務綁定
     */
    fun unbindMqttService() {
        if (isBound) {
            try {
                getApplication<Application>().unbindService(connection)
                isBound = false
                mqttService = null
            } catch (e: Exception) {
                Log.e(TAG, "解除MQTT服務綁定時出錯: ${e.message}")
            }
        }
    }
    
    /**
     * 監聽健康數據消息
     */
    private fun observeHealthMessages() {
        mqttService?.let { service ->
            viewModelScope.launch {
                service.healthMessages.collect { message ->
                    processHealthMessage(message)
                }
            }
        }
    }
    
    /**
     * 監聽體溫數據消息
     */
    private fun observeTemperatureMessages() {
        mqttService?.let { service ->
            viewModelScope.launch {
                service.temperatureMessages.collect { message ->
                    processTemperatureMessage(message)
                }
            }
        }
    }
    
    /**
     * 處理健康數據消息（300B 格式）
     */
    private fun processHealthMessage(message: HealthMessage) {
        Log.d(TAG, "收到健康數據: ${message.heartRate}, 體溫: ${message.skinTemp}")
        
        // 確保消息包含必要信息
        if (message.id.isNullOrEmpty()) {
            Log.w(TAG, "健康數據缺少ID, 跳過處理")
            return
        }
        
        // 創建體溫數據
        val patientName = message.name ?: "Unknown-${message.id}"
        val temperatureData = TemperatureData(
            patientId = message.id!!,
            patientName = patientName,
            temperature = message.skinTemp,
            roomTemperature = message.roomTemp,
            timestamp = message.time ?: LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ),
            isAbnormal = message.skinTemp > 37.5f || message.skinTemp < 36.0f
        )
        
        // 添加到數據組
        addTemperatureData(temperatureData)
    }
    
    /**
     * 處理體溫專用消息（自定義格式）
     */
    private fun processTemperatureMessage(message: TemperatureMessage) {
        // 添加更詳細的日誌以便調試
        if (enableDebugLogs) {
            Log.d(TAG, "收到體溫數據: 病患ID=${message.id}, 名稱=${message.name}, 體溫=${message.temperature.value}°C, 房間溫度=${message.temperature.roomTemp}°C, 時間=${message.time}, 異常=${message.temperature.isAbnormal}")
        }
        
        // 創建體溫數據
        val temperatureData = TemperatureData(
            patientId = message.id,
            patientName = message.name,
            temperature = message.temperature.value,
            roomTemperature = message.temperature.roomTemp,
            timestamp = message.time,
            isAbnormal = message.temperature.isAbnormal
        )
        
        // 添加到數據組並打印詳細日誌
        addTemperatureData(temperatureData)
    }
    
    /**
     * 添加體溫數據到對應的病患組
     */
    private fun addTemperatureData(data: TemperatureData) {
        viewModelScope.launch(Dispatchers.IO) {
            if (enableDebugLogs) {
                Log.d(TAG, "添加體溫數據: 病患=${data.patientName}(${data.patientId}), 體溫=${data.temperature}, 時間=${data.timestamp}")
            }
            
            // 獲取當前數據
            val currentGroups = _temperatureGroups.value
            
            // 找到對應的病患組
            val patientGroupIndex = currentGroups.indexOfFirst { it.patientId == data.patientId }
            
            // 準備更新的群組列表
            val newGroups = if (patientGroupIndex >= 0) {
                // 更新現有病患組
                val updatedGroup = currentGroups[patientGroupIndex].copy(
                    records = currentGroups[patientGroupIndex].records + data
                )
                currentGroups.toMutableList().apply {
                    set(patientGroupIndex, updatedGroup)
                }
            } else {
                // 創建新的病患組
                val newGroup = PatientTemperatureGroup(
                    patientId = data.patientId,
                    patientName = data.patientName,
                    records = listOf(data)
                )
                currentGroups + newGroup
            }
            
            // 更新狀態流
            _temperatureGroups.update { newGroups }
            
            // 如果沒有選擇的病患ID或ID為空，自動選擇這個病患
            if (selectedPatientId.value == null || selectedPatientId.value!!.isEmpty()) {
                _selectedPatientId.update { data.patientId }
                Log.d(TAG, "設置選中的病患ID: ${data.patientId}, 名稱: ${data.patientName}")
            }
            
            // 打印診斷信息
            if (enableDebugLogs) {
                Log.d(TAG, "更新後的病患組數量: ${newGroups.size}")
                val patientSummary = newGroups.joinToString("\n") { group ->
                    "  - ${group.patientName}(ID:${group.patientId}): ${group.records.size}筆記錄"
                }
                Log.d(TAG, "目前所有病患群組:\n$patientSummary")
            }
            
            // 保存到本地緩存
            saveDataToCache()
        }
    }
    
    /**
     * 將數據保存到本地緩存
     */
    private fun saveDataToCache() {
        try {
            val dataToSave = _temperatureGroups.value.map { group ->
                // 只保存每個病患的必要信息和最多100條記錄
                PatientTemperatureGroup(
                    patientId = group.patientId,
                    patientName = group.patientName,
                    records = group.records.take(100)
                )
            }
            
            val jsonData = gson.toJson(dataToSave)
            sharedPreferences.edit().putString("cached_temperature_data", jsonData).apply()
            Log.d(TAG, "體溫數據已保存到本地緩存")
        } catch (e: Exception) {
            Log.e(TAG, "保存數據到緩存時出錯: ${e.message}")
        }
    }

    /**
     * 加載本地緩存的數據
     */
    private fun loadCachedData() {
        try {
            val jsonData = sharedPreferences.getString("cached_temperature_data", null)
            if (!jsonData.isNullOrEmpty()) {
                val cachedGroups: List<PatientTemperatureGroup> = gson.fromJson(jsonData, 
                    object : com.google.gson.reflect.TypeToken<List<PatientTemperatureGroup>>() {}.type)
                _temperatureGroups.update { cachedGroups }
                
                // 如果有數據，選擇第一個病患
                if (cachedGroups.isNotEmpty()) {
                    _selectedPatientId.update { cachedGroups.first().patientId }
                }
                
                Log.d(TAG, "從本地緩存加載了 ${cachedGroups.size} 名病患的體溫數據")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加載緩存數據時出錯: ${e.message}")
        }
    }
    
    /**
     * 設置選中的病患
     */
    fun selectPatient(patientId: String) {
        _selectedPatientId.update { patientId }
    }
    
    /**
     * 設置是否只顯示異常數據
     */
    fun setShowOnlyAbnormal(show: Boolean) {
        _showOnlyAbnormal.update { show }
    }
    
    /**
     * 設置時間範圍（天數）
     */
    fun setTimeRangeInDays(days: Int) {
        _timeRangeInDays.update { days }
    }
    
    /**
     * 設置溫度過濾類型
     */
    fun setTemperatureFilterType(type: Int) {
        _temperatureFilterType.update { type }
    }
    
    /**
     * 依據病患ID、顯示天數與是否只顯示異常，取得過濾後的體溫資料
     */
    fun getPatientTemperatureData(
        patientId: String,
        daysToShow: Int,
        showAbnormalOnly: Boolean
    ): StateFlow<List<TemperatureData>> {
        return _temperatureGroups
            .map { groups ->
                val group = groups.find { it.patientId == patientId }
                if (group == null) return@map emptyList()

                val cutoffTime = LocalDateTime.now().minusDays(daysToShow.toLong())

                group.records.filter { record ->
                    val inRange = record.getLocalDateTime().isAfter(cutoffTime)
                    val abnormalFilter = if (showAbnormalOnly) record.isAbnormal else true
                    inRange && abnormalFilter
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }
    
    /**
     * 設定異常過濾 (null 表示全部)
     */
    fun setAbnormalFilter(status: TemperatureStatus?) {
        val type = when (status) {
            TemperatureStatus.HIGH -> 1
            TemperatureStatus.LOW -> 2
            else -> 0
        }
        _temperatureFilterType.update { type }
    }
    
    /**
     * 獲取當前選中病患的過濾後數據
     */
    fun getFilteredData(): List<TemperatureData> {
        val patientId = selectedPatientId.value ?: return emptyList()
        val patientGroup = _temperatureGroups.value.find { it.patientId == patientId } ?: return emptyList()
        
        // 設置時間範圍
        val cutoffTime = LocalDateTime.now().minusDays(_timeRangeInDays.value.toLong())
        
        // 過濾數據
        return patientGroup.records.filter { record ->
            // 時間範圍過濾
            val recordTime = record.getLocalDateTime()
            val isInTimeRange = recordTime.isAfter(cutoffTime)
            
            // 異常狀態過濾
            val passesAbnormalFilter = if (_showOnlyAbnormal.value) record.isAbnormal else true
            
            // 溫度類型過濾
            val passesTemperatureFilter = when (_temperatureFilterType.value) {
                1 -> record.temperature > 37.5f  // 高溫
                2 -> record.temperature < 36.0f  // 低溫
                else -> true                     // 全部
            }
            
            isInTimeRange && passesAbnormalFilter && passesTemperatureFilter
        }
    }
    
    /**
     * 清除ViewModel資源
     */
    override fun onCleared() {
        unbindMqttService()
        super.onCleared()
    }
}
