package wizard.undo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.model.player.PlayerFactory
import wizard.model.player.PlayerType.Human
import wizard.controller.PlayerLogic
import wizard.testUtils.TestUtil

class GameUndoCommandsTest extends AnyWordSpec with Matchers {
  "Undo/Redo Commands" should {

    "undo and redo a bid change" in {
      val player = PlayerFactory.createPlayer(Some("P1"), Human)
      player.roundBids shouldBe 0
      TestUtil.simulateInput("3\n") {
        val bid = PlayerLogic.bid(player)
        bid shouldBe 3
      }
      player.roundBids shouldBe 3
      // Undo
      UndoService.manager.undoStep()
      player.roundBids shouldBe 0
      // Redo
      UndoService.manager.redoStep()
      player.roundBids shouldBe 3
    }

    "undo and redo a played card removal from hand" in {
      val player = PlayerFactory.createPlayer(Some("P1"), Human)
      val c1 = Card(Value.One, Color.Red)
      val c2 = Card(Value.Two, Color.Blue)
      player.addHand(Hand(List(c1, c2)))
      // Play second card (index 2)
      TestUtil.simulateInput("2\n1\n") {
        val played = PlayerLogic.playCard(Some(Color.Red), Some(Color.Blue), 0, player)
        played shouldBe c1 // must follow lead -> user corrects to 1
      }
      // After play, one card should be removed
      player.hand.cards.length shouldBe 1
      // Undo removal
      UndoService.manager.undoStep()
      player.hand.cards.length shouldBe 2
      player.hand.cards should contain allOf (c1, c2)
      // Redo removal
      UndoService.manager.redoStep()
      player.hand.cards.length shouldBe 1
    }
  }
}
