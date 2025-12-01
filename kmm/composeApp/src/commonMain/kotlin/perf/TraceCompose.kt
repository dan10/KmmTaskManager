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
 * 
 * ## Naming Conventions
 * 
 * For consistency across the codebase, use these naming patterns:
 * 
 * - **Screens**: Use the exact composable function name
 *   - Example: `TraceLifecycle("LoginScreen")` for `LoginScreen()`
 *   - Example: `TraceLifecycle("TasksScreen")` for `TasksScreen()`
 * 
 * - **Bottom Sheets**: Use descriptive name with "BottomSheet" suffix
 *   - Example: `TraceLifecycle("TaskCreateBottomSheet")`
 *   - Example: `TraceLifecycle("CreateEditProjectBottomSheet")`
 * 
 * - **Events/Actions**: Use "event:" prefix for user actions
 *   - Example: `TraceLifecycle("event:TaskCreated")`
 *   - Example: `TraceLifecycle("event:LoginSubmit")`
 * 
 * - **Data Loading**: Use "load:" prefix for async data operations
 *   - Example: `TraceLifecycle("load:TaskList")`
 *   - Example: `TraceLifecycle("load:ProjectDetails")`
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

/**
 * Traces an async operation within a LaunchedEffect or similar coroutine scope.
 * Use this for marking data fetch operations or other async work.
 * 
 * Usage:
 * ```
 * LaunchedEffect(Unit) {
 *     traceAsync("load:TaskList") {
 *         // fetch data
 *     }
 * }
 * ```
 */
suspend fun <T> traceAsync(name: String, block: suspend () -> T): T {
    val cookie = SysTrace.beginAsync(name)
    try {
        return block()
    } finally {
        SysTrace.endAsync(name, cookie)
    }
}

