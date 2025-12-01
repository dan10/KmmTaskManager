package com.danioliveira.appium.drivers

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.ios.options.XCUITestOptions
import io.appium.java_client.flutter.ios.FlutterIOSDriver
import java.net.URL
import java.time.Duration

object IOSDriverFactory {
    private const val APPIUM_URL = "http://127.0.0.1:4723"
    
    fun create(config: BenchmarkConfig): IOSDriver {
        val options = XCUITestOptions()
            .setBundleId(config.bundleId ?: getBundleId(config.app))  // Use config bundle ID if provided
            .setAutoAcceptAlerts(true)
            .setNewCommandTimeout(Duration.ofMinutes(10))
            .setShowXcodeLog(true)  // Show detailed Xcode build logs for debugging
        
        // Real device configuration
        // Simulators have UUID format (XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX with 4 hyphens at standard positions)
        // Real device hardware UDIDs have different format (e.g., 00008110-000C6DA40110401E - only 1 hyphen)
        val isRealDevice = config.udid != null && config.udid.count { it == '-' } == 1
        if (isRealDevice) {
            // Use pre-built WebDriverAgent (manually started from Xcode)
            // This is needed when you don't have a paid Apple Developer account
            options.setCapability("usePrebuiltWDA", true)
            options.setCapability("useNewWDA", false)
        }
        
        config.udid?.let { options.setUdid(it) }
        config.deviceName?.let { options.setDeviceName(it) }
        config.ipaPath?.let { options.setApp(it) }
        
        return when (config.app) {
            App.FLUTTER -> FlutterIOSDriver(URL(APPIUM_URL), options)
            App.KMM -> IOSDriver(URL(APPIUM_URL), options)
        }
    }
    
    private fun getBundleId(app: App): String = when (app) {
        App.KMM -> "com.danioliveira.taskmanager.KmmTaskManager"
        App.FLUTTER -> "com.danieloliveira.taskManagerApp"
    }
}


