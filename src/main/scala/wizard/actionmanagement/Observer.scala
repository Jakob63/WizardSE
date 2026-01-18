package wizard.actionmanagement

trait Observer {
    def update(updateMSG: String, obj: Any*): Any
}

class Observable {
    var subscribers: Vector[Observer] = Vector()
    def add(s: Observer): Unit = {
        if (!subscribers.contains(s)) {
            subscribers = subscribers :+ s
        }
    }
    def remove(s: Observer): Unit = {
        subscribers = subscribers.filterNot(o => o == s)
    }
    def notifyObservers(updateMSG: String, obj: Any*): Unit = {
        subscribers.foreach { o =>
            try {
                o.update(updateMSG, obj*)
            } catch {
                case _: Throwable => ()
            }
        }
    }
}
