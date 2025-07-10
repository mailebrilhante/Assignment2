package org.example.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingSimulator(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) {
    val shipments = mutableMapOf<String, Shipment>()
    private val scope = CoroutineScope(dispatcher)

    fun start() {
        scope.launch {
            val inputStream = javaClass.classLoader.getResourceAsStream("test.txt")
            if (inputStream == null) {
                println("Error: test.txt not found in resources.")
                return@launch
            }
            inputStream.bufferedReader().readLines().forEach { line ->
                processUpdate(line)
                delay(1000)
            }
        }
    }

    private fun processUpdate(line: String) {
        val parts = line.split(",", limit = 4)
        val type = parts[0]
        val id = parts[1]
        val timestamp = parts[2].toLong()
        val otherInfo = parts.getOrNull(3)

        val shipment = shipments.getOrPut(id) { Shipment(id) }

        val updateAction: (Shipment, Long, String?) -> Unit = when (type) {
            "created" -> { s, _, _ -> s.status = "created" }
            "shipped" -> { s, ts, info ->
                s.status = "shipped"
                info?.let { s.location = it }
                s.expectedDelivery = ts
            }
            "location" -> { s, _, info -> info?.let { s.location = it } }
            "delivered" -> { s, _, _ -> s.status = "delivered" }
            "delayed" -> { s, _, _ -> s.status = "delayed" }
            "lost" -> { s, _, _ -> s.status = "lost" }
            "canceled" -> { s, _, _ -> s.status = "canceled" }
            "noteadded" -> { s, _, info -> info?.let { s.addNote(it) } }
            else -> { _, _, _ -> /* Do nothing for unknown types */ }
        }

        println("SIMULATOR: Applying '$type' to shipment '$id'")
        shipment.applyUpdate(updateAction, timestamp, otherInfo)
    }
}
