package perf

/**
 * Cross-platform system tracing API.
 * Emits markers visible in Perfetto (Android) and Instruments (iOS).
 * Use for lifecycle events, action boundaries, etc.
 */
expect object SysTrace {
    /**
     * Begin an async trace section.
     * Returns a cookie/token that must be passed to endAsync.
     * 
     * @param name Name of the section (will be prefixed with "act:" in traces)
     * @return Cookie to pass to endAsync
     */
    fun beginAsync(name: String): Long
    
    /**
     * End an async trace section.
     * 
     * @param name Same name used in beginAsync
     * @param cookie Cookie returned from beginAsync
     */
    fun endAsync(name: String, cookie: Long)
}

