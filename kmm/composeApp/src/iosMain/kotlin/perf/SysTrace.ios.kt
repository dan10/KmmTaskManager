package perf

import com.danioliveira.taskmanager.perf.native.TaskItTracer
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation using Swift wrapper for os_signpost.
 * Markers appear in Instruments as signpost intervals under "TaskItTrace".
 */
@OptIn(ExperimentalForeignApi::class)
actual object SysTrace {

    actual fun beginAsync(name: String): Long {
        // Swift's UInt64 is represented as ULong in Kotlin.
        // We convert it to Long to match the common `expect` interface.
        return TaskItTracer.beginAsyncWithName(name).toLong()
    }

    actual fun endAsync(name: String, cookie: Long) {
        // We convert the Long cookie back to ULong for the Swift function.
        TaskItTracer.endAsyncWithName(name, cookie.toULong())
    }
}