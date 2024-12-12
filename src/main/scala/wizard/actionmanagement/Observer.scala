package wizard.actionmanagement

trait Observer {
    def update(updateMSG: String, obj: Any*): Any
}

class Observable {
    var subscribers:Vector[Observer] = Vector()
    def add(s:Observer): Unit = subscribers=subscribers:+s
    def remove(s:Observer): Unit = subscribers=subscribers.filterNot(o=>o==s) // wofür bräuchte man das
    def notifyObservers(updateMSG: String, obj: Any*): Any = {
        subscribers.map(o=> {
            val n = o.update(updateMSG, obj*)
            if (n.isInstanceOf[Unit]) true
            else n
        }).head // head gibt erste element zurück
        
    }
}
