package com.seniorcareplus.app

import android.app.Application
import android.util.Log
import com.seniorcareplus.app.auth.UserManager

class MyApplication : Application() {
    
    companion object {
        lateinit var instance: MyApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化UserManager，檢查登錄狀態
        UserManager.initialize()
        
        Log.d("MyApplication", "應用程序初始化成功，已檢查登錄狀態")
    }
} 