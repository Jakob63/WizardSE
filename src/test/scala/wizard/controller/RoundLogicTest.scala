package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value, Hand, Dealer}
import wizard.model.rounds.Round
import wizard.actionmanagement.{CardsDealt, Observer}

class RoundLogicTest extends AnyWordSpec with Matchers {

  class TestPlayer(nameStr: String) extends Player(nameStr) {
    var nextBid: Int = 0
    var nextCard: Card = _
    
    override def bid(): Int = nextBid
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = {
      if (nextCard == null) throw new RuntimeException("nextCard not set")
      nextCard
    }
  }

  class TestObserver extends Observer {
    var lastMessage: String = ""
    override def update(updateMSG: String, obj: Any*): Any = {
      lastMessage = updateMSG
    }
  }

  "RoundLogic" should {
    val roundLogic = new RoundLogic
    val players = List(new TestPlayer("P1"), new TestPlayer("P2"), new TestPlayer("P3"))

    "correctly determine the winner of a trick" in {
      val round = new Round(players)
      round.setTrump(Some(Color.Red))
      
      val trick1 = List(
        (players(0), Card(Value.Seven, Color.Red)),
        (players(1), Card(Value.WizardKarte, Color.Blue)),
        (players(2), Card(Value.Thirteen, Color.Red))
      )
      roundLogic.trickwinner(trick1, round) should be(players(1))

      val trick2 = List(
        (players(0), Card(Value.Seven, Color.Red)),
        (players(1), Card(Value.Thirteen, Color.Blue)),
        (players(2), Card(Value.Two, Color.Red))
      )
      roundLogic.trickwinner(trick2, round) should be(players(0))

      round.setTrump(None)
      val trick3 = List(
        (players(0), Card(Value.Ten, Color.Blue)),
        (players(1), Card(Value.Twelve, Color.Blue)),
        (players(2), Card(Value.Three, Color.Red))
      )
      roundLogic.trickwinner(trick3, round) should be(players(1))

      val trick4 = List(
        (players(0), Card(Value.Chester, Color.Red)),
        (players(1), Card(Value.Chester, Color.Blue)),
        (players(2), Card(Value.Chester, Color.Green))
      )
      roundLogic.trickwinner(trick4, round) should be(players(0))

      val trick5 = List(
        (players(0), Card(Value.Chester, Color.Red)),
        (players(1), Card(Value.WizardKarte, Color.Blue)),
        (players(2), Card(Value.WizardKarte, Color.Green))
      )
      roundLogic.trickwinner(trick5, round) should be(players(1))

      round.setTrump(None)
      round.leadColor = Some(Color.Red)
      val trick6 = List(
        (players(0), Card(Value.Two, Color.Blue)),
        (players(1), Card(Value.Five, Color.Green)),
        (players(2), Card(Value.Ten, Color.Yellow))
      )
      roundLogic.trickwinner(trick6, round) should be(players(0))
    }

    "play a round correctly (simplified)" in {
      val p1 = new TestPlayer("P1")
      val p2 = new TestPlayer("P2")
      val p3 = new TestPlayer("P3")
      val testPlayers = List(p1, p2, p3)
      
      p1.nextBid = 1
      p2.nextBid = 0
      p3.nextBid = 1
      p1.roundBids = 1
      p2.roundBids = 0
      p3.roundBids = 1
      
      val c1 = Card(Value.Five, Color.Red)
      val c2 = Card(Value.Ten, Color.Red)
      val c3 = Card(Value.Two, Color.Blue)
      
      p1.nextCard = c1
      p2.nextCard = c2
      p3.nextCard = c3
      
      p1.hand = Hand(List(c1))
      p2.hand = Hand(List(c2))
      p3.hand = Hand(List(c3))
      
      Dealer.allCards = List(c1, c2, c3, Card(Value.Seven, Color.Red)) ++ Dealer.allCards.drop(4)
      Dealer.index = 0
      
      roundLogic.playRound(1, testPlayers, isResumed = true)
      
      p2.roundTricks should be(1)
      p1.roundTricks should be(0)
      p3.roundTricks should be(0)
      
      p1.points should be(-10)
      p2.points should be(-10)
      p3.points should be(-10)
    }
    
    "static methods should work" in {
       val round = new Round(players)
       val trick = List((players(0), Card(Value.WizardKarte, Color.Blue)))
       RoundLogic.trickwinner(trick, round) should be(players(0))
    }
  }
}
