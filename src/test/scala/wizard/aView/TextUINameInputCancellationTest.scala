package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.AskForPlayerCount

class TextUINameInputCancellationTest extends AnyWordSpec with Matchers {
  "TextUI name input" should {
    "cancel and return to AwaitPlayerCount when AskForPlayerCount arrives (e.g., GUI undo)" in {
      val gl = new GameLogic
      val tui = new TextUI(gl)

      // Simulate selecting player count to move into name entry phase
      gl.playerCountSelected(3)
      // Trigger the AskForPlayerNames phase
      gl.notifyObservers("AskForPlayerNames", wizard.actionmanagement.AskForPlayerNames)

      // Give the background name reader a moment to start
      Thread.sleep(50)

      // Now simulate GUI triggering a reset to player count
      gl.resetPlayerCountSelection()

      // Allow a brief time for the TUI to process and cancel the name reader
      Thread.sleep(50)

      tui.testPhase shouldBe "AwaitPlayerCount"
    }
  }
}
