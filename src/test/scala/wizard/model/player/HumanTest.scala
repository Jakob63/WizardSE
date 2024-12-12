package wizard.model.player

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.testUtils.TestUtil

class HumanTest extends AnyWordSpec with Matchers {

    "Human" should {

        "play a valid card when an invalid card is attempted first" in {
            val player = new Human("TestPlayer")
            val hand = Hand(List(Card(Value.One, Color.Red), Card(Value.One, Color.Blue)))
            player.addHand(hand)
            var card: Option[Card] = None
            TestUtil.simulateInput("3\n1\n") {
                card = Some(player.playCard(Color.Red, Color.Blue, 0))
            }
            card shouldBe Some(Card(Value.One, Color.Red))
        }
        "handle invalid card index correctly" in {
            val player = new Human("TestPlayer")
            val hand = Hand(List(Card(Value.One, Color.Red), Card(Value.One, Color.Blue)))
            player.addHand(hand)
            var card: Option[Card] = None
            TestUtil.simulateInput("invalid\n1\n") {
                card = Some(player.playCard(Color.Red, Color.Blue, 0))
            }
            card shouldBe Some(Card(Value.One, Color.Red))
        }
    }
}