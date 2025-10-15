package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.AskForPlayerCount

class WizardGUINameSwitchCancellationTest extends AnyWordSpec with Matchers {
  "WizardGUI" should {
    "cancel a pending switch-to-names if AskForPlayerCount arrives afterwards (race guard)" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)

      // Start on initial screen (PlayerCount)
      gui.testCurrentScreen shouldBe "PlayerCount"
      val before = gui.testContentBoxRef
      before should not be null

      // Simulate that a PlayerCountSelected was processed and would schedule a switch with a captured epoch
      val capturedEpoch = gui.testGetNavEpoch

      // Now an AskForPlayerCount arrives (e.g., user pressed Undo very fast) -> should increment epoch
      gui.update("AskForPlayerCount", AskForPlayerCount)
      gui.testCurrentScreen shouldBe "PlayerCount"
      // Undo/redo bar should be hidden on player-count screen
      gui.testUndoRedoBarPresent shouldBe false

      // Try to apply the previously captured switch; it must be ignored due to stale epoch
      gui.testSwitchToPlayerNames(3, capturedEpoch)
      gui.testCurrentScreen shouldBe "PlayerCount"
      gui.testUndoRedoBarPresent shouldBe false

      // Now apply with the current epoch -> should switch to PlayerNames and show undo/redo bar
      gui.testSwitchToPlayerNames(3, gui.testGetNavEpoch)
      gui.testCurrentScreen shouldBe "PlayerNames"
      gui.testUndoRedoBarPresent shouldBe true
    }
  }
}
