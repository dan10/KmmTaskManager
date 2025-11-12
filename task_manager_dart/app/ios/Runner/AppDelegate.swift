import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    
    // Register performance tracing channel
    if let controller = window?.rootViewController as? FlutterViewController {
      PerfTraceChannel.register(with: controller.engine!.registrarForPlugin("PerfTraceChannel")!)
    }
    
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
