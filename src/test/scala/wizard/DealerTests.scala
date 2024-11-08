package wizard.cards

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec

class DealerTests extends AnyWordSpec with Matchers {
    "Dealer" should {
        
        "have a list of all cards" in {
            Dealer.allCards should not be empty
        }
        
        "have a list of 60 cards" in {
            Dealer.allCards should have size 60
        }
        
        "have 15 yellow cards" in {
            Dealer.allCards.filter(_.color == Color.Yellow) should have size 15
        }
        
        "have 15 red cards" in {
            Dealer.allCards.filter(_.color == Color.Red) should have size 15
        }
        
        "have 15 green cards" in {
            Dealer.allCards.filter(_.color == Color.Green) should have size 15
        }
        
        "have 15 blue cards" in {
            Dealer.allCards.filter(_.color == Color.Blue) should have size 15
        }
    }
}