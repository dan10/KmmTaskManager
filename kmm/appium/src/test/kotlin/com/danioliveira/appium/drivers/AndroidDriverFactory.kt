package com.danioliveira.appium.drivers

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import io.appium.java_client.Setting
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
            // UiAutomator2 specific settings for better compatibility
            .setSkipServerInstallation(false)  // Ensure UiAutomator2 server is always installed
            .setSystemPort(8200)  // Set a fixed system port for UiAutomator2 server
            .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(60))  // Increase server launch timeout
            .setUiautomator2ServerInstallTimeout(Duration.ofSeconds(60))  // Increase server install timeout
        
        //config.apkPath?.let { options.setApp(it) }
        
        return AndroidDriver(URL(APPIUM_URL), options).also {
            it.setSetting(Setting.ALLOW_INVISIBLE_ELEMENTS, true)
        }
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


