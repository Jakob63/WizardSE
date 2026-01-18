package wizard.actionmanagement

trait Observer {
    def update(updateMSG: String, obj: Any*): Any
}

object Debug {
    val enabled: Boolean = sys.props.get("WIZARD_DEBUG").forall(v => v != "0" && v.toLowerCase != "false")
    @inline def log(msg: => String): Unit = if (enabled) Console.err.println(s"${Console.BLUE}[DEBUG_LOG] $msg${Console.RESET}")

    def initEnvironment(): Unit = {
        try {
            System.setProperty("javafx.logging.level", "OFF")
            System.setProperty("glass.accessible.force", "false")
            System.setProperty("jdk.module.illegalAccess", "deny")

            val originalErr = System.err
            val filteringErr = new java.io.PrintStream(new java.io.OutputStream {
                private val buffer = new StringBuilder()
                override def write(b: Int): Unit = {
                    val c = b.toChar
                    buffer.append(c)
                    if (c == '\n') {
                        flushBuffer()
                    }
                }
                private def flushBuffer(): Unit = {
                    val line = buffer.toString()
                    if (!line.contains("Unsupported JavaFX configuration: classes were loaded from 'unnamed module")) {
                        originalErr.print(line)
                    }
                    buffer.setLength(0)
                }
            })
            System.setErr(filteringErr)

            val loggers = List(
                "javafx",
                "com.sun.javafx",
                "com.sun.javafx.application",
                "com.sun.glass.ui"
            )
            loggers.foreach { name =>
                val l = java.util.logging.Logger.getLogger(name)
                l.setUseParentHandlers(false)
                l.setLevel(java.util.logging.Level.OFF)
            }
        } catch {
            case _: Throwable => ()
        }
    }
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
