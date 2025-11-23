import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    
    // Register performance tracing channel for iOS signposts
    if let controller = window?.rootViewController as? FlutterViewController {
      let registrar = self.registrar(forPlugin: "PerfTraceChannel")!
      PerfTraceChannel.register(with: registrar)
    }
    
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
