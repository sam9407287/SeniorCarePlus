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
import com.seniorcareplus.app.models.HeartRateData
import com.seniorcareplus.app.models.PatientHeartRateGroup
import com.seniorcareplus.app.models.HeartRateStatus
import com.seniorcareplus.app.mqtt.HealthMessage
import com.seniorcareplus.app.mqtt.MqttService
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 心率監測視圖模型，負責處理MQTT心率數據的接收和儲存
 */
class HeartRateViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HeartRateViewModel"
    
    // 服務綁定相關
    private var mqttService: MqttService? = null
    private var isBound = false
    
    // 心率數據流
    private val _heartRateGroups = MutableStateFlow<List<PatientHeartRateGroup>>(emptyList())
    val heartRateGroups: StateFlow<List<PatientHeartRateGroup>> = _heartRateGroups.asStateFlow()
    
    // 對外暴露的病患列表 (Pair<patientId, patientName>)
    val patientList: StateFlow<List<Pair<String, String>>> =
        _heartRateGroups
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
    
    // 心率過濾類型: 0=全部, 1=只顯示高心率, 2=只顯示低心率
    private val _heartRateFilterType = MutableStateFlow(0)
    val heartRateFilterType: StateFlow<Int> = _heartRateFilterType.asStateFlow()
    
    // 異常過濾器: 0=全部, 1=只顯示異常, 2=只顯示正常
    private val _abnormalFilter = MutableStateFlow(0)
    val abnormalFilter: StateFlow<Int> = _abnormalFilter.asStateFlow()
    
    // 時間範圍過濾器: 0=1天, 1=3天, 2=7天
    private val _timeRangeFilter = MutableStateFlow(2)
    val timeRangeFilter: StateFlow<Int> = _timeRangeFilter.asStateFlow()
    
    // 存儲數據
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "heart_rate_data", Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
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
     * 處理健康數據消息，提取心率數據
     */
    private fun processHealthMessage(message: HealthMessage) {
        Log.d(TAG, "收到健康數據: 心率=${message.heartRate}, ID=${message.id}")
        
        // 確保消息包含必要信息
        if (message.id.isNullOrEmpty() || message.heartRate <= 0) {
            Log.w(TAG, "健康數據缺少ID或心率數據無效, 跳過處理")
            return
        }
        
        // 創建心率數據
        val patientName = message.name ?: "Unknown-${message.id}"
        val heartRateData = HeartRateData(
            patientId = message.id!!,
            patientName = patientName,
            heartRate = message.heartRate,
            timestamp = message.time ?: LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ),
            isAbnormal = message.heartRate > 100 || message.heartRate < 60
        )
        
        // 添加到數據組
        addHeartRateData(heartRateData)
    }
    
    /**
     * 添加心率數據到對應的患者組
     */
    private fun addHeartRateData(data: HeartRateData) {
        viewModelScope.launch(Dispatchers.IO) {
            _heartRateGroups.update { currentGroups ->
                val existingGroupIndex = currentGroups.indexOfFirst { it.patientId == data.patientId }
                
                if (existingGroupIndex != -1) {
                    // 更新現有組
                    val existingGroup = currentGroups[existingGroupIndex]
                    val updatedRecords = (existingGroup.records + data)
                        .sortedByDescending { it.getLocalDateTime() }
                        .take(1000) // 限制記錄數量
                    
                    val updatedGroup = existingGroup.copy(records = updatedRecords)
                    currentGroups.toMutableList().apply {
                        set(existingGroupIndex, updatedGroup)
                    }
                } else {
                    // 創建新組
                    val newGroup = PatientHeartRateGroup(
                        patientId = data.patientId,
                        patientName = data.patientName,
                        records = listOf(data)
                    )
                    currentGroups + newGroup
                }
            }
            
            // 保存到本地存儲
            saveCachedData()
            
            if (enableDebugLogs) {
                Log.d(TAG, "添加心率數據: ${data.patientName} - ${data.heartRate} bpm")
            }
        }
    }
    
    /**
     * 獲取指定患者的心率數據
     */
    fun getHeartRateDataForPatient(patientId: String): List<HeartRateData> {
        return _heartRateGroups.value
            .find { it.patientId == patientId }
            ?.records ?: emptyList()
    }
    
    /**
     * 獲取過濾後的心率數據
     */
    fun getFilteredHeartRateData(patientId: String): List<HeartRateData> {
        val allData = getHeartRateDataForPatient(patientId)
        val timeRange = _timeRangeInDays.value
        val showOnlyAbnormal = _showOnlyAbnormal.value
        val filterType = _heartRateFilterType.value
        
        val cutoffTime = LocalDateTime.now().minusDays(timeRange.toLong())
        
        return allData.filter { data ->
            // 時間過濾
            val dataTime = data.getLocalDateTime()
            val withinTimeRange = dataTime.isAfter(cutoffTime)
            
            // 異常過濾
            val abnormalFilter = if (showOnlyAbnormal) data.isAbnormal else true
            
            // 心率類型過濾
            val heartRateTypeFilter = when (filterType) {
                1 -> data.getHeartRateStatus() == HeartRateStatus.HIGH
                2 -> data.getHeartRateStatus() == HeartRateStatus.LOW
                else -> true
            }
            
            withinTimeRange && abnormalFilter && heartRateTypeFilter
        }
    }
    
    /**
     * 設置選中的患者ID
     */
    fun setSelectedPatientId(patientId: String?) {
        _selectedPatientId.value = patientId
    }
    
    /**
     * 設置是否只顯示異常數據
     */
    fun setShowOnlyAbnormal(showOnly: Boolean) {
        _showOnlyAbnormal.value = showOnly
    }
    
    /**
     * 設置時間範圍（天數）
     */
    fun setTimeRangeInDays(days: Int) {
        _timeRangeInDays.value = days
    }
    
    /**
     * 設置心率過濾類型
     */
    fun setHeartRateFilterType(type: Int) {
        _heartRateFilterType.value = type
    }
    
    /**
     * 設置異常過濾器
     */
    fun setAbnormalFilter(filter: Int) {
        _abnormalFilter.value = filter
    }
    
    /**
     * 設置時間範圍過濾器
     */
    fun setTimeRangeFilter(filter: Int) {
        _timeRangeFilter.value = filter
    }
    
    /**
     * 保存數據到本地存儲
     */
    private fun saveCachedData() {
        try {
            val json = gson.toJson(_heartRateGroups.value)
            sharedPreferences.edit()
                .putString("heart_rate_groups", json)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "保存心率數據失敗: ${e.message}")
        }
    }
    
    /**
     * 從本地存儲加載數據
     */
    private fun loadCachedData() {
        try {
            val json = sharedPreferences.getString("heart_rate_groups", null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<PatientHeartRateGroup>>() {}.type
                val groups = gson.fromJson<List<PatientHeartRateGroup>>(json, type)
                _heartRateGroups.value = groups
                Log.d(TAG, "加載了 ${groups.size} 個心率數據組")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加載心率數據失敗: ${e.message}")
        }
    }
    
    /**
     * 清除所有數據
     */
    fun clearAllData() {
        _heartRateGroups.value = emptyList()
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "已清除所有心率數據")
    }
    
    override fun onCleared() {
        super.onCleared()
        unbindMqttService()
    }
} 