package wizard.undo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value, Hand}
import wizard.controller.GameLogic
import wizard.actionmanagement.Observer

class UndoTest extends AnyWordSpec with Matchers {

  class TestPlayer(name: String) extends Player(name) {
    override def bid(): Int = 0
    override def playCard(lc: Option[Color], t: Option[Color], idx: Int): Card = Card(Value.One, Color.Red)
  }

  "UndoManager" should {
    "correctly do, undo and redo steps" in {
      val manager = new UndoManager
      val player = new TestPlayer("Test")
      player.roundBids = 0
      
      val command = new BidCommand(player, 5)
      
      manager.doStep(command)
      player.roundBids should be(5)
      
      // Test that redoStack is cleared on new doStep
      manager.undoStep()
      player.roundBids should be(0)
      
      val command2 = new BidCommand(player, 3)
      manager.doStep(command2)
      player.roundBids should be(3)
      
      manager.redoStep() // Should do nothing as redoStack should be empty
      player.roundBids should be(3)

      manager.undoStep()
      player.roundBids should be(0)
      
      manager.redoStep()
      player.roundBids should be(3)
    }

    "do nothing on undo/redo when stacks are empty" in {
      val manager = new UndoManager
      // Should not throw exceptions
      manager.undoStep()
      manager.redoStep()
    }
  }

  "UndoService" should {
    "provide a singleton UndoManager" in {
      UndoService.manager should not be null
      UndoService.manager shouldBe a[UndoManager]
    }
  }

  "BidCommand" should {
    "update player bids" in {
      val player = new TestPlayer("Test")
      player.roundBids = 1
      val command = new BidCommand(player, 3)
      
      command.doStep()
      player.roundBids should be(3)
      
      command.undoStep()
      player.roundBids should be(1)
      
      command.redoStep()
      player.roundBids should be(3)
    }
  }

  "PlayCardCommand" should {
    "update player hand" in {
      val player = new TestPlayer("Test")
      val card1 = Card(Value.One, Color.Red)
      val card2 = Card(Value.Two, Color.Blue)
      player.hand = Hand(List(card1, card2))
      
      val nextHand = Hand(List(card2))
      val command = new PlayCardCommand(player, nextHand)
      
      command.doStep()
      player.hand should be(nextHand)
      
      command.undoStep()
      player.hand.cards should contain(card1)
      
      command.redoStep()
      player.hand should be(nextHand)
    }
  }

  "SetPlayerNameCommand" should {
    "update player name" in {
      val player = new TestPlayer("OldName")
      val command = new SetPlayerNameCommand(player, "NewName")
      
      command.doStep()
      player.name should be("NewName")
      
      command.undoStep()
      player.name should be("OldName")
      
      command.redoStep()
      player.name should be("NewName")
    }
  }

  "StartGameCommand" should {
    "handle undo and redo via GameLogic" in {
      val gameLogic = new GameLogic
      val players = List(new TestPlayer("P1"), new TestPlayer("P2"))
      
      var notified = false
      gameLogic.add(new Observer {
        override def update(msg: String, obj: Any*): Any = {
          if (msg == "AskForPlayerNames") notified = true
        }
      })
      
      val command = new StartGameCommand(gameLogic, players)
      
      // doStep does nothing currently as per implementation
      command.doStep()
      
      // undoStep should stop game and notify
      command.undoStep()
      notified should be(true)
      
      // redoStep should call setPlayersFromRedo
      // Since it starts a thread, we just verify it doesn't crash here
      command.redoStep()
    }
  }
}
