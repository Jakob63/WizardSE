package wizard.actionmanagement

trait Observer {
    def update(updateMSG: String, obj: Any*): Any
}

object Debug {
    val enabled: Boolean = sys.props.get("WIZARD_DEBUG").forall(v => v != "0" && v.toLowerCase != "false")
    @inline def log(msg: => String): Unit = if (enabled) Console.out.println(s"${Console.BLUE}[DEBUG_LOG] $msg${Console.RESET}")
}

class Observable {
    var subscribers: Vector[Observer] = Vector()
    def add(s: Observer): Unit = {
        if (!subscribers.contains(s)) {
            subscribers = subscribers :+ s
            Debug.log(s"Observable.add -> now ${subscribers.size} subscribers; added ${s.getClass.getName}")
        }
    }
    def remove(s: Observer): Unit = {
        subscribers = subscribers.filterNot(o => o == s)
        Debug.log(s"Observable.remove -> now ${subscribers.size} subscribers; removed ${s.getClass.getName}")
    } 
    def notifyObservers(updateMSG: String, obj: Any*): Unit = {
        Debug.log(s"Observable.notifyObservers('$updateMSG') -> notifying ${subscribers.size} observers")
        subscribers.foreach { o =>
            try {
                o.update(updateMSG, obj*)
            } catch {
                case e: Throwable => Debug.log(s"Observer ${o.getClass.getName} threw: ${e.getMessage}")
            }
        }
    }
}
