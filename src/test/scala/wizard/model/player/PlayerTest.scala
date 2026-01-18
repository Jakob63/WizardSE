package wizard.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.model.cards.{Card, Color, Hand, Value}

class PlayerTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  class ConcretePlayer(name: String) extends Player(name) {
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = 
      Card(Value.Seven, Color.Red)
    override def bid(): Int = 0
  }

  "A Player" should {
    "have a name" in {
      val player = new ConcretePlayer("TestPlayer")
      player.name should be("TestPlayer")
    }

    "be able to change its name" in {
      val player = new ConcretePlayer("OldName")
      player.name = "NewName"
      player.name should be("NewName")
    }

    "start with zero points and tricks" in {
      val player = new ConcretePlayer("Test")
      player.points should be(0)
      player.tricks should be(0)
      player.bids should be(0)
      player.roundPoints should be(0)
      player.roundBids should be(0)
      player.roundTricks should be(0)
    }

    "allow adding a hand" in {
      val player = new ConcretePlayer("Test")
      val hand = Hand(List(Card(Value.One, Color.Blue)))
      player.addHand(hand)
      player.hand should be(hand)
    }

    "allow adding total tricks" in {
      val player = new ConcretePlayer("Test")
      player.addTricks(2)
      player.tricks should be(2)
      player.addTricks(3)
      player.tricks should be(5)
    }
  }
}
