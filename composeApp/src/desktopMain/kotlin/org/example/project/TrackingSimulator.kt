package org.example.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingSimulator(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) {
    private val _shipments = mutableMapOf<String, Shipment>()
    val shipments: Map<String, Shipment>
        get() = _shipments.toMap()

    private val scope = CoroutineScope(dispatcher)

    fun start() {
        scope.launch {
            val inputStream = javaClass.classLoader.getResourceAsStream("test.txt")
            if (inputStream == null) {
                println("Error: test.txt not found in resources.")
                return@launch
            }
            inputStream.bufferedReader().readLines().forEach { line ->
                sendUpdate(line)
                delay(1000)
            }
        }
    }

    fun sendUpdate(line: String) {
        val parts = line.split(",", limit = 4)
        val type = parts[0]
        val id = parts[1]
        val timestamp = parts[2].toLong()
        val otherInfo = parts.getOrNull(3)

        val shipment = _shipments.getOrPut(id) { Shipment(id) }

        val updateAction: (Shipment, Long, String?) -> Unit = when (type) {
            "created" -> { s, _, _ -> s.setStatus("created") }
            "shipped" -> { s, _, info ->
                s.setStatus("shipped")
                info?.toLongOrNull()?.let { s.setExpectedDelivery(it) }
            }
            "location" -> { s, _, info -> info?.let { s.setLocation(it) } }
            "delivered" -> { s, _, _ -> s.setStatus("delivered") }
            "delayed" -> { s, _, info ->
                s.setStatus("delayed")
                info?.toLongOrNull()?.let { s.setExpectedDelivery(it) }
            }
            "lost" -> { s, _, _ -> s.setStatus("lost") }
            "canceled" -> { s, _, _ -> s.setStatus("canceled") }
            "noteadded" -> { s, _, info -> info?.let { s.addNote(it) } }
            else -> { _, _, _ -> /* Do nothing for unknown types */ }
        }

        println("SIMULATOR: Applying '$type' to shipment '$id'")
        shipment.applyUpdate(updateAction, timestamp, otherInfo)
    }
}
