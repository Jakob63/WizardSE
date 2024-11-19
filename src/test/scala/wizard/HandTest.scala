package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Card, Color, Hand, Value}

class HandTest extends AnyWordSpec with Matchers {
    "Hand" should {
        
        "correct addCards" in {
            val hand = Hand(List())
            hand.addCards(List())
            hand.cards shouldBe List()
        }
        
        "correct removeCard" in {
            val hand = Hand(List())
            hand.removeCard(Card(Value.One, Color.Red))
            hand.cards shouldBe List()
        }
        
        "correct hasColor" in {
            val hand = Hand(List(Card(Value.One, Color.Red)))
            hand.hasColor(Color.Red) shouldBe true
        }
        
        "correct hasValue" in {
            val hand = Hand(List(Card(Value.One, Color.Red)))
            hand.hasValue(Value.One) shouldBe true
        }
        
        "correct hasTrumpColor" in {
            val hand = Hand(List(Card(Value.One, Color.Red)))
            hand.hasTrumpColor(Color.Red) shouldBe true
        }
        
        "correct isEmpty" in {
            val hand = Hand(List())
            hand.isEmpty shouldBe true
        }
        
        "correct getCard" in {
            val hand = Hand(List(Card(Value.One, Color.Red)))
            hand.getCard(0) shouldBe Card(Value.One, Color.Red)
        }
    }
}
