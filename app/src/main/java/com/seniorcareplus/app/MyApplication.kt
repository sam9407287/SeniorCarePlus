package com.seniorcareplus.app

import android.app.Application
import android.util.Log

class MyApplication : Application() {
    
    companion object {
        lateinit var instance: MyApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.d("MyApplication", "應用程序初始化成功")
    }
} 