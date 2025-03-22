package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 自定义的深色主题配色方案
private val CustomDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

// 自定义的浅色主题配色方案
private val CustomLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    error = LightError,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
)

// 定义一个全局可访问的主题模式状态
object ThemeManager {
    var isDarkTheme by mutableStateOf(false)
    
    // 添加主題切換監聽器
    private val themeChangeListeners = mutableListOf<() -> Unit>()
    
    /**
     * 切換深淺模式
     */
    fun toggleTheme() {
        // 先執行所有監聽器，確保清理工作在主題切換前完成
        themeChangeListeners.forEach { it() }
        
        // 然後切換主題
        isDarkTheme = !isDarkTheme
    }
    
    /**
     * 新增主題切換監聽器
     * @param listener 當主題將要切換時執行的回調
     */
    fun addThemeChangeListener(listener: () -> Unit) {
        themeChangeListeners.add(listener)
    }
    
    /**
     * 移除主題切換監聽器
     * @param listener 要移除的監聽器
     */
    fun removeThemeChangeListener(listener: () -> Unit) {
        themeChangeListeners.remove(listener)
    }
}

// 创建一个composition local来访问当前的主题模式
val LocalThemeIsDark = staticCompositionLocalOf { false }

@Composable
fun MyApplicationTheme(
    // 使用ThemeManager的状态，而不是系统的深色模式
    darkTheme: Boolean = ThemeManager.isDarkTheme,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 默认禁用动态颜色，使用我们自定义的颜色方案
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CustomDarkColorScheme
        else -> CustomLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // 设置状态栏为透明，确保内容可以延伸到状态栏
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // 更新状态栏颜色
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 