package com.example.myapplication.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LanguageManager {
    // 語言模式：true 為中文，false 為英文
    var isChineseLanguage by mutableStateOf(true)
    
    // 切換語言
    fun toggleLanguage() {
        isChineseLanguage = !isChineseLanguage
    }
    
    // 設置特定語言
    fun setLanguage(isChinese: Boolean) {
        isChineseLanguage = isChinese
    }
} 