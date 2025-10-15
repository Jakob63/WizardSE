package wizard.aView.aView_GUI

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import wizard.controller.GameLogic
import javafx.scene.control.TextField

class WizardGUIPlayerNameFieldsFocusableTest extends AnyWordSpec with Matchers {
  "WizardGUI player name screen" should {
    "create focusable and enabled text fields for all players" in {
      val controller = new GameLogic
      val gui = new WizardGUI(controller)

      // Build the player-name root for 3 players without launching a stage
      val root = gui.testBuildPlayerNameRoot(3)

      // Extract TextFields from the current content (they are direct children in order: title, fields..., start button)
      val textFields = root.getChildren
        .filtered(_ => true) // StackPane children; the VBox is at index 1 when bg exists; index 0 otherwise
        .toArray(Array.ofDim[javafx.scene.Node](0))
        .toList
        .collectFirst { case vbox: javafx.scene.layout.VBox => vbox }
        .map { vbox =>
          vbox.getChildren.toArray(Array.ofDim[javafx.scene.Node](0)).toList.collect { case tf: TextField => tf }
        }
        .getOrElse(Nil)

      textFields.size mustBe 3
      // All should be enabled and focusTraversable
      textFields.foreach { tf =>
        tf.isDisable mustBe false
        tf.isFocusTraversable mustBe true
      }
    }
  }
}
