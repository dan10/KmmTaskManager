package com.danioliveira.appium.config

/**
 * Configuration for benchmark runs, parsed from system properties.
 */
data class BenchmarkConfig(
    val platform: Platform,
    val app: App,
    val scenario: String,
    val runs: Int,
    val warmup: Int,
    val deviceName: String?,
    val udid: String?,
    val apkPath: String?,
    val ipaPath: String?,
    val bundleId: String?,
    val packageName: String?,
    val registerEveryCycle: Boolean,
    val appiumServerUrl: String
) {
    companion object {
        fun fromSystemProperties(): BenchmarkConfig {
            val platformStr = System.getProperty("platform", "android")
            val appStr = System.getProperty("app", "kmm")
            val scenario = System.getProperty("scenario", "all")
            val runs = System.getProperty("runs", "15").toInt()
            val warmup = System.getProperty("warmup", "1").toInt()
            val deviceName = System.getProperty("deviceName")
            val udid = System.getProperty("udid")
            val apkPath = System.getProperty("apk")
            val ipaPath = System.getProperty("ipa")
            val bundleId = System.getProperty("bundleId")
            val packageName = System.getProperty("package")
            val registerEveryCycle = System.getProperty("registerEveryCycle", "false").toBoolean()
            
            return BenchmarkConfig(
                platform = Platform.fromString(platformStr),
                app = App.fromString(appStr),
                scenario = scenario,
                runs = runs,
                warmup = warmup,
                deviceName = deviceName,
                udid = udid,
                apkPath = apkPath,
                ipaPath = ipaPath,
                bundleId = bundleId,
                packageName = packageName,
                registerEveryCycle = registerEveryCycle,
                appiumServerUrl = System.getProperty("appiumUrl", "http://127.0.0.1:4723")
            )
        }
    }
}

enum class Platform {
    ANDROID, IOS;
    
    companion object {
        fun fromString(s: String): Platform = when (s.lowercase()) {
            "android" -> ANDROID
            "ios" -> IOS
            else -> throw IllegalArgumentException("Unknown platform: $s")
        }
    }
}

enum class App {
    KMM, FLUTTER;
    
    companion object {
        fun fromString(s: String): App = when (s.lowercase()) {
            "kmm" -> KMM
            "flutter" -> FLUTTER
            else -> throw IllegalArgumentException("Unknown app: $s")
        }
    }
}


