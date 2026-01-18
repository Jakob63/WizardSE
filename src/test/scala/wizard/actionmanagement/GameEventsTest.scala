package wizard.actionmanagement

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value}

class GameEventsTest extends AnyWordSpec with Matchers {

  "GameEvents" should {
    "support SetPlayer event" in {
      val event = SetPlayer("Alice")
      event.player1 should be("Alice")
    }

    "support PlayerCountSelected event" in {
      val event = PlayerCountSelected(4)
      event.count should be(4)
    }

    "support CardsDealt event" in {
      val p1 = new Player("P1") {
        override def bid(): Int = 0
        override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = Card(Value.One, Color.Red)
      }
      val event = CardsDealt(List(p1))
      event.players should contain(p1)
    }

    "support ShowHand event" in {
      val p1 = new Player("P1") {
        override def bid(): Int = 0
        override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = Card(Value.One, Color.Red)
      }
      val event = ShowHand(p1)
      event.player should be(p1)
    }

    "have all case objects defined" in {
      StartGame shouldBe a [GameEvent]
      PlayGame shouldBe a [GameEvent]
      AskForPlayerCount shouldBe a [GameEvent]
      AskForPlayerNames shouldBe a [GameEvent]
      CardAuswahl shouldBe a [GameEvent]
      RoundOver shouldBe a [GameEvent]
      TrickOver shouldBe a [GameEvent]
      Bid shouldBe a [GameEvent]
      PlayCard shouldBe a [GameEvent]
      EndRound shouldBe a [GameEvent]
      EndTrick shouldBe a [GameEvent]
      EndGame shouldBe a [GameEvent]
      EndBid shouldBe a [GameEvent]
      EndPlayCard shouldBe a [GameEvent]
      EndRoundOver shouldBe a [GameEvent]
      EndTrickOver shouldBe a [GameEvent]
      EndGameOver shouldBe a [GameEvent]
      EndStartGame shouldBe a [GameEvent]
      EndPlayGame shouldBe a [GameEvent]
    }
  }
}
