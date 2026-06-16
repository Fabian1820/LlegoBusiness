package com.llego.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.llego.business.orders.data.notification.NotificationServiceFactory
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.objc.sel_registerName

private const val IOS_EDGE_BACK_MIN_TRANSLATION = 50.0

@OptIn(ExperimentalForeignApi::class)
private class IosEdgeBackGestureHandler : NSObject() {
    @ObjCAction
    fun handleEdgePan(gesture: UIScreenEdgePanGestureRecognizer) {
        if (gesture.state != UIGestureRecognizerStateEnded) return

        val translationX = gesture.translationInView(gesture.view).useContents { x }
        if (translationX >= IOS_EDGE_BACK_MIN_TRANSLATION) {
            IosNativeBackDispatcher.dispatchBack()
        }
    }
}

private var iosEdgeBackGestureHandlerRef: IosEdgeBackGestureHandler? = null

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    // Solicita permisos de notificación al arrancar (idempotente: si ya están
    // concedidos/denegados iOS responde sin mostrar diálogo).
    NotificationServiceFactory.create().requestNotificationPermission { /* ignore */ }

    val viewController = ComposeUIViewController {
        val appContainer = remember { AppContainer() }

        val viewModels = remember {
            appContainer.createAppViewModels()
        }
        App(viewModels)
    }

    val handler = IosEdgeBackGestureHandler()
    iosEdgeBackGestureHandlerRef = handler

    val edgePanGesture = UIScreenEdgePanGestureRecognizer(
        target = handler,
        action = sel_registerName("handleEdgePan:")
    )
    edgePanGesture.edges = UIRectEdgeLeft
    edgePanGesture.cancelsTouchesInView = false
    viewController.view.addGestureRecognizer(edgePanGesture)

    return viewController
}
