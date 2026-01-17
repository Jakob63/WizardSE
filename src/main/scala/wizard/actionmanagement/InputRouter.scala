package wizard.actionmanagement

import java.util.concurrent.LinkedBlockingQueue
import scala.util.Try

object InputRouter {
  private val queue = new LinkedBlockingQueue[String]()
  @volatile private var feederStarted = false

  private def ensureFeeder(): Unit = this.synchronized {
    if (!feederStarted) {
      feederStarted = true
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          try {
            while (true) {
              val line = scala.io.StdIn.readLine()
              if (line != null) queue.offer(line)
              else Thread.sleep(10)
            }
          } catch {
            case _: Throwable => () 
          }
        }
      })
      t.setDaemon(true)
      t.setName("InputRouter-StdIn-Feeder")
      t.start()
    }
  }

  def offer(line: String): Unit = {
    if (line != null) queue.offer(line)
  }

  def readLine(): String = {
    ensureFeeder()
    val res = queue.take()
    if (res == "__UNDO__") throw new UndoException("undo")
    if (res == "__REDO__") throw new RedoException("redo")
    res
  }

  class UndoException(msg: String) extends RuntimeException(msg)
  class RedoException(msg: String) extends RuntimeException(msg)

  def readInt(): Int = {
    var done = false
    var value = 0
    while (!done) {
      val line = readLine()
      Try(Option(line).getOrElse("").trim.toInt).toOption match {
        case Some(n) => value = n; done = true
        case None => ()
      }
    }
    value
  }

  def clear(): Unit = queue.clear()
}