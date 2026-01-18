package wizard.model.rounds

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value, Hand}

class RoundTest extends AnyWordSpec with Matchers {

  class TestPlayer(name: String) extends Player(name) {
    override def bid(): Int = 0
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = 
      Card(Value.One, Color.Red)
  }

  "A Round" should {
    val p1 = new TestPlayer("P1")
    val p2 = new TestPlayer("P2")
    val players = List(p1, p2)
    
    "be initialized correctly" in {
      val round = new Round(players)
      round.trump should be(None)
      round.leadColor should be(None)
      round.currentPlayerIndex should be(0)
    }

    "allow setting trump and lead color" in {
      val round = new Round(players)
      round.setTrump(Some(Color.Red))
      round.trump should be(Some(Color.Red))
      
      round.leadColor = Some(Color.Blue)
      round.leadColor should be(Some(Color.Blue))
    }

    "iterate through players correctly" in {
      val round = new Round(players)
      round.nextPlayer() should be(p1)
      round.currentPlayerIndex should be(1)
      round.nextPlayer() should be(p2)
      round.currentPlayerIndex should be(0)
      round.nextPlayer() should be(p1)
    }

    "check if round is over" in {
      val round = new Round(players)
      p1.hand = Hand(List(Card(Value.One, Color.Red)))
      p2.hand = Hand(List(Card(Value.Two, Color.Blue)))
      round.isOver() should be(false)

      p1.hand = Hand(Nil)
      round.isOver() should be(false)

      p2.hand = Hand(Nil)
      round.isOver() should be(true)
    }

    "finalize round correctly and reset round stats" in {
      val round = new Round(players)
      p1.points = 100
      p1.roundPoints = 20
      p1.roundTricks = 2
      p1.roundBids = 2

      p2.points = 50
      p2.roundPoints = -10
      p2.roundTricks = 1
      p2.roundBids = 2

      round.finalizeRound()

      p1.points should be(120)
      p1.tricks should be(2)
      p1.bids should be(2)
      p1.roundPoints should be(0)
      p1.roundTricks should be(0)
      p1.roundBids should be(0)

      p2.points should be(40)
      p2.tricks should be(1)
      p2.bids should be(2)
      p2.roundPoints should be(0)
      p2.roundTricks should be(0)
      p2.roundBids should be(0)
    }

    "have a proper toString representation" in {
      val round = new Round(players)
      round.setTrump(Some(Color.Green))
      val s = round.toString
      s should include("Trump: Some(Green)")
      s should include("P1")
      s should include("P2")
    }
  }
}
