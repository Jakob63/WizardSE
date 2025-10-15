package wizard.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.actionmanagement.InputRouter

class HumanInputTests extends AnyWordSpec with Matchers {
  "Human" should {
    "read bid via InputRouter when provided by GUI" in {
      val h = Human.create("Alice").get
      InputRouter.clear()
      InputRouter.offer("3")
      h.bid() shouldBe 3
    }

    "select card by 1-based index via InputRouter" in {
      val h = Human.create("Bob").get
      // give player a hand with known cards
      val cards = List(
        Card(Value.One, Color.Red),
        Card(Value.Two, Color.Green),
        Card(Value.Three, Color.Blue)
      )
      h.addHand(Hand(cards))
      InputRouter.clear()
      InputRouter.offer("2")
      val c = h.playCard(None, None, 0)
      c shouldBe cards(1)
    }
  }
}
