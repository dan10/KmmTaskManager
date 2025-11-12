package perf

import android.os.Build
import android.os.Trace
import androidx.annotation.RequiresApi
import java.util.concurrent.atomic.AtomicLong

/**
 * Android implementation using android.os.Trace.
 * Markers appear in Perfetto system traces as async slices.
 */
actual object SysTrace {
    private val nextCookie = AtomicLong(1)
    
    @RequiresApi(Build.VERSION_CODES.Q)
    actual fun beginAsync(name: String): Long {
        val cookie = nextCookie.getAndIncrement()
        // Prefix with "act:" to easily filter in Perfetto
        Trace.beginAsyncSection("act:$name", cookie.toInt())
        return cookie
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    actual fun endAsync(name: String, cookie: Long) {
        Trace.endAsyncSection("act:$name", cookie.toInt())
    }
}

