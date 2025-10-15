package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import wizard.actionmanagement.{Observer, Debug}

class GameLogicTests extends AnyWordSpec with Matchers {

  "GameLogic" should {
    "emit PlayerCountSelected again after reset" in {
      val gl = new GameLogic
      // Test observer to count notifications
      @volatile var playerCountSelectedEmits = 0
      @volatile var askForPlayerNamesEmits = 0
      @volatile var askForPlayerCountEmits = 0
      val obs = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          updateMSG match {
            case "PlayerCountSelected" => playerCountSelectedEmits += 1
            case "AskForPlayerNames" => askForPlayerNamesEmits += 1
            case "AskForPlayerCount" => askForPlayerCountEmits += 1
            case _ => ()
          }
        }
      }
      gl.add(obs)

      // First selection
      gl.playerCountSelected(3)
      playerCountSelectedEmits mustBe 1
      askForPlayerNamesEmits mustBe 1

      // Reset to allow re-selection
      gl.resetPlayerCountSelection()
      askForPlayerCountEmits mustBe 1

      // Select again (same value) should now be accepted
      gl.playerCountSelected(3)
      playerCountSelectedEmits mustBe 2
      askForPlayerNamesEmits mustBe 2
    }
  }
}
