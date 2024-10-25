package wizard.cards

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec

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
    }
}