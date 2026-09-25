package com.llego.shared.data.push

import com.llego.multiplatform.graphql.type.DevicePlatformEnum
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

/**
 * Punto de entrada desde Swift (`PushTokenBridgeKt.registerApnsDeviceToken`).
 * Lo llama el AppDelegate en `didRegisterForRemoteNotificationsWithDeviceToken`.
 */
fun registerApnsDeviceToken(tokenHex: String) {
    PushTokenRegistrar.onDeviceToken(
        PushTokenRegistrar.DeviceInfo(
            token = tokenHex,
            platform = DevicePlatformEnum.IOS,
            bundleId = NSBundle.mainBundle.bundleIdentifier,
            appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String,
            osVersion = UIDevice.currentDevice.systemVersion,
        )
    )
}
