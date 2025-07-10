import org.example.project.Shipment

class NoteAddedUpdate : UpdateStrategy {
    override fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?) {
        otherInfo?.let {
            shipment.addNote(it)
            val updateMessage = "Note added on ${formatDate(timestamp)}: '$it'"
            shipment.addUpdate(updateMessage)
        }
    }
}