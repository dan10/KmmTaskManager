import SwiftUI

@main
struct iOSApp: App {

    init() {
        KoinInitializerKt.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}