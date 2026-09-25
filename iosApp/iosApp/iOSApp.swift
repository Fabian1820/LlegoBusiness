import SwiftUI
import UIKit
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

/// Registro de push remoto. El delegate de UNUserNotificationCenter lo maneja
/// IosNotificationService (Kotlin); aquí solo se obtiene el token de APNs.
final class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // El token se entrega aunque el usuario no haya dado permiso de alertas;
        // el permiso lo pide IosNotificationService.
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        PushTokenBridgeKt.registerApnsDeviceToken(tokenHex: token)
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("[Push] Error registrando para push remoto: \(error.localizedDescription)")
    }
}
