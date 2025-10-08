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
        val argTypes = try obj.map(_.getClass.getSimpleName).mkString("[", ", ", "]") catch { case _: Throwable => "[]" }
        val subsList = try subscribers.map(_.getClass.getName).mkString("[", ", ", "]") catch { case _: Throwable => "[]" }
        println(s"[DEBUG_LOG] notifyObservers msg='$updateMSG', subscribers=${subscribers.size}, list=$subsList, args=$argTypes")
        subscribers.foreach { o =>
            val on = o.getClass.getName
            println(s"[DEBUG_LOG] notifying -> $on with '$updateMSG' (async)")
            val t = new Thread(new Runnable {
                override def run(): Unit = {
                    try {
                        o.update(updateMSG, obj: _*)
                    } catch {
                        case ex: Throwable => println(s"[DEBUG_LOG] observer threw on '$updateMSG' in $on: ${ex.getClass.getSimpleName}: ${ex.getMessage}")
                    }
                }
            })
            t.setDaemon(true)
            t.start()
        }
    }
}
