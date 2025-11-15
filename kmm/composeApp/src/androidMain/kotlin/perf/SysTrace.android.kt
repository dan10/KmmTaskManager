package perf

import androidx.tracing.Trace
import java.util.concurrent.atomic.AtomicInteger

/**
 * Android implementation using androidx.tracing.Trace.
 *
 * **Using AndroidX Tracing** with the "app" atrace category enabled in Perfetto configuration.
 * This ensures trace events are properly captured when using Perfetto's ftrace data source.
 *
 * Markers appear in Perfetto system traces as async slices with "act:" prefix
 * for easy filtering and identification of app-level performance sections.
 *
 * **Requirements:**
 * - Perfetto config must include "app" in atrace_categories
 * - atrace_apps must be set to the app package name
 *
 * **Note:** Async sections require API 29+ (Android 10+). For older devices, these
 * calls will be no-ops, but the app will still function normally.
 *
 * @see <a href="https://developer.android.com/jetpack/androidx/releases/tracing">androidx.tracing</a>
 */
actual object SysTrace {
    private val nextCookie = AtomicInteger(1)

    /**
     * Begin an async trace section.
     *
     * This creates an async slice in the Perfetto trace that can span across
     * multiple threads and doesn't need to be ended on the same call stack.
     * Perfect for tracking screen lifecycle and long-running operations.
     *
     * @param name The name of the trace section (will be prefixed with "act:")
     * @return A cookie to pass to [endAsync] to end this section
     */
    actual fun beginAsync(name: String): Long {
        val cookie = nextCookie.getAndIncrement()
        // Prefix with "act:" to easily filter in Perfetto
        // Note: beginAsyncSection requires API 29+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Trace.beginAsyncSection("act:$name", cookie)
        }
        return cookie.toLong()
    }

    /**
     * End an async trace section.
     *
     * @param name The name of the trace section (must match [beginAsync])
     * @param cookie The cookie returned from [beginAsync]
     */
    actual fun endAsync(name: String, cookie: Long) {
        // Note: endAsyncSection requires API 29+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Trace.endAsyncSection("act:$name", cookie.toInt())
        }
    }
}

