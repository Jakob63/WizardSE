package wizard.model.cards

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*

class HandTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  val redOne = Card(Value.One, Color.Red)
  val blueTwo = Card(Value.Two, Color.Blue)
  val wizard = Card(Value.WizardKarte, Color.Green)
  val chester = Card(Value.Chester, Color.Yellow)

  "A Hand" should {
    "be empty initially" in {
      val hand = Hand(Nil)
      hand.isEmpty should be (true)
      hand.cards.size should be (0)
    }

    "allow adding cards" in {
      val hand = Hand(Nil).addCards(List(redOne, blueTwo))
      hand.isEmpty should be (false)
      hand.cards should contain allOf (redOne, blueTwo)
      hand.cards.size should be (2)
    }

    "allow removing cards" in {
      val hand = Hand(List(redOne, blueTwo)).removeCard(redOne)
      hand.cards should not contain (redOne)
      hand.cards should contain (blueTwo)
      hand.cards.size should be (1)
    }

    "correctly check for color (excluding special cards)" in {
      val hand = Hand(List(redOne, wizard, chester))
      hand.hasColor(Color.Red) should be (true)
      hand.hasColor(Color.Green) should be (false)
      hand.hasColor(Color.Yellow) should be (false)
      hand.hasColor(Color.Blue) should be (false)
    }

    "correctly check for value" in {
      val hand = Hand(List(redOne, wizard))
      hand.hasValue(Value.One) should be (true)
      hand.hasValue(Value.WizardKarte) should be (true)
      hand.hasValue(Value.Two) should be (false)
    }

    "correctly check for trump color (excluding special cards)" in {
      val hand = Hand(List(redOne, wizard, chester))
      hand.hasTrumpColor(Color.Red) should be (true)
      hand.hasTrumpColor(Color.Green) should be (false)
      hand.hasTrumpColor(Color.Yellow) should be (false)
    }

    "allow getting a card by index" in {
      val hand = Hand(List(redOne, blueTwo))
      hand.getCard(0) should be (redOne)
      hand.getCard(1) should be (blueTwo)
    }
  }
}
