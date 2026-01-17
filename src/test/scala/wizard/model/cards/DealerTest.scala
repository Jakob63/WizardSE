package wizard.model.cards

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DealerTest extends AnyWordSpec with Matchers {

  "The Dealer" should {
    "have all 60 cards initially (15 values * 4 colors)" in {
      Dealer.allCards.size should be (60)
    }

    "allow shuffling cards" in {
      val beforeShuffle = Dealer.allCards
      Dealer.shuffleCards() should be (true)
      Dealer.index should be (0)
    }

    "deal correct amount of cards" in {
      Dealer.index = 0
      val hand = Dealer.dealCards(5)
      hand.cards.size should be (5)
      Dealer.index should be (5)
    }

    "exclude a specific card when dealing" in {
      Dealer.index = 0
      val cardToExclude = Dealer.allCards(0)
      val hand = Dealer.dealCards(1, Some(cardToExclude))
      
      hand.cards should not contain (cardToExclude)
      hand.cards.size should be (1)
      Dealer.index should be (2)
    }

    "handle index correctly across multiple deals" in {
      Dealer.index = 0
      Dealer.dealCards(3)
      Dealer.index should be (3)
      Dealer.dealCards(2)
      Dealer.index should be (5)
    }
  }
}
