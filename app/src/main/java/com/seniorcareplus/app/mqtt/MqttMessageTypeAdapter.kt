package com.seniorcareplus.app.mqtt

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * 為 MqttMessage 自定義的 JsonDeserializer
 * 根據消息中的 content 字段決定實例化哪個子類別
 */
class MqttMessageDeserializer : JsonDeserializer<MqttMessage> {
    private val TAG = "MqttMessageDeserializer"

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MqttMessage {
        val jsonObject = json.asJsonObject
        
        // 根據content字段識別消息類型
        val contentElement = jsonObject.get("content")
        if (contentElement == null || !contentElement.isJsonPrimitive) {
            Log.e(TAG, "缺少或無效的'content'字段")
            throw JsonParseException("無效的MQTT消息：缺少content字段")
        }
        
        val content = contentElement.asString
        Log.d(TAG, "解析MQTT消息，content類型: $content")
        
        // 根據content類型反序列化為不同的子類
        return try {
            when (content) {
                MqttConstants.CONTENT_TYPE_LOCATION -> context.deserialize(json, LocationMessage::class.java)
                MqttConstants.CONTENT_TYPE_HEALTH -> context.deserialize(json, HealthMessage::class.java)
                MqttConstants.CONTENT_TYPE_DIAPER -> context.deserialize(json, DiaperMessage::class.java)
                else -> {
                    // 如果是未知類型但包含position字段，嘗試作為LocationMessage解析
                    if (jsonObject.has("position")) {
                        Log.d(TAG, "未知消息類型但包含位置信息，嘗試作為LocationMessage解析")
                        context.deserialize(json, LocationMessage::class.java)
                    } else {
                        Log.e(TAG, "無法識別的消息類型: $content")
                        throw JsonParseException("無法識別的MQTT消息類型: $content")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "反序列化MQTT消息時出錯: ${e.message}")
            throw JsonParseException("反序列化MQTT消息時出錯", e)
        }
    }
}

/**
 * 創建配置好的Gson實例的輔助方法
 */
object MqttGsonHelper {
    /**
     * 創建可用於MQTT消息反序列化的Gson實例
     */
    fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(MqttMessage::class.java, MqttMessageDeserializer())
            .create()
    }
}
