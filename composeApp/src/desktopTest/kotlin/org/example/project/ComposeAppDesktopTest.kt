package org.example.project

import LocationUpdate
import NoteAddedUpdate
import ShippedUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.example.project.*
import java.io.File
import kotlin.test.*
import CanceledUpdate
import CreatedUpdate
import DeliveredUpdate
import LostUpdate
import org.example.project.Shipment

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingSimulatorTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun testProcessUpdateCreated() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        val line = "created,s1,1652712855468"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("created", shipment.status)
    }

    @Test
    fun testProcessUpdateShipped() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "shipped,s1,1652712855468,1652713940874"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("shipped", shipment.status)
        assertEquals(1652713940874, shipment.expectedDelivery)
    }

    @Test
    fun testProcessUpdateLocation() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "location,s1,1652712855468,Los Angeles CA"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("Los Angeles CA", shipment.location)
    }

    @Test
    fun testProcessUpdateDelivered() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "delivered,s1,1652712855468"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("delivered", shipment.status)
    }

    @Test
    fun testProcessUpdateDelayed() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "delayed,s1,1652712855468,1652718051403"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("delayed", shipment.status)
        assertEquals(1652718051403, shipment.expectedDelivery)
    }

    @Test
    fun testProcessUpdateLost() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "lost,s1,1652712855468"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("lost", shipment.status)
    }

    @Test
    fun testProcessUpdateCanceled() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "canceled,s1,1652712855468"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals("canceled", shipment.status)
    }

    @Test
    fun testProcessUpdateNoteAdded() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val line = "noteadded,s1,1652712855468,Test note"
        simulator.sendUpdate(line)
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertTrue(shipment.updates.contains("Note added: Test note"))
    }

    @Test
    fun testProcessUpdateShippedInfoNull() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialShipment = simulator.shipments["s1"]?.copy()
        val timestamp = 1652712855468L
        simulator.sendUpdate("shipped,s1,$timestamp")
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals(initialShipment?.expectedDelivery, shipment.expectedDelivery)
    }

    @Test
    fun testProcessUpdateShippedInfoInvalid() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialShipment = simulator.shipments["s1"]?.copy()
        val timestamp = 1652712855468L
        simulator.sendUpdate("shipped,s1,$timestamp,not_a_long")
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals(initialShipment?.expectedDelivery, shipment.expectedDelivery)
    }

    @Test
    fun testProcessUpdateLocationInfoNull() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialLocation = simulator.shipments["s1"]?.location
        simulator.sendUpdate("location,s1,1652712855468")
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals(initialLocation, shipment.location)
    }

    @Test
    fun testProcessUpdateDelayedInfoNull() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialShipment = simulator.shipments["s1"]?.copy()
        val timestamp = 1652712855468L
        simulator.sendUpdate("delayed,s1,$timestamp")
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals(initialShipment?.expectedDelivery, shipment.expectedDelivery)
    }

    @Test
    fun testProcessUpdateDelayedInfoInvalid() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialShipment = simulator.shipments["s1"]?.copy()
        val timestamp = 1652712855468L
        simulator.sendUpdate("delayed,s1,$timestamp,not_a_long")
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals(initialShipment?.expectedDelivery, shipment.expectedDelivery)
    }

    @Test
    fun testProcessUpdateNoteAddedInfoNull() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialUpdates = simulator.shipments["s1"]?.updates?.size ?: 0
        simulator.sendUpdate("noteadded,s1,1652712855468")
        val shipment = simulator.shipments["s1"]
        assertNotNull(shipment)
        assertEquals(initialUpdates, shipment.updates.size)
    }

    @Test
    fun testProcessUpdateUnknownType() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialShipment = simulator.shipments["s1"]?.copy()
        simulator.sendUpdate("unknown,s1,1652712855468")
        val shipment = simulator.shipments["s1"]
        assertEquals(initialShipment?.status, shipment?.status)
        assertEquals(initialShipment?.location, shipment?.location)
        assertEquals(initialShipment?.expectedDelivery, shipment?.expectedDelivery)
    }

    @Test
    fun testProcessUpdateExistingShipment() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        assertEquals(1, simulator.shipments.size)
        simulator.sendUpdate("location,s1,1652712855468,new_location")
        assertEquals(1, simulator.shipments.size)
        assertEquals("new_location", simulator.shipments["s1"]?.location)
    }

    @Test
    fun testDelayedUpdateSetsNewDeliveryDate() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val newDeliveryDate = 1652718051403L
        simulator.sendUpdate("delayed,s1,1652712855468,$newDeliveryDate")
        val shipment = simulator.shipments["s1"]
        assertEquals(newDeliveryDate, shipment?.expectedDelivery, "The expected delivery date should be updated.")
    }

    @Test
    fun testDelayedUpdateWithInvalidDate() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val initialShipment = simulator.shipments["s1"]?.copy()
        simulator.sendUpdate("delayed,s1,1652712855468,invalid_date")
        val shipment = simulator.shipments["s1"]
        assertEquals(initialShipment?.expectedDelivery, shipment?.expectedDelivery, "The expected delivery date should not change with invalid data.")
    }

    @Test
    fun testStart() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.start()
        advanceUntilIdle() // Ensure all coroutines have completed
        assertFalse(simulator.shipments.isEmpty(), "Shipments should be loaded after start.")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewHelperTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun testTrackShipmentAlreadyTracked() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val helper = TrackerViewHelper(simulator)
        helper.trackShipment("s1")
        val initialTrackedShipment = helper.trackedShipments["s1"]

        helper.trackShipment("s1")
        val secondTrackedShipment = helper.trackedShipments["s1"]

        assertEquals(initialTrackedShipment, secondTrackedShipment, "Shipment should not change if tracked again.")
    }

    @Test
    fun testTrackShipmentNotFound() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        val helper = TrackerViewHelper(simulator)
        helper.trackShipment("s1")
        assertTrue(helper.trackedShipments.isEmpty(), "Should not track a shipment that doesn't exist.")
    }

    @Test
    fun testStopTrackingShipmentNotFound() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        val helper = TrackerViewHelper(simulator)
        helper.stopTrackingShipment("s1") // Should not throw an exception
        assertTrue(helper.trackedShipments.isEmpty())
    }

    @Test
    fun testUpdateUntrackedShipment() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        val helper = TrackerViewHelper(simulator)
        val untrackedShipment = Shipment("s2")
        helper.update(untrackedShipment)
        assertTrue(helper.trackedShipments.isEmpty(), "Should not add untracked shipment on update.")
    }

    @Test
    fun testTrackShipment() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val helper = TrackerViewHelper(simulator)
        helper.trackShipment("s1")

        val trackedShipment = helper.trackedShipments["s1"]
        assertNotNull(trackedShipment)
        assertEquals("s1", trackedShipment.id)

        // Check if observer is registered
        val realShipment = simulator.shipments["s1"]
        assertNotNull(realShipment)
        assertTrue(realShipment.hasObserver(helper))
    }

    @Test
    fun testStopTrackingShipment() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val helper = TrackerViewHelper(simulator)
        helper.trackShipment("s1")
        helper.stopTrackingShipment("s1")

        assertNull(helper.trackedShipments["s1"])

        val realShipment = simulator.shipments["s1"]
        assertNotNull(realShipment)
        assertFalse(realShipment.hasObserver(helper))
    }

    @Test
    fun testUpdate() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val helper = TrackerViewHelper(simulator)
        helper.trackShipment("s1")

        simulator.sendUpdate("shipped,s1,1652712855468,1652713940874")

        val trackedShipment = helper.trackedShipments["s1"]
        assertNotNull(trackedShipment)
        assertEquals("shipped", trackedShipment.status)
    }

    @Test
    fun testGetShipment() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        simulator.sendUpdate("created,s1,1652712855468")
        val helper = TrackerViewHelper(simulator)
        val shipment = helper.getShipment("s1")
        assertNotNull(shipment)
        assertEquals("s1", shipment.id)
    }
}

class ShipmentTest {
    private class TestObserver : Observer {
        var updateCalled = false
        override fun update(shipment: Shipment) {
            updateCalled = true
        }
    }

    @Test
    fun testRegisterObserver() {
        val shipment = Shipment("s1")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        assertTrue(shipment.hasObserver(observer))
    }

    @Test
    fun testRemoveObserver() {
        val shipment = Shipment("s1")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.removeObserver(observer)
        assertFalse(shipment.hasObserver(observer))
    }

    @Test
    fun testApplyUpdateNotifiesObservers() {
        val shipment = Shipment("s1")
        val observer = TestObserver()
        shipment.registerObserver(observer)
        shipment.applyUpdate({ s, _, _ -> s.setStatus("updated") }, 1L, null)
        assertTrue(observer.updateCalled)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingSimulatorFileNotFoundTest {

    private val dispatcher = StandardTestDispatcher()
    private val resourceDir = "src/desktopMain/resources"
    private val originalFile = File(resourceDir, "test.txt")
    private val renamedFile = File(resourceDir, "test.txt.bak")

    @BeforeTest
    fun setup() {
        if (originalFile.exists()) {
            originalFile.renameTo(renamedFile)
        }
    }

    @AfterTest
    fun teardown() {
        if (renamedFile.exists()) {
            renamedFile.renameTo(originalFile)
        }
    }

    @Test
    fun testStartFileNotFound() = runTest(dispatcher) {
        val simulator = TrackingSimulator(dispatcher)
        try {
            simulator.start()
        } catch (e: Exception) {
            fail("start() should not throw an exception when the file is not found.")
        }
        assertTrue(simulator.shipments.isEmpty(), "Shipments should be empty when file is not found.")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateStrategyTest {

    @Test
    fun testLocationUpdate() {
        val shipment = Shipment("s1")
        val locationUpdate = LocationUpdate()
        val timestamp = System.currentTimeMillis()
        val location = "Los Angeles, CA"
        locationUpdate.applyUpdate(shipment, timestamp, location)
        assertEquals(location, shipment.location)
        val expectedMessage = "Shipment arrived at location $location on ${locationUpdate.formatDate(timestamp)}"
        assertTrue(shipment.updates.contains(expectedMessage))
    }

    @Test
    fun testLocationUpdateNullInfo() {
        val shipment = Shipment("s1")
        val initialLocation = shipment.location
        val locationUpdate = LocationUpdate()
        locationUpdate.applyUpdate(shipment, System.currentTimeMillis(), null)
        assertEquals(initialLocation, shipment.location)
    }

    @Test
    fun testNoteAddedUpdate() {
        val shipment = Shipment("s1")
        val noteAddedUpdate = NoteAddedUpdate()
        val timestamp = System.currentTimeMillis()
        val note = "This is a test note."
        noteAddedUpdate.applyUpdate(shipment, timestamp, note)
        val expectedMessageInUpdates = "Note added on ${noteAddedUpdate.formatDate(timestamp)}: '$note'"
        val expectedNoteInShipment = "Note added: $note"
        assertTrue(shipment.updates.contains(expectedMessageInUpdates))
        assertTrue(shipment.updates.contains(expectedNoteInShipment))
    }

    @Test
    fun testNoteAddedUpdateNullInfo() {
        val shipment = Shipment("s1")
        val initialUpdates = shipment.updates.toList()
        val noteAddedUpdate = NoteAddedUpdate()
        noteAddedUpdate.applyUpdate(shipment, System.currentTimeMillis(), null)
        assertEquals(initialUpdates, shipment.updates)
    }

    @Test
    fun testShippedUpdate() {
        val shipment = Shipment("s1")
        val shippedUpdate = ShippedUpdate()
        val timestamp = System.currentTimeMillis()
        val expectedDelivery = timestamp + 86400000 // 1 day later
        val previousStatus = shipment.status
        shippedUpdate.applyUpdate(shipment, timestamp, expectedDelivery.toString())
        assertEquals("shipped", shipment.status)
        assertEquals(expectedDelivery, shipment.expectedDelivery)
        val expectedMessage = "Shipment went from $previousStatus to shipped on ${shippedUpdate.formatDate(timestamp)}"
        assertTrue(shipment.updates.contains(expectedMessage))
    }

    @Test
    fun testShippedUpdateNullInfo() {
        val shipment = Shipment("s1")
        val initialExpectedDelivery = shipment.expectedDelivery
        val shippedUpdate = ShippedUpdate()
        shippedUpdate.applyUpdate(shipment, System.currentTimeMillis(), null)
        assertEquals(initialExpectedDelivery, shipment.expectedDelivery)
    }

    @Test
    fun testShippedUpdateInvalidInfo() {
        val shipment = Shipment("s1")
        val initialExpectedDelivery = shipment.expectedDelivery
        val shippedUpdate = ShippedUpdate()
        shippedUpdate.applyUpdate(shipment, System.currentTimeMillis(), "not a long")
        assertEquals(initialExpectedDelivery, shipment.expectedDelivery)
    }

    @Test
    fun testCanceledUpdate() {
        val shipment = Shipment("s1")
        val canceledUpdate = CanceledUpdate()
        val timestamp = System.currentTimeMillis()
        val previousStatus = shipment.status
        canceledUpdate.applyUpdate(shipment, timestamp, null)
        assertEquals("canceled", shipment.status)
        val expectedMessage = "Shipment went from $previousStatus to canceled on ${canceledUpdate.formatDate(timestamp)}"
        assertTrue(shipment.updates.contains(expectedMessage))
    }

    @Test
    fun testCreatedUpdate() {
        val shipment = Shipment("s1")
        shipment.setStatus("unknown") // Set to something else to see the change
        val createdUpdate = CreatedUpdate()
        val timestamp = System.currentTimeMillis()
        createdUpdate.applyUpdate(shipment, timestamp, null)
        assertEquals("created", shipment.status)
        val expectedMessage = "Shipment created on ${createdUpdate.formatDate(timestamp)}"
        assertTrue(shipment.updates.contains(expectedMessage))
    }

    @Test
    fun testDeliveredUpdate() {
        val shipment = Shipment("s1")
        val deliveredUpdate = DeliveredUpdate()
        val timestamp = System.currentTimeMillis()
        val previousStatus = shipment.status
        deliveredUpdate.applyUpdate(shipment, timestamp, null)
        assertEquals("delivered", shipment.status)
        val expectedMessage = "Shipment went from $previousStatus to delivered on ${deliveredUpdate.formatDate(timestamp)}"
        assertTrue(shipment.updates.contains(expectedMessage))
    }

    @Test
    fun testLostUpdate() {
        val shipment = Shipment("s1")
        val lostUpdate = LostUpdate()
        val timestamp = System.currentTimeMillis()
        val previousStatus = shipment.status
        lostUpdate.applyUpdate(shipment, timestamp, null)
        assertEquals("lost", shipment.status)
        val expectedMessage = "Shipment went from $previousStatus to lost on ${lostUpdate.formatDate(timestamp)}"
        assertTrue(shipment.updates.contains(expectedMessage))
    }
}