package wizard.actionmanagement

trait Observer {
    def update(updateMSG: String, obj: Any*): Unit
}

class Observable {
    var subscribers:Vector[Observer] = Vector()
    def add(s:Observer): Unit = subscribers=subscribers:+s
    //def remove(s:Observer): Unit = subscribers=subscribers.filterNot(o=>o==s) // wofür bräuchte man das
    def notifyObservers(updateMSG: String, obj: Any*): Unit = subscribers.foreach(o=>o.update(updateMSG, obj*))
}
