package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic

class WizardGUITest extends AnyWordSpec with Matchers {
  "WizardGUI" should {
    "show undo/redo bar from player-name screen onwards" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)
      // build the player-name root (does not require starting JavaFX App Thread for this helper)
      gui.testBuildPlayerNameRoot(3)
      gui.testUndoRedoBarPresent shouldBe true
    }
  }
}
