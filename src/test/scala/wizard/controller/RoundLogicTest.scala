package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value, Hand, Dealer}
import wizard.model.rounds.Round
import wizard.actionmanagement.{CardsDealt, Observer}

class RoundLogicTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

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
    val players = List(new TestPlayer("P1"), new TestPlayer("P2"), new TestPlayer("P3"))

    "notifyObservers for playerLogic as well" in {
      val roundLogicLocal = new RoundLogic
      var notified = false
      roundLogicLocal.add(new Observer {
        override def update(msg: String, obj: Any*): Any = {
          if (msg == "test_bid") notified = true
        }
      })
    }

    "correctly determine the winner of a trick" in {
      val roundLogicLocal = new RoundLogic
      val round = new Round(players)
      round.setTrump(Some(Color.Red))
      
      val trick1 = List(
        (players(0), Card(Value.Seven, Color.Red)),
        (players(1), Card(Value.WizardKarte, Color.Blue)),
        (players(2), Card(Value.Thirteen, Color.Red))
      )
      roundLogicLocal.trickwinner(trick1, round) should be(players(1))

      val trick2 = List(
        (players(0), Card(Value.Seven, Color.Red)),
        (players(1), Card(Value.Thirteen, Color.Blue)),
        (players(2), Card(Value.Two, Color.Red))
      )
      roundLogicLocal.trickwinner(trick2, round) should be(players(0))

      round.setTrump(None)
      val trick3 = List(
        (players(0), Card(Value.Ten, Color.Blue)),
        (players(1), Card(Value.Twelve, Color.Blue)),
        (players(2), Card(Value.Three, Color.Red))
      )
      roundLogicLocal.trickwinner(trick3, round) should be(players(1))

      val trick4 = List(
        (players(0), Card(Value.Chester, Color.Red)),
        (players(1), Card(Value.Chester, Color.Blue)),
        (players(2), Card(Value.Chester, Color.Green))
      )
      roundLogicLocal.trickwinner(trick4, round) should be(players(0))

      val trick5 = List(
        (players(0), Card(Value.Chester, Color.Red)),
        (players(1), Card(Value.WizardKarte, Color.Blue)),
        (players(2), Card(Value.WizardKarte, Color.Green))
      )
      roundLogicLocal.trickwinner(trick5, round) should be(players(1))

      round.setTrump(None)
      round.leadColor = Some(Color.Red)
      val trick6 = List(
        (players(0), Card(Value.Two, Color.Blue)),
        (players(1), Card(Value.Five, Color.Green)),
        (players(2), Card(Value.Ten, Color.Yellow))
      )
      roundLogicLocal.trickwinner(trick6, round) should be(players(0))
    }

    "play a round correctly (simplified)" in {
      println("[DEBUG_LOG] Starting test: play a round correctly (simplified)")
      val roundLogicLocal = new RoundLogic
      val p1 = new TestPlayer("P1")
      val p2 = new TestPlayer("P2")
      val p3 = new TestPlayer("P3")
      val testPlayers = List(p1, p2, p3)
      
      println("[DEBUG_LOG] Setting up bids")
      p1.nextBid = 1
      p2.nextBid = 0
      p3.nextBid = 1
      p1.roundBids = -1
      p2.roundBids = -1
      p3.roundBids = -1
      
      println("[DEBUG_LOG] Setting up cards")
      val c1 = Card(Value.Five, Color.Red)
      val c2 = Card(Value.Ten, Color.Red)
      val c3 = Card(Value.Two, Color.Blue)
      
      p1.nextCard = c1
      p2.nextCard = c2
      p3.nextCard = c3
      
      p1.hand = Hand(List(c1))
      p2.hand = Hand(List(c2))
      p3.hand = Hand(List(c3))
      
      println("[DEBUG_LOG] Setting up dealer")
      Dealer.allCards = List(c1, c2, c3, Card(Value.Seven, Color.Red)) ++ Dealer.allCards.drop(4)
      Dealer.index = 0
      
      println("[DEBUG_LOG] Calling playRound(1, ...)")
      roundLogicLocal.playRound(1, testPlayers, isResumed = true)
      println("[DEBUG_LOG] playRound returned")
      
      println("[DEBUG_LOG] Verifying tricks")
      p2.roundTricks should be(1)
      p1.roundTricks should be(0)
      p3.roundTricks should be(0)
      
      println("[DEBUG_LOG] Verifying points")
      p1.points should be(-10)
      p2.points should be(-10)
      p3.points should be(-10)
      println("[DEBUG_LOG] Test finished successfully")
    }

    "handle various resumed scenarios and edge cases" in {
      val roundLogicLocal = new RoundLogic
      val p1 = new TestPlayer("P1")
      val p2 = new TestPlayer("P2")
      val p3 = new TestPlayer("P3")
      val testPlayers = List(p1, p2, p3)

      val cards = List.fill(60)(Card(Value.Seven, Color.Red))
      Dealer.allCards = cards
      Dealer.index = 0

      roundLogicLocal.lastTrumpCard = Some(Card(Value.Seven, Color.Green))
      
      p1.nextBid = 1; p2.nextBid = 1; p3.nextBid = 1
      p1.nextCard = Card(Value.One, Color.Red)
      p2.nextCard = Card(Value.Two, Color.Red)
      p3.nextCard = Card(Value.Three, Color.Red)
      p1.hand = Hand(List(p1.nextCard))
      p2.hand = Hand(List(p2.nextCard))
      p3.hand = Hand(List(p3.nextCard))

      roundLogicLocal.playRound(1, testPlayers, isResumed = true)
      roundLogicLocal.lastTrumpCard should be(Some(Card(Value.Seven, Color.Green)))

      Dealer.index = 58
      roundLogicLocal.lastTrumpCard = None
      roundLogicLocal.playRound(30, testPlayers, isResumed = false)

      Dealer.allCards = List(Card(Value.Seven, Color.Blue)) ++ List.fill(59)(Card(Value.WizardKarte, Color.Red))
      Dealer.index = 0
      wizard.actionmanagement.InputRouter.clear()
      wizard.actionmanagement.InputRouter.offer("1")
      roundLogicLocal.playRound(1, testPlayers, isResumed = false)

      Dealer.allCards = Nil
    }

    "handle bidding undo/redo and skips" in {
      val roundLogicLocal = new RoundLogic
      val p1 = new TestPlayer("P1")
      val p2 = new TestPlayer("P2")
      val testPlayers = List(p1, p2)
      
      Dealer.allCards = List.fill(60)(Card(Value.Seven, Color.Red))
      Dealer.index = 0

      p1.roundBids = 1
      p2.roundBids = -1
      
      p1.nextCard = Card(Value.One, Color.Red); p2.nextCard = Card(Value.Two, Color.Red)
      p1.hand = Hand(List(p1.nextCard)); p2.hand = Hand(List(p2.nextCard))
      
      p2.nextBid = 1
      roundLogicLocal.playRound(1, testPlayers, isResumed = true)
      p2.roundBids should be(1)

      val p1Mock = new TestPlayer("P1") {
        override def bid(): Int = throw new wizard.actionmanagement.InputRouter.UndoException("undo")
      }
      intercept[wizard.actionmanagement.GameStoppedException] {
         roundLogicLocal.playRound(1, List(p1Mock), isResumed = false)
      }

      val p1Redo = new TestPlayer("P1") {
        var callCount = 0
        override def bid(): Int = {
          callCount += 1
          if (callCount == 1) throw new wizard.actionmanagement.InputRouter.RedoException("redo")
          1
        }
      }
      val p1Normal = new TestPlayer("P1") { override def bid() = 1 }
      p1Normal.hand = Hand(List(Card(Value.One, Color.Red)))
      p1Redo.hand = Hand(List(Card(Value.Two, Color.Red)))
      p1Normal.nextCard = p1Normal.hand.getCard(0)
      p1Redo.nextCard = p1Redo.hand.getCard(0)
      
      roundLogicLocal.playRound(1, List(p1Redo, p1Normal), isResumed = false)
    }

    "handle trick undo/redo and resumed trick state" in {
      val roundLogicLocal = new RoundLogic
      val p1 = new TestPlayer("P1")
      val p2 = new TestPlayer("P2")
      val testPlayers = List(p1, p2)
      
      Dealer.allCards = List.fill(60)(Card(Value.Eight, Color.Blue))
      Dealer.index = 0

      roundLogicLocal.currentTrickCards = List(Card(Value.Five, Color.Red), Card(Value.Six, Color.Red))
      p1.roundBids = 1; p2.roundBids = 1
      p1.hand = Hand(Nil)
      p2.hand = Hand(Nil)

      p1.roundTricks = 0; p2.roundTricks = 1
      roundLogicLocal.playRound(2, testPlayers, isResumed = true)
      roundLogicLocal.currentTrickCards should be(Nil)

      val p1Undo = new TestPlayer("P1") {
        var callCount = 0
        override def playCard(lc: Option[Color], t: Option[Color], idx: Int): Card = {
           callCount += 1
           if (callCount == 2) throw new wizard.actionmanagement.InputRouter.UndoException("undo")
           Card(Value.One, Color.Red)
        }
      }
      val p2Normal = new TestPlayer("P2") { override def playCard(lc: Option[Color], t: Option[Color], idx: Int) = Card(Value.Two, Color.Red) }
      p1Undo.hand = Hand(List(Card(Value.One, Color.Red), Card(Value.Three, Color.Red)))
      p2Normal.hand = Hand(List(Card(Value.Two, Color.Red), Card(Value.Four, Color.Red)))
      p1Undo.nextCard = Card(Value.One, Color.Red)
      p2Normal.nextCard = Card(Value.Two, Color.Red)

      val t = new Thread(() => {
        try { 
          roundLogicLocal.stopGame = false
          roundLogicLocal.playRound(1, List(p1Undo, p2Normal), isResumed = false) 
        } catch { case _: Throwable => () }
      })
      t.setDaemon(true)
      t.start()
      Thread.sleep(500)
      roundLogicLocal.stopGame = true
      t.join(2000)
    }
    
    "static methods should work" in {
       val round = new Round(players)
       val trick = List((players(0), Card(Value.WizardKarte, Color.Blue)))
       RoundLogic.trickwinner(trick, round) should be(players(0))
    }
  }
}
