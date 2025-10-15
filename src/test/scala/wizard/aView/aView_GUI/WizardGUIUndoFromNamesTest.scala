package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import scalafx.scene.layout.VBox
import javafx.scene.control.{Label, Button}

class WizardGUIUndoFromNamesTest extends AnyWordSpec with Matchers {
  "WizardGUI" should {
    "revert to the exact player-count UI (same container) when undo is pressed on player-names" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)
      // Enter the player-names screen for 3 players
      gui.testBuildPlayerNameRoot(3)
      gui.testCurrentScreen shouldBe "PlayerNames"
      val refBefore = gui.testContentBoxRef
      refBefore should not be null

      // Simulate clicking the top-left undo while on the names screen (synchronous helper)
      gui.testSimulateUndoFromNames()

      // Should now be back on player-count screen and using the same VBox instance
      gui.testCurrentScreen shouldBe "PlayerCount"
      val refAfter = gui.testContentBoxRef
      refAfter should be theSameInstanceAs refBefore

      // Inspect children to verify they correspond to the player-count composition
      val box = refAfter.asInstanceOf[VBox]
      val kids = box.children
      kids.isEmpty shouldBe false

      // Expect there is a Label with the exact text for player count prompt and a Button with text "Weiter"
      val labelTexts = kids.collect { case l: Label => l.getText }
      labelTexts should contain ("Spieleranzahl (3-6)")
      val buttonTexts = kids.collect { case b: Button => b.getText }
      buttonTexts should contain ("Weiter")

      // Undo/redo bar must be hidden on the player-count screen
      gui.testUndoRedoBarPresent shouldBe false
    }
  }
}
