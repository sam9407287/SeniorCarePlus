package com.seniorcareplus.app.mqtt

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

/**
 * 為字符串ID型別創建自定义的TypeAdapter
 * 確保即使ID看起來像數字也被當作字串處理
 */
class StringIdTypeAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): String {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            return ""
        }
        // 始終以字符串形式讀取ID，無論其内容是什麼
        return reader.nextString()
    }
}

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
        
        // 檢查ID字段，確保以字符串形式處理
        if (jsonObject.has("id") && jsonObject.get("id").isJsonPrimitive) {
            try {
                val idElement = jsonObject.get("id")
                val idString = idElement.asString
                Log.d(TAG, "正在處理ID: $idString (以字符串形式)")
            } catch (e: Exception) {
                Log.w(TAG, "讀取ID字段時出現警告: ${e.message}")
            }
        }
        
        // 使用特定類型解析器而非通用context.deserialize
        return try {
            when (content) {
                MqttConstants.CONTENT_TYPE_LOCATION -> {
                    // 直接使用Gson解析為具體類型而非通過context
                    val gson = GsonBuilder()
                        .registerTypeAdapter(String::class.java, StringIdTypeAdapter())
                        .create()
                    val result = gson.fromJson(json, LocationMessage::class.java)
                    Log.d(TAG, "成功解析LocationMessage: id=${result.id}, position=${result.position}")
                    result
                }
                MqttConstants.CONTENT_TYPE_HEALTH -> {
                    val gson = GsonBuilder().create()
                    gson.fromJson(json, HealthMessage::class.java)
                }
                MqttConstants.CONTENT_TYPE_DIAPER -> {
                    val gson = GsonBuilder().create()
                    gson.fromJson(json, DiaperMessage::class.java)
                }
                else -> {
                    // 如果是未知類型但包含position字段，嘗試作為LocationMessage解析
                    if (jsonObject.has("position")) {
                        Log.d(TAG, "未知消息類型但包含位置信息，嘗試作為LocationMessage解析")
                        val gson = GsonBuilder()
                            .registerTypeAdapter(String::class.java, StringIdTypeAdapter())
                            .create()
                        val result = gson.fromJson(json, LocationMessage::class.java)
                        Log.d(TAG, "成功解析未知類型的LocationMessage: id=${result.id}, position=${result.position}")
                        result
                    } else {
                        Log.e(TAG, "無法識別的消息類型: $content")
                        throw JsonParseException("無法識別的MQTT消息類型: $content")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "反序列化MQTT消息時出錯: ${e.message}, ${e.javaClass.simpleName}, ${e.stackTrace.joinToString(",")}")
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
            // 註冊自定義的StringIdTypeAdapter，確保ID字段永遠以字符串形式處理
            .registerTypeAdapter(String::class.java, StringIdTypeAdapter())
            .create()
    }
}
