package com.example.task_manager_app

import android.os.Trace
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.atomic.AtomicInteger

/**
 * Platform channel for performance tracing.
 * Emits markers visible in Perfetto system traces.
 */
class PerfTraceChannel(flutterEngine: FlutterEngine) {
    companion object {
        private val nextCookie = AtomicInteger(1)
    }
    
    init {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "perf.trace"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "begin" -> {
                    val name = call.argument<String>("name")
                    if (name != null) {
                        val cookie = nextCookie.getAndIncrement()
                        // Prefix with "act:" to match KMM convention
                        Trace.beginAsyncSection("act:$name", cookie)
                        result.success(cookie)
                    } else {
                        result.error("INVALID_ARGUMENT", "name is required", null)
                    }
                }
                "end" -> {
                    val name = call.argument<String>("name")
                    val cookie = call.argument<Int>("cookie")
                    if (name != null && cookie != null) {
                        Trace.endAsyncSection("act:$name", cookie)
                        result.success(null)
                    } else {
                        result.error("INVALID_ARGUMENT", "name and cookie are required", null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }
}

