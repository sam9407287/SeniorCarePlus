package com.seniorcareplus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.seniorcareplus.app.ui.theme.LanguageManager
import com.seniorcareplus.app.ui.theme.ThemeManager

/**
 * 可重用的设置按钮组件，包含语言和主题切换功能
 */
@Composable
fun SettingsButton() {
    Box {
        var showSettingsMenu by remember { mutableStateOf(false) }
        val isChineseLanguage = LanguageManager.isChineseLanguage
        val isDarkTheme = ThemeManager.isDarkTheme
        
        // 设置按钮
        IconButton(
            onClick = { showSettingsMenu = true },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = if (isChineseLanguage) "設置" else "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 设置下拉菜单
        DropdownMenu(
            expanded = showSettingsMenu,
            onDismissRequest = { showSettingsMenu = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .width(220.dp)
        ) {
            // 语言切换选项
            DropdownMenuItem(
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isChineseLanguage) "切換為英文" else "Switch to Chinese",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                onClick = { 
                    LanguageManager.toggleLanguage()
                    showSettingsMenu = false
                }
            )
            
            Divider()
            
            // 主题切换选项
            DropdownMenuItem(
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isDarkTheme) {
                                if (isChineseLanguage) "切換為亮色模式" else "Switch to Light Mode"
                            } else {
                                if (isChineseLanguage) "切換為暗色模式" else "Switch to Dark Mode"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                onClick = { 
                    ThemeManager.toggleTheme()
                    showSettingsMenu = false
                }
            )
        }
    }
}
