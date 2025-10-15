package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic

class TextUICountReaderAfterResetTest extends AnyWordSpec with Matchers {
  "TextUI" should {
    "start count reader again after GUI-triggered reset to AskForPlayerCount" in {
      val gl = new GameLogic
      val tui = new TextUI(gl)

      // Move into name entry phase
      gl.playerCountSelected(3)
      gl.notifyObservers("AskForPlayerNames", wizard.actionmanagement.AskForPlayerNames)

      // Let the background name reader start
      Thread.sleep(50)

      // Simulate GUI undo -> back to count
      gl.resetPlayerCountSelection()

      // Allow TUI to process and spin up the count reader
      Thread.sleep(50)

      tui.testPhase shouldBe "AwaitPlayerCount"
      tui.testIsCountReaderStarted shouldBe true
    }
  }
}
