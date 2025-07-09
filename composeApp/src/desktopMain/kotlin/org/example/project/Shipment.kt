package org.example.project

class Shipment (private var _id: String): Subject {

    val id: String
    get() = _id

    var status: String = "created"
    var location: String = "unknown"
    var expectedDelivery: Long? = null
    var notes = mutableListOf<String>()
    var updates = mutableListOf<String>()
    private val observers = mutableListOf<Observer>()

    override fun registerObserver(observer: Observer) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.update(this) }
    }

    fun addNote(note: String) {
        notes.add(note)
        notifyObservers()
    }

    fun addUpdate(update: String){
        updates.add(update)
        notifyObservers()
    }

    fun applyUpdate(updateAction: (Shipment, Long, String?) -> Unit, timestamp: Long, otherInfo: String?) {
        updateAction(this, timestamp, otherInfo)
        notifyObservers()
    }

    
}