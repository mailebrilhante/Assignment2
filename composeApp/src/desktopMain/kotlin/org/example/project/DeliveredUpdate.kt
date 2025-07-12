import org.example.project.Shipment

class DeliveredUpdate : UpdateStrategy {
    override fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?) {
        val previousStatus = shipment.status
        shipment.setStatus("delivered")
        val updateMessage = "Shipment went from $previousStatus to ${shipment.status} on ${formatDate(timestamp)}"
        shipment.addUpdate(updateMessage)
    }
}