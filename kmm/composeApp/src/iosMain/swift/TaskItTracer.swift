import Foundation
import os.log

/// TaskItTracer provides a Swift wrapper for os_signpost functionality
/// that can be called from Kotlin/Native code.
///
/// The class and its methods must be public and marked with @objc
/// to be visible from Kotlin through the cinterop layer.
@objc public class TaskItTracer: NSObject {
    /// Create a logger instance for your app's subsystem.
    private static let log = OSLog(subsystem: "com.danioliveira.taskmanager", category: "KMM-Trace")

    /// Begin an async signpost interval.
    ///
    /// - Parameter name: The name/label for this trace event
    /// - Returns: A unique signpost ID that must be passed to endAsync
    @objc public static func beginAsync(name: String) -> UInt64 {
        let signpostID = OSSignpostID(log: log)
        os_signpost(.begin, log: log, name: "TaskItTrace", signpostID: signpostID, "act: %{public}s", name)
        return signpostID.rawValue
    }

    /// End an async signpost interval.
    ///
    /// - Parameters:
    ///   - name: The same name used in beginAsync
    ///   - cookie: The signpost ID returned from beginAsync
    @objc public static func endAsync(name: String, cookie: UInt64) {
        let signpostID = OSSignpostID(rawValue: cookie)
        os_signpost(.end, log: log, name: "TaskItTrace", signpostID: signpostID, "act: %{public}s", name)
    }
}

