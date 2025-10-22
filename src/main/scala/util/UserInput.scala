package util

import java.util.concurrent.LinkedBlockingQueue
import javax.inject.*

trait UserInput {
  def readLine(prompt: Option[String] = None): String
  def offer(value: String): Boolean
  def clear(): Unit
}

@Singleton
class QueueInput @Inject()() extends UserInput {
  private val queue = new LinkedBlockingQueue[String]()

  override def readLine(prompt: Option[String]): String = queue.take()
  override def offer(value: String): Boolean = queue.offer(value)
  override def clear(): Unit = queue.clear()
}