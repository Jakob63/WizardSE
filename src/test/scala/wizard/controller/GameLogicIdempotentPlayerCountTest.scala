package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import wizard.actionmanagement.{Observer, Debug}

class GameLogicIdempotentPlayerCountTest extends AnyWordSpec with Matchers {

  class CaptureObserver extends Observer {
    @volatile var events: Vector[String] = Vector.empty
    override def update(updateMSG: String, obj: Any*): Any = {
      events = events :+ updateMSG
    }
  }

  "GameLogic.playerCountSelected" should {
    "rebroadcast PlayerCountSelected and AskForPlayerNames when the same count is selected again after an undo-local-back race" in {
      val gl = new GameLogic
      val cap = new CaptureObserver
      gl.add(cap)

      // First selection
      gl.playerCountSelected(3)
      // We expect two events to have been emitted among others
      assert(cap.events.contains("PlayerCountSelected"))
      assert(cap.events.contains("AskForPlayerNames"))

      // Simulate that a view locally navigated back (GUI) but before reset completes, user clicks continue again.
      // No reset yet; calling playerCountSelected(3) again must still rebroadcast to keep UIs in sync.
      val before = cap.events.size
      gl.playerCountSelected(3)
      val after = cap.events.size
      assert(after >= before + 2) // at least two more notifications

      // Now ensure that selecting a different count does NOT override once selected
      val beforeDiff = cap.events.size
      gl.playerCountSelected(4)
      assert(cap.events.size == beforeDiff) // no new events for different count once set

      // And reset should clear and notify AskForPlayerCount
      gl.resetPlayerCountSelection()
      assert(cap.events.contains("AskForPlayerCount"))

      // After reset, selecting the same count again must work
      val before2 = cap.events.size
      gl.playerCountSelected(3)
      assert(cap.events.size >= before2 + 2)
    }
  }
}
