package wizard.cards

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.Model.cards.{Card, Color, Value, valueToAnsi}

class CardTests extends AnyWordSpec with Matchers {
    "Card" should {

        "have a string representation for all values" in {
            val values = Value.values
            val colors = Color.values
            for (value <- values) {
                for (color <- colors) {
                    val card = Card(value, color)
                    card.toString() shouldBe s"$value of $color"
                }
            }
        }

        "return the correct ANSI color code for Chester" in {
            valueToAnsi(Value.Chester) shouldBe Console.RESET
        }

        "return the correct ANSI color code for WizardKarte" in {
            valueToAnsi(Value.WizardKarte) shouldBe Console.RESET
        }

        "return the correct ANSI color code for Red" in {
            colorToAnsi(Color.Red) shouldBe Console.RED
        }

        "return the correct ANSI color code for Green" in {
            colorToAnsi(Color.Green) shouldBe Console.GREEN
        }

        "return the correct ANSI color code for Blue" in {
            colorToAnsi(Color.Blue) shouldBe Console.BLUE
        }

        "return the correct ANSI color code for Yellow" in {
            colorToAnsi(Color.Yellow) shouldBe Console.YELLOW
        }

        "show a card with value 10" in {
            val card = Card(Value.Ten, Color.Red)
            card.showcard() shouldBe "┌─────────┐\n" +
                "│ \u001B[31m\u001B[0m10      \u001B[0m│\n" +
                "│         │\n" +
                "│         │\n" +
                "│         │\n" +
                "│      \u001B[31m\u001B[0m10\u001B[0m │\n" +
                "└─────────┘"
        }



    }
}