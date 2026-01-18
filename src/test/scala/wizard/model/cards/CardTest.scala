package wizard.model.cards

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*

class CardTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  "A Color" should {
    "have the correct values" in {
      Color.values should contain allOf (Color.Red, Color.Green, Color.Blue, Color.Yellow)
    }
    "be convertible to ANSI strings" in {
      colorToAnsi(Color.Red) should be (Console.RED)
      colorToAnsi(Color.Green) should be (Console.GREEN)
      colorToAnsi(Color.Blue) should be (Console.BLUE)
      colorToAnsi(Color.Yellow) should be (Console.YELLOW)
    }
  }

  "A Value" should {
    "have the correct card types" in {
      Value.Chester.cardType() should be ("C")
      Value.One.cardType() should be ("1")
      Value.WizardKarte.cardType() should be ("W")
      Value.Thirteen.cardType() should be ("13")
    }
    "be convertible to ANSI strings (reset for special cards)" in {
      valueToAnsi(Value.Chester) should be (Console.RESET)
      valueToAnsi(Value.WizardKarte) should be (Console.RESET)
      valueToAnsi(Value.One) should be ("")
      valueToAnsi(Value.Thirteen) should be ("")
    }
  }

  "A Card" should {
    "have a value and a color" in {
      val card = Card(Value.Seven, Color.Blue)
      card.value should be (Value.Seven)
      card.color should be (Color.Blue)
    }
    "have a correct string representation" in {
      val card = Card(Value.Eight, Color.Red)
      card.toString should be ("Eight of Red")
    }
  }
}
