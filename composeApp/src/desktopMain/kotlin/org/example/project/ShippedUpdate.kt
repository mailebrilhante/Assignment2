import org.example.project.Shipment

class ShippedUpdate : UpdateStrategy {
    override fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?) {
        val previousStatus = shipment.status
        shipment.status = "shipped"
        otherInfo?.toLongOrNull()?.let {
            shipment.expectedDelivery = it
        }
        val updateMessage = "Shipment went from $previousStatus to ${shipment.status} on ${formatDate(timestamp)}"
        shipment.addUpdate(updateMessage)
    }
}