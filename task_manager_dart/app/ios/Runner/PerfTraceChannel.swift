import Flutter
import os.signpost

/// Platform channel for performance tracing.
/// Emits markers visible in Instruments as signpost intervals.
class PerfTraceChannel: NSObject, FlutterPlugin {
    private static let log = OSLog(subsystem: "com.danioliveira.taskmanager", category: "Flutter")
    
    static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(
            name: "perf.trace",
            binaryMessenger: registrar.messenger()
        )
        let instance = PerfTraceChannel()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "begin":
            guard let args = call.arguments as? [String: Any],
                  let name = args["name"] as? String else {
                result(FlutterError(
                    code: "INVALID_ARGUMENT",
                    message: "name is required",
                    details: nil
                ))
                return
            }
            
            let signpostId = OSSignpostID(log: Self.log)
            // Prefix with "act:" to match KMM convention
            os_signpost(.begin, log: Self.log, name: "act", "act:%{public}s", name)
            
            // Return signpost ID as cookie
            result(Int(signpostId.rawValue))
            
        case "end":
            guard let args = call.arguments as? [String: Any],
                  let name = args["name"] as? String,
                  let cookie = args["cookie"] as? Int else {
                result(FlutterError(
                    code: "INVALID_ARGUMENT",
                    message: "name and cookie are required",
                    details: nil
                ))
                return
            }
            
            let signpostId = OSSignpostID(rawValue: UInt64(cookie))
            os_signpost(.end, log: Self.log, name: "act", "act:%{public}s", name)
            
            result(nil)
            
        default:
            result(FlutterMethodNotImplemented)
        }
    }
}

