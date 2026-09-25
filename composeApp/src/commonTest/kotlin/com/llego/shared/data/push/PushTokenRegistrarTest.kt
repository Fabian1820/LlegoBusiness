package com.llego.shared.data.push

import com.llego.multiplatform.graphql.type.DevicePlatformEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Registro del token de push: cuándo se envía y con qué JWT. El envío al backend se
 * reemplaza por un closure que captura las llamadas, sin tocar la red.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PushTokenRegistrarTest {

    private data class Sent(val token: String, val jwt: String?, val bundleId: String?)

    private val sent = mutableListOf<Sent>()
    private var storedJwt: String? = null

    private fun device(token: String = "tok-1") = PushTokenRegistrar.DeviceInfo(
        token = token,
        platform = DevicePlatformEnum.IOS,
        bundleId = "com.llego.business.LlegoBusiness",
        appVersion = "1.0",
        osVersion = "18.0",
    )

    private fun TestScope.useTestScope() {
        PushTokenRegistrar.scope = this
        PushTokenRegistrar.sender = { info, jwt -> sent += Sent(info.token, jwt, info.bundleId) }
    }

    @BeforeTest
    fun setUp() {
        PushTokenRegistrar.resetForTests()
        sent.clear()
        storedJwt = null
    }

    @AfterTest
    fun tearDown() {
        PushTokenRegistrar.resetForTests()
        PushTokenRegistrar.scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Test
    fun deviceToken_withSession_sendsStoredJwt() = runTest {
        useTestScope()
        storedJwt = "jwt-owner"
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()

        assertEquals(listOf(Sent("tok-1", "jwt-owner", "com.llego.business.LlegoBusiness")), sent)
    }

    @Test
    fun deviceToken_withoutSession_sendsNullJwt() = runTest {
        useTestScope()
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()

        assertEquals(1, sent.size)
        assertNull(sent.single().jwt)
    }

    @Test
    fun deviceToken_includesBusinessBundleId() = runTest {
        useTestScope()
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()

        assertTrue(sent.single().bundleId!!.startsWith("com.llego.business"))
    }

    @Test
    fun sameDeviceTokenTwice_sendsOnlyOnce() = runTest {
        useTestScope()
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onDeviceToken(device())
        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()

        assertEquals(1, sent.size)
    }

    @Test
    fun refreshedDeviceToken_isSentAgain() = runTest {
        useTestScope()
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onDeviceToken(device("tok-1"))
        PushTokenRegistrar.onDeviceToken(device("tok-2"))
        advanceUntilIdle()

        assertEquals(listOf("tok-1", "tok-2"), sent.map { it.token })
    }

    @Test
    fun sessionStarted_withoutDeviceToken_sendsNothing() = runTest {
        useTestScope()
        storedJwt = "jwt-owner"
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onSessionStarted()
        PushTokenRegistrar.onSessionEnded()
        advanceUntilIdle()

        assertTrue(sent.isEmpty())
    }

    @Test
    fun login_reRegistersWithNewJwt() = runTest {
        useTestScope()
        PushTokenRegistrar.initialize { storedJwt }
        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()
        sent.clear()

        storedJwt = "jwt-after-login"
        PushTokenRegistrar.onSessionStarted()
        advanceUntilIdle()

        assertEquals(listOf(Sent("tok-1", "jwt-after-login", "com.llego.business.LlegoBusiness")), sent)
    }

    @Test
    fun logout_reRegistersWithoutJwt_evenIfJwtStillStored() = runTest {
        useTestScope()
        storedJwt = "jwt-owner"
        PushTokenRegistrar.initialize { storedJwt }
        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()
        sent.clear()

        // Aunque el almacenamiento aún tenga el JWT, el logout debe desvincular el token
        PushTokenRegistrar.onSessionEnded()
        advanceUntilIdle()

        assertEquals(1, sent.size)
        assertNull(sent.single().jwt)
    }

    @Test
    fun logoutThenLogin_areSentInOrder() = runTest {
        useTestScope()
        storedJwt = "jwt-a"
        PushTokenRegistrar.initialize { storedJwt }
        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()
        sent.clear()

        PushTokenRegistrar.onSessionEnded()
        storedJwt = "jwt-b"
        PushTokenRegistrar.onSessionStarted()
        advanceUntilIdle()

        assertEquals(listOf(null, "jwt-b"), sent.map { it.jwt })
    }

    @Test
    fun tokenArrivingBeforeInitialize_isSyncedOnInitializeWithJwt() = runTest {
        useTestScope()
        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()
        sent.clear()

        storedJwt = "jwt-restored"
        PushTokenRegistrar.initialize { storedJwt }
        advanceUntilIdle()

        assertEquals(listOf("jwt-restored"), sent.map { it.jwt })
    }

    @Test
    fun secondInitialize_isIgnored() = runTest {
        useTestScope()
        PushTokenRegistrar.initialize { "jwt-first" }
        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()
        sent.clear()

        PushTokenRegistrar.initialize { "jwt-second" }
        PushTokenRegistrar.onSessionStarted()
        advanceUntilIdle()

        assertEquals(listOf("jwt-first"), sent.map { it.jwt })
    }

    @Test
    fun failedSend_doesNotBlockNextSends() = runTest {
        useTestScope()
        var fail = true
        PushTokenRegistrar.sender = { info, jwt ->
            if (fail) {
                fail = false
                error("sin red")
            }
            sent += Sent(info.token, jwt, info.bundleId)
        }
        PushTokenRegistrar.initialize { storedJwt }

        PushTokenRegistrar.onDeviceToken(device())
        advanceUntilIdle()
        storedJwt = "jwt-owner"
        PushTokenRegistrar.onSessionStarted()
        advanceUntilIdle()

        assertEquals(listOf("jwt-owner"), sent.map { it.jwt })
    }
}
