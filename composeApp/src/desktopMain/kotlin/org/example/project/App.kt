package org.example.project

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
@Preview
fun App() {
    val simulator = remember { TrackingSimulator().apply { start() } }
    val trackerViewHelper = remember { TrackerViewHelper(simulator) }
    var shipmentId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val trackedShipments = trackerViewHelper.trackedShipments

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = shipmentId,
                    onValueChange = {
                        shipmentId = it
                        errorMessage = null
                    },
                    label = { Text("Enter Shipment ID (e.g., s10000)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (trackerViewHelper.getShipment(shipmentId) != null) {
                        trackerViewHelper.trackShipment(shipmentId)
                    } else {
                        errorMessage = "Shipment with ID '$shipmentId' not found."
                    }
                }) {
                    Text("Track")
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trackedShipments.keys.toList()) { id ->
                    trackedShipments[id]?.let { shipment ->
                        ShipmentCard(shipment) {
                            trackerViewHelper.stopTrackingShipment(id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShipmentCard(shipment: Shipment, onStopTracking: () -> Unit) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Shipment ID: ${shipment.id}", style = MaterialTheme.typography.h6)
                Button(onClick = onStopTracking) {
                    Text("Stop Tracking")
                }
            }
            Text("Status: ${shipment.status}")
            Text("Location: ${shipment.location}")
            Text("Expected Delivery: ${shipment.expectedDelivery?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: "N/A"}")

            Spacer(modifier = Modifier.height(8.dp))
            Text("Update History:", style = MaterialTheme.typography.subtitle1)
            shipment.updates.forEach { update ->
                Text("- $update")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Notes:", style = MaterialTheme.typography.subtitle1)
            shipment.notes.forEach { note ->
                Text("- $note")
            }
        }
    }
}