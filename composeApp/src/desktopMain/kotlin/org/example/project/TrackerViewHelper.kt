package org.example.project

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateMap

class TrackerViewHelper(private val simulator: TrackingSimulator) : Observer {
    private val _trackedShipments = mutableStateMapOf<String, Shipment>()
    val trackedShipments: SnapshotStateMap<String, Shipment> = _trackedShipments

    fun trackShipment(id: String) {
        if (_trackedShipments.containsKey(id)) return

        val shipment = simulator.shipments[id]
        shipment?.let {
            it.registerObserver(this)
            _trackedShipments[id] = it.copy()
        }
    }

    fun stopTrackingShipment(id: String) {
        simulator.shipments[id]?.removeObserver(this)
        _trackedShipments.remove(id)
    }

    fun getShipment(id: String): Shipment? {
        return simulator.shipments[id]
    }

    override fun update(shipment: Shipment) {
        if (_trackedShipments.containsKey(shipment.id)) {
            _trackedShipments[shipment.id] = shipment.copy()
            println("UI_HELPER: Received update for ${shipment.id}, UI state changed.")
        }
    }
} 