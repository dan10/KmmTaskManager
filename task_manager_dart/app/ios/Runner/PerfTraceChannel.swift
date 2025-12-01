import Flutter
import UIKit
import os.signpost

@available(iOS 12.0, *)
class PerfTraceChannel: NSObject, FlutterPlugin {
    private static let subsystem = "com.danieloliveira.taskManagerApp"
    private static let log = OSLog(subsystem: subsystem, category: "Performance")
    
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
            if let args = call.arguments as? [String: Any],
               let name = args["name"] as? String {
                beginTrace(name: name)
                // Return a dummy cookie (iOS doesn't need it)
                result(0)
            } else {
                result(FlutterError(code: "INVALID_ARGS", message: "Missing trace name", details: nil))
            }
            
        case "end":
            if let args = call.arguments as? [String: Any],
               let name = args["name"] as? String {
                endTrace(name: name)
                result(nil)
            } else {
                result(FlutterError(code: "INVALID_ARGS", message: "Missing trace name", details: nil))
            }
            
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func beginTrace(name: String) {
        if #available(iOS 12.0, *) {
            os_signpost(.begin, log: PerfTraceChannel.log, name: "PerformanceTrace", "%{public}s", name)
        }
    }
    
    private func endTrace(name: String) {
        if #available(iOS 12.0, *) {
            os_signpost(.end, log: PerfTraceChannel.log, name: "PerformanceTrace", "%{public}s", name)
        }
    }
}
