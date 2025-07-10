package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UnitTests {

    private class TestObserver : Observer {
        var updateCalled = false
        var updatedShipment: Shipment? = null

        override fun update(shipment: Shipment) {
            updateCalled = true
            updatedShipment = shipment
        }
    }

    @Test
    fun testRegisterObserver() {
        val shipment = Shipment("123")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addNote("Test note")
        assertTrue(observer.updateCalled)
        assertEquals(shipment, observer.updatedShipment)
    }

    @Test
    fun testRemoveObserver() {
        val shipment = Shipment("456")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.removeObserver(observer)
        shipment.addNote("Another test note")
        assertTrue(!observer.updateCalled)
    }

    @Test
    fun testNotifyObserversOnAddNote() {
        val shipment = Shipment("789")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addNote("A note to trigger notification")
        assertTrue(observer.updateCalled)
        assertEquals(shipment, observer.updatedShipment)
    }

    @Test
    fun testNotifyObserversOnAddUpdate() {
        val shipment = Shipment("101")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.addUpdate("An update to trigger notification")
        assertTrue(observer.updateCalled)
        assertEquals(shipment, observer.updatedShipment)
    }

    @Test
    fun testNotifyObserversOnApplyUpdate() {
        val shipment = Shipment("112")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.applyUpdate({ s, _, _ -> s.status = "shipped" }, 12345L, null)
        assertTrue(observer.updateCalled)
        assertEquals(shipment, observer.updatedShipment)
        assertEquals("shipped", shipment.status)
    }

    @Test
    fun testStartTrackingSetsInitialState() {
        val shipment = Shipment("S1000")
        val helper = TrackerViewHelper(shipment)
        helper.startTracking()
        assertEquals(shipment.id, helper.latestShipment?.id, "After startTracking, the shipment IDs should match.")
    }

    @Test
    fun testUpdateReceivesShipmentChanges() {
        val shipment = Shipment("S2000")
        val helper = TrackerViewHelper(shipment)
        helper.startTracking()

        shipment.addNote("Package is out for delivery")

        assertEquals(shipment.id, helper.latestShipment?.id, "The shipment IDs should match after an update.")
        assertTrue(
            helper.latestShipment?.notes?.contains("Package is out for delivery") ?: false,
            "The notes in the helper's shipment should reflect the update."
        )
    }

    @Test
    fun testStopTrackingCeasesUpdates() {
        val shipment = Shipment("S3000")
        val helper = TrackerViewHelper(shipment)
        helper.startTracking()

        shipment.addNote("First update")
        assertEquals(1, helper.latestShipment?.notes?.size)

        helper.stopTracking()
        shipment.addNote("Second update after stopping")

        assertEquals(1, helper.latestShipment?.notes?.size, "latestShipment should not be updated after stopTracking.")
    }

    // --- Update Strategy Tests ---

    private val created: (Shipment, Long, String?) -> Unit = { shipment, _, _ ->
        shipment.status = "created"
    }

    private val shipped: (Shipment, Long, String?) -> Unit = { shipment, timestamp, newLocation ->
        shipment.status = "shipped"
        newLocation?.let { shipment.location = it }
        shipment.expectedDelivery = timestamp
    }

    private val delayed: (Shipment, Long, String?) -> Unit = { shipment, timestamp, reason ->
        shipment.status = "delayed"
        reason?.let { shipment.addNote("Delayed: $it") }
        shipment.expectedDelivery = timestamp
    }

    @Test
    fun testApplyUpdateWithShippedStrategy() {
        val shipment = Shipment("S4000")
        val newLocation = "Warehouse B"
        val deliveryDate = System.currentTimeMillis() + 100000L

        shipment.applyUpdate(shipped, deliveryDate, newLocation)

        assertEquals("shipped", shipment.status)
        assertEquals(newLocation, shipment.location)
        assertEquals(deliveryDate, shipment.expectedDelivery)
    }

    @Test
    fun testApplyUpdateWithDelayedStrategy() {
        val shipment = Shipment("S5000")
        val reason = "Weather conditions"
        val newDeliveryDate = System.currentTimeMillis() + 200000L

        shipment.applyUpdate(delayed, newDeliveryDate, reason)

        assertEquals("delayed", shipment.status)
        assertEquals(newDeliveryDate, shipment.expectedDelivery)
        assertTrue(shipment.notes.contains("Delayed: $reason"))
    }

    @Test
    fun testApplyUpdateNotifiesObservers() {
        val shipment = Shipment("S6000")
        val observer = TestObserver()
        shipment.registerObserver(observer)

        shipment.applyUpdate(created, System.currentTimeMillis(), null)

        assertTrue(observer.updateCalled, "Observer should be notified when an update is applied.")
        assertEquals(shipment, observer.updatedShipment)
    }

    // --- Tracking Simulator Test ---

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFullSimulation() = runTest {
        val simulator = TrackingSimulator(this.coroutineContext[kotlin.coroutines.ContinuationInterceptor] as kotlinx.coroutines.CoroutineDispatcher)
        simulator.start()

        advanceTimeBy(81000)

        // Check final state of s10000 (lost)
        val shipment1 = simulator.shipments["s10000"]
        assertNotNull(shipment1)
        assertEquals("lost", shipment1.status)

        // Check final state of s10003 (canceled)
        val shipment2 = simulator.shipments["s10003"]
        assertNotNull(shipment2)
        assertEquals("canceled", shipment2.status)

        // Check final state of s10006 (delivered)
        val shipment3 = simulator.shipments["s10006"]
        assertNotNull(shipment3)
        assertEquals("delivered", shipment3.status)
    }
}