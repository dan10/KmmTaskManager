package perf

import androidx.compose.runtime.*

/**
 * Composable that traces the lifecycle of a screen or component.
 * Emits beginAsync on creation and endAsync on disposal.
 * 
 * Usage:
 * ```
 * @Composable
 * fun LoginScreen() {
 *     TraceLifecycle("LoginScreen")
 *     // ... your UI
 * }
 * ```
 * 
 * The trace will appear as "act:LoginScreen" in Perfetto/Instruments.
 */
@Composable
fun TraceLifecycle(name: String) {
    val cookie = remember {
        SysTrace.beginAsync(name)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            SysTrace.endAsync(name, cookie)
        }
    }
}

/**
 * Composable that wraps content with lifecycle tracing.
 * 
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     TraceSection("MyScreen") {
 *         // ... your UI
 *     }
 * }
 * ```
 */
@Composable
fun TraceSection(name: String, content: @Composable () -> Unit) {
    TraceLifecycle(name)
    content()
}

