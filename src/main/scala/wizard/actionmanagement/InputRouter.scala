package wizard.actionmanagement

import java.util.concurrent.LinkedBlockingQueue
import scala.util.Try

/**
  * InputRouter provides a tiny bridge so non-UI model code (e.g., Human.bid/playCard)
  * can obtain user inputs without depending on any particular UI. By default
  * it falls back to StdIn, so the TUI continues to work unchanged. A GUI can
  * enqueue responses so the model can read them without blocking the JavaFX thread.
  */
object InputRouter {
  private val queue = new LinkedBlockingQueue[String]()
  @volatile private var feederStarted = false

  /** Start a single background feeder that forwards StdIn lines into the queue. */
  private def ensureFeeder(): Unit = this.synchronized {
    if (!feederStarted) {
      feederStarted = true
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          try {
            while (true) {
              val line = scala.io.StdIn.readLine()
              // Forward nulls cautiously; some environments may return null on EOF
              if (line != null) queue.offer(line)
              else Thread.sleep(10)
            }
          } catch {
            case _: Throwable => () // do not crash application if StdIn is unavailable
          }
        }
      })
      t.setDaemon(true)
      t.setName("InputRouter-StdIn-Feeder")
      t.start()
    }
  }

  /**
    * Offer an input line to be consumed by Human players. This is typically called from the GUI
    * when the user submits a bid or clicks a card.
    */
  def offer(line: String): Unit = {
    if (line != null) queue.offer(line)
  }

  /**
    * Read a line for the model. Blocks on the internal queue, which is fed either by the GUI
    * via offer() or by a background feeder that mirrors StdIn. This design allows cooperative
    * cancellation by injecting sentinel tokens (e.g., "__BACK_TO_COUNT__").
    */
  def readLine(): String = {
    ensureFeeder()
    queue.take()
  }

  /**
    * Convenience helper to read an Int, ignoring non-integer inputs until a valid one arrives.
    */
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

  /**
    * For tests: clear pending inputs.
    */
  def clear(): Unit = queue.clear()
}