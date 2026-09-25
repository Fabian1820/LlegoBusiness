package com.llego.business.orders.data.notification

import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Coordinates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Cambio de sucursal al tocar "Cambiar para ver" en la notificación de un pedido
 * de otra sucursal.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BranchSwitchHandlerTest {

    private fun branch(id: String, name: String) = Branch(
        id = id,
        businessId = "biz-1",
        name = name,
        coordinates = Coordinates(coordinates = listOf(-82.36, 23.11)),
        phone = "",
        createdAt = "2026-01-01T00:00:00Z",
    )

    private val centro = branch("b-centro", "Centro")
    private val playa = branch("b-playa", "Playa")

    @Test
    fun notificationTap_storesPendingSwitch() {
        val handler = BranchSwitchHandler()

        handler.handleBranchSwitchFromNotification("b-playa", "ord-1", "Playa")

        assertTrue(handler.hasPendingSwitch())
        assertEquals(BranchSwitchEvent("b-playa", "ord-1", "Playa"), handler.pendingSwitchEvent.value)
    }

    @Test
    fun switchToOtherBranch_changesBranch_emitsSuccess_andNavigates() = runTest {
        val handler = BranchSwitchHandler()
        val results = mutableListOf<BranchSwitchResult>()
        val navigations = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { handler.switchResult.toList(results) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { handler.navigateToOrder.toList(navigations) }
        var selected: Branch? = null

        handler.handleBranchSwitchFromNotification("b-playa", "ord-1")
        handler.executePendingSwitch(listOf(centro, playa), currentBranch = centro) { selected = it }

        assertEquals(playa, selected)
        assertEquals(listOf<BranchSwitchResult>(BranchSwitchResult.Success("Centro", "Playa", "ord-1")), results)
        assertEquals(listOf("ord-1"), navigations)
        assertFalse(handler.hasPendingSwitch())
    }

    @Test
    fun sameBranch_onlyNavigates_withoutChangingBranch() = runTest {
        val handler = BranchSwitchHandler()
        val results = mutableListOf<BranchSwitchResult>()
        val navigations = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { handler.switchResult.toList(results) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { handler.navigateToOrder.toList(navigations) }
        var changed = false

        handler.handleBranchSwitchFromNotification("b-centro", "ord-2")
        handler.executePendingSwitch(listOf(centro, playa), currentBranch = centro) { changed = true }

        assertFalse(changed)
        assertTrue(results.isEmpty())
        assertEquals(listOf("ord-2"), navigations)
        assertFalse(handler.hasPendingSwitch())
    }

    @Test
    fun unknownBranch_emitsBranchNotFound_andClearsPending() = runTest {
        val handler = BranchSwitchHandler()
        val results = mutableListOf<BranchSwitchResult>()
        val navigations = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { handler.switchResult.toList(results) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { handler.navigateToOrder.toList(navigations) }

        handler.handleBranchSwitchFromNotification("b-borrada", "ord-3")
        handler.executePendingSwitch(listOf(centro, playa), currentBranch = centro) {}

        assertEquals(listOf<BranchSwitchResult>(BranchSwitchResult.BranchNotFound), results)
        assertTrue(navigations.isEmpty())
        assertFalse(handler.hasPendingSwitch())
    }

    @Test
    fun executeWithoutPendingSwitch_doesNothing() = runTest {
        val handler = BranchSwitchHandler()
        var changed = false

        handler.executePendingSwitch(listOf(centro, playa), currentBranch = centro) { changed = true }

        assertFalse(changed)
        assertNull(handler.pendingSwitchEvent.value)
    }

    @Test
    fun clearPendingSwitch_discardsEvent() {
        val handler = BranchSwitchHandler()
        handler.handleBranchSwitchFromNotification("b-playa", "ord-4")

        handler.clearPendingSwitch()

        assertFalse(handler.hasPendingSwitch())
    }

    @Test
    fun newOrderSoundPreference_defaultsOn_andCanBeToggled() {
        try {
            assertTrue(NotificationPreferences.newOrderSoundEnabled)
            NotificationPreferences.setNewOrderSoundEnabled(false)
            assertFalse(NotificationPreferences.newOrderSoundEnabled)
        } finally {
            NotificationPreferences.setNewOrderSoundEnabled(true)
        }
    }
}
