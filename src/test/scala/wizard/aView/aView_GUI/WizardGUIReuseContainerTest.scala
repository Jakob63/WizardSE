package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.AskForPlayerCount

class WizardGUIReuseContainerTest extends AnyWordSpec with Matchers {
  "WizardGUI" should {
    "reuse the same content VBox when navigating back to player count (AskForPlayerCount)" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)
      // Build player-name UI (this also ensures undo/redo becomes visible)
      gui.testBuildPlayerNameRoot(4)
      val refBefore = gui.testContentBoxRef
      refBefore should not be null
      // Simulate controller asking for player count again (e.g., undo at first name)
      gui.update("AskForPlayerCount", AskForPlayerCount)
      // The content container reference should remain identical (no scene/root replacement)
      val refAfter = gui.testContentBoxRef
      refAfter shouldBe theSameInstanceAs (refBefore)
      // And the undo/redo bar should be hidden on the count screen
      gui.testUndoRedoBarPresent shouldBe false
    }
  }
}
