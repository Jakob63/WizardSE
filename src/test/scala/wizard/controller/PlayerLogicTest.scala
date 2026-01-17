package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value, Hand}
import scala.compiletime.uninitialized

class PlayerLogicTest extends AnyWordSpec with Matchers {

  class TestPlayer(nameStr: String) extends Player(nameStr) {
    var nextBid: Int = 0
    var nextCard: Card = uninitialized
    
    override def bid(): Int = nextBid
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = nextCard
  }

  "PlayerLogic" should {
    val playerLogic = new PlayerLogic

    "calculate points correctly when bids equal tricks" in {
      val player = new TestPlayer("Test")
      player.points = 100
      player.roundBids = 3
      player.roundTricks = 3
      
      // 100 + 20 + 3 * 10 = 150
      playerLogic.calculatePoints(player) should be(150)
    }

    "calculate points correctly when bids do not equal tricks" in {
      val player = new TestPlayer("Test")
      player.points = 100
      player.roundBids = 3
      player.roundTricks = 2
      
      playerLogic.calculatePoints(player) should be(90)
      
      player.roundTricks = 4
      playerLogic.calculatePoints(player) should be(90)
    }

    "add points to player correctly" in {
      val player = new TestPlayer("Test")
      player.points = 100
      player.roundBids = 2
      player.roundTricks = 2
      
      playerLogic.addPoints(player)
      player.points should be(140)
      
      player.roundTricks = 1
      playerLogic.addPoints(player) // 140 - 10 = 130
      player.points should be(130)
    }

    "handle bidding" in {
      val player = new TestPlayer("Test")
      player.nextBid = 2
      
      val result = playerLogic.bid(player, 5)
      result should be(2)
      player.roundBids should be(2)
    }

    "validate bidding and retry if invalid" in {
      val player = new TestPlayer("Test") {
        var callCount = 0
        override def bid(): Int = {
          callCount += 1
          if (callCount == 1) 10 
          else if (callCount == 2) -1
          else 3
        }
      }
      
      val result = playerLogic.bid(player, 5)
      result should be(3)
      player.roundBids should be(3)
    }
    
    "handle card playing" in {
      val card = Card(Value.Seven, Color.Blue)
      val player = new TestPlayer("Test")
      player.hand = Hand(List(card))
      player.nextCard = card
      
      val result = playerLogic.playCard(None, None, 0, player)
      result should be(card)
      player.hand.cards should not contain (card)
    }
    
    "enforce following lead color" in {
      val blueSeven = Card(Value.Seven, Color.Blue)
      val redEight = Card(Value.Eight, Color.Red)
      val player = new TestPlayer("Test") {
          var callCount = 0
          override def playCard(lc: Option[Color], t: Option[Color], idx: Int): Card = {
              callCount += 1
              if (callCount == 1) redEight 
              else blueSeven 
          }
      }
      player.hand = Hand(List(blueSeven, redEight))
      
      val result = playerLogic.playCard(Some(Color.Blue), None, 0, player)
      result should be(blueSeven)
      player.hand.cards should not contain (blueSeven)
      player.hand.cards should contain (redEight)
    }

    "allow playing Wizard regardless of lead color" in {
        val wizard = Card(Value.WizardKarte, Color.Blue) 
        val blueSeven = Card(Value.Seven, Color.Blue)
        val player = new TestPlayer("Test")
        player.hand = Hand(List(wizard, blueSeven))
        player.nextCard = wizard
        
        val result = playerLogic.playCard(Some(Color.Blue), None, 0, player)
        result should be(wizard)
        player.hand.cards should not contain (wizard)
    }

    "allow playing Narre regardless of lead color" in {
        val narre = Card(Value.Chester, Color.Blue)
        val blueSeven = Card(Value.Seven, Color.Blue)
        val player = new TestPlayer("Test")
        player.hand = Hand(List(narre, blueSeven))
        player.nextCard = narre
        
        val result = playerLogic.playCard(Some(Color.Blue), None, 0, player)
        result should be(narre)
        player.hand.cards should not contain (narre)
    }

    "static methods should work" in {
      val player = new TestPlayer("Test")
      player.points = 100
      player.roundBids = 2
      player.roundTricks = 2
      
      PlayerLogic.calculatePoints(player) should be(140)
      PlayerLogic.addPoints(player)
      player.points should be(140)
      
      player.nextBid = 3
      PlayerLogic.bid(player, 5) should be(3)
      
      val card = Card(Value.Nine, Color.Red)
      player.hand = Hand(List(card))
      player.nextCard = card
      PlayerLogic.playCard(None, None, 0, player) should be(card)
    }
  }
}
