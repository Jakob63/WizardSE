package wizard.cards

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Card, Color, Value, valueToAnsi, colorToAnsi}
import wizard.aView.TextUI.showcard

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
        
        "dont change the console color for the other values" in {
            valueToAnsi(Value.One) shouldBe ""
        }

        "class card to String should return the value of the card" in {
            val card = Card(Value.One, Color.Red)
            card.toString() shouldBe "One of Red"
        }
        
        "show a card with value 10" in {
            val card = Card(Value.Ten, Color.Red)
            showcard(card) shouldBe "┌─────────┐\n" +
                s"│ ${Console.RED}10${Console.RESET}      │\n" +
                "│         │\n" +
                "│         │\n" +
                "│         │\n" +
                s"│      ${Console.RED}10${Console.RESET} │\n" +
                "└─────────┘"
        }
        // green card should be console green
        "return the correct ANSI color code for Green" in {
            colorToAnsi(Color.Green) shouldBe Console.GREEN
        }
        // blue card should be console blue
        "return the correct ANSI color code for Blue" in {
            colorToAnsi(Color.Blue) shouldBe Console.BLUE
        }
        // yellow card should be console yellow
        "return the correct ANSI color code for Yellow" in {
            colorToAnsi(Color.Yellow) shouldBe Console.YELLOW
        }
        "return the correct ANSI color code for Red" in {
            colorToAnsi(Color.Red) shouldBe Console.RED
        }



    }
}