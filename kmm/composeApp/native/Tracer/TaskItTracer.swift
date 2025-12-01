import Foundation
import os

/// TaskItTracer provides a Swift wrapper for OSSignposter functionality
/// that can be called from Kotlin/Native code.
///
/// Uses the modern OSSignposter API (iOS 15+) instead of deprecated os_signpost.
@objc public class TaskItTracer: NSObject {
    /// Create a signposter instance for your app's subsystem.
    private static let signposter = OSSignposter(subsystem: "com.danioliveira.taskmanager", category: "KMM-Trace")

    /// Begin an async signpost interval.
    ///
    /// - Parameter name: The name/label for this trace event
    /// - Returns: A unique signpost ID that must be passed to endAsync
    @objc public static func beginAsync(name: String) -> UInt64 {
        let signpostID = signposter.makeSignpostID()

        // Begin the interval and discard the returned state since we'll recreate it later
        let _ = signposter.beginInterval("TaskItTrace", id: signpostID, "\(name)")

        // Return the signpost ID's raw value as the cookie
        return signpostID.rawValue
    }

    /// End an async signpost interval.
    ///
    /// - Parameters:
    ///   - name: The same name used in beginAsync (unused but kept for API consistency)
    ///   - cookie: The signpost ID returned from beginAsync
    @objc public static func endAsync(name: String, cookie: UInt64) {
        // Recreate the signpost ID from the cookie
        let signpostID = OSSignpostID(cookie)

        // Recreate the interval state using the signpost ID
        let state = OSSignpostIntervalState.beginState(id: signpostID)

        // End the interval using the recreated state
        signposter.endInterval("TaskItTrace", state)
    }
}