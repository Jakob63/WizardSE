package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.{AskForPlayerCount, PlayerCountSelected}

class TextUIBidirectionalUndoTest extends AnyWordSpec with Matchers {
  "TextUI" should {
    "return to AwaitPlayerCount phase when GUI requests AskForPlayerCount after a selection" in {
      val controller = new GameLogic
      val tui = new TextUI(controller)

      // Simulate that a player count was selected from GUI
      controller.playerCountSelected(3)
      // TextUI should have moved to AwaitPlayerNames
      tui.testPhase shouldBe "AwaitPlayerNames"

      // Now simulate GUI pressing undo which triggers AskForPlayerCount via controller reset
      controller.resetPlayerCountSelection()

      // TUI should switch back into AwaitPlayerCount phase so the user can type the count again in TUI
      tui.testPhase shouldBe "AwaitPlayerCount"
    }
  }
}
