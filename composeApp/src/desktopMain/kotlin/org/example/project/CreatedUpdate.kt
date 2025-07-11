import org.example.project.Shipment

class CreatedUpdate : UpdateStrategy {
    override fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?) {
        shipment.setStatus("created")
        val updateMessage = "Shipment created on ${formatDate(timestamp)}"
        shipment.addUpdate(updateMessage)
    }
}