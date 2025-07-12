import org.example.project.Shipment

class CanceledUpdate : UpdateStrategy {
    override fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?) {
        val previousStatus = shipment.status
        shipment.setStatus("canceled")
        val updateMessage = "Shipment went from $previousStatus to ${shipment.status} on ${formatDate(timestamp)}"
        shipment.addUpdate(updateMessage)
    }
}