package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.AskForPlayerCount

class WizardGUIBackToCountTest extends AnyWordSpec with Matchers {
  "WizardGUI" should {
    "hide undo/redo bar when AskForPlayerCount is received" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)
      // Build player-name UI to ensure bar becomes visible
      gui.testBuildPlayerNameRoot(3)
      gui.testUndoRedoBarPresent shouldBe true
      // Simulate controller asking for player count again
      gui.update("AskForPlayerCount", AskForPlayerCount)
      // After handling, the bar should be hidden
      gui.testUndoRedoBarPresent shouldBe false
    }
  }
}
