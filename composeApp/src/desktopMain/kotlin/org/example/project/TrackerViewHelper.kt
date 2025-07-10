package org.example.project

class TrackerViewHelper(private val shipment: Shipment) : Observer {

    val shipmentId: String
        get() = shipment.id

    var latestShipment: Shipment? = null
        private set

    fun startTracking() {
        shipment.registerObserver(this)
        latestShipment = shipment.copy()
    }

    fun stopTracking() {
        shipment.removeObserver(this)
    }

    override fun update(updatedShipment: Shipment) {
        latestShipment = updatedShipment.copy()
        println("Tracker for ${shipment.id} received an update.")
    }
} 