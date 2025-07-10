package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
}