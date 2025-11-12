package com.danioliveira.appium.drivers

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import java.net.URL
import java.time.Duration

object AndroidDriverFactory {
    private const val APPIUM_URL = "http://127.0.0.1:4723"
    
    fun create(config: BenchmarkConfig): AndroidDriver {
        val options = UiAutomator2Options()
            .setDeviceName(config.deviceName ?: "Android Emulator")
            .setAppPackage(getPackageName(config.app))
            .setAppActivity(getMainActivity(config.app))
            .setAutoGrantPermissions(true)
            .setNoReset(false)
            .setFullReset(false)
            .setNewCommandTimeout(Duration.ofMinutes(10))
        
        //config.apkPath?.let { options.setApp(it) }
        
        return AndroidDriver(URL(APPIUM_URL), options)
    }
    
    private fun getPackageName(app: App): String = when (app) {
        App.KMM -> "com.danioliveira.taskmanager"
        App.FLUTTER -> "com.example.task_manager_app"
    }
    
    private fun getMainActivity(app: App): String = when (app) {
        App.KMM -> "com.danioliveira.taskmanager.MainActivity"
        App.FLUTTER -> "com.example.task_manager_app.MainActivity"
    }
}


