package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.{Observer, AskForPlayerNames}

class GameLogicAskForPlayerNamesTests extends AnyWordSpec with Matchers {
  "GameLogic.playerCountSelected" should {
    "also broadcast AskForPlayerNames for legacy listeners" in {
      val controller = new GameLogic
      @volatile var askNotified = 0
      val testObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "AskForPlayerNames") askNotified += 1
        }
      }
      controller.add(testObserver)
      controller.playerCountSelected(3)
      askNotified shouldBe 1
    }
  }
}
