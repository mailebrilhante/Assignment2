package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShipmentTest {

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
}