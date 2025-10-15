package wizard.controller

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import wizard.actionmanagement.Observer

class GameLogicObserverSyncTests extends AnyWordSpec with Matchers {
  "GameLogic" should {
    "notify observers when player count is selected" in {
      val controller = new GameLogic
      @volatile var events: List[String] = Nil
      val obs = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          events = events :+ updateMSG
        }
      }
      controller.add(obs)
      controller.playerCountSelected(4)
      events should contain ("PlayerCountSelected")
      events should contain ("AskForPlayerNames")
    }
  }
}
