package wizard.actionmanagement

trait Observer {
    def update(updateMSG: String, obj: Any*): Any
}

class Observable {
    var subscribers: Vector[Observer] = Vector()
    def add(s: Observer): Unit = {
        if (!subscribers.contains(s)) subscribers = subscribers :+ s
    }
    def remove(s: Observer): Unit = subscribers = subscribers.filterNot(o => o == s) // wofür bräuchte man das
    def notifyObservers(updateMSG: String, obj: Any*): Unit = {
        // Synchronous notification to satisfy test expectations; no debug logging
        subscribers.foreach { o =>
            try {
                o.update(updateMSG, obj: _*)
            } catch {
                case _: Throwable => ()
            }
        }
    }
}
