import org.example.project.Shipment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface UpdateStrategy{
    fun applyUpdate(shipment: Shipment, timestamp: Long, otherInfo: String?)

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}