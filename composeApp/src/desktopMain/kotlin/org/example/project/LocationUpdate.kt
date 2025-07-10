import org.example.project.Shipment

class LocationUpdate : UpdateStrategy {
    override fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?) {
        otherInfo?.let {
            shipment.location = it
            val updateMessage = "Shipment arrived at location $it on ${formatDate(timestamp)}"
            shipment.addUpdate(updateMessage)
        }
    }
}