package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.Observer

class GameLogicResetPlayerCountTest extends AnyWordSpec with Matchers {
  class CountingObserver extends Observer {
    var askForCountNotifies = 0
    override def update(updateMSG: String, obj: Any*): Any = {
      if (updateMSG == "AskForPlayerCount") askForCountNotifies += 1
      ()
    }
  }

  "GameLogic.resetPlayerCountSelection" should {
    "notify AskForPlayerCount only once when there was a prior selection" in {
      val gl = new GameLogic
      val obs = new CountingObserver
      gl.add(obs)
      // Start will trigger an initial AskForPlayerCount that we ignore in this count
      // We assert only the notifications caused by reset logic after a selection
      gl.playerCountSelected(3)
      obs.askForCountNotifies = 0 // reset baseline
      gl.resetPlayerCountSelection() // should notify once
      gl.resetPlayerCountSelection() // should be ignored
      obs.askForCountNotifies shouldBe 1
    }
  }
}
