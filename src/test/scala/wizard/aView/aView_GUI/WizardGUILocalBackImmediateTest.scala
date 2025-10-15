package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import scalafx.scene.layout.VBox
import javafx.scene.control.{Label, Button}

class WizardGUILocalBackImmediateTest extends AnyWordSpec with Matchers {
  "WizardGUI local back" should {
    "immediately revert UI to player-count on undo from names (same container, bar hidden)" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)
      // Enter the player-names screen for 4 players
      gui.testBuildPlayerNameRoot(4)
      gui.testCurrentScreen shouldBe "PlayerNames"
      val refBefore = gui.testContentBoxRef
      refBefore should not be null

      // Simulate the immediate local back used by the Undo button
      gui.testLocalBackToPlayerCount()

      // Should now be back on player-count screen and using the same VBox instance
      gui.testCurrentScreen shouldBe "PlayerCount"
      val refAfter = gui.testContentBoxRef
      refAfter should be theSameInstanceAs refBefore

      // Inspect children to verify they correspond to the player-count composition
      val box = refAfter.asInstanceOf[VBox]
      val kids = box.children
      kids.isEmpty shouldBe false

      val labelTexts = kids.collect { case l: Label => l.getText }
      labelTexts should contain ("Spieleranzahl (3-6)")
      val buttonTexts = kids.collect { case b: Button => b.getText }
      buttonTexts should contain ("Weiter")

      // Undo/redo bar must be hidden on the player-count screen
      gui.testUndoRedoBarPresent shouldBe false
    }
  }
}
