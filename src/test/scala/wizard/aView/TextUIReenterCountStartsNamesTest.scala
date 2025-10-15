package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.InputRouter

class TextUIReenterCountStartsNamesTest extends AnyWordSpec with Matchers {
  "TextUI" should {
    "start name prompt again after GUI undo and re-entering player count" in {
      System.setProperty("WIZARD_INTERACTIVE", "1")
      InputRouter.clear()
      val gl = new GameLogic
      val tui = new TextUI(gl)

      // Select count first time via controller (simulating GUI selection)
      gl.playerCountSelected(3)
      // Controller emits AskForPlayerNames
      gl.notifyObservers("AskForPlayerNames", wizard.actionmanagement.AskForPlayerNames)
      // Give background name reader a moment to start
      Thread.sleep(50)
      tui.testPhase shouldBe "AwaitPlayerNames"

      // Now simulate GUI undo to go back to count
      gl.resetPlayerCountSelection()
      // Allow TUI to cancel and switch back
      Thread.sleep(50)
      tui.testPhase shouldBe "AwaitPlayerCount"

      // Re-enter player count via TUI (InputRouter)
      InputRouter.offer("3")
      // Allow time for the count reader thread to pick it up and for notifications to propagate
      Thread.sleep(100)

      // After re-selecting, TUI should transition to AwaitPlayerNames and start names reader
      tui.testPhase shouldBe "AwaitPlayerNames"
      tui.testIsNameReaderStarted shouldBe true
    }
  }
}
