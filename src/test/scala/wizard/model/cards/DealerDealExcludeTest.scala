package wizard.model.cards

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DealerDealExcludeTest extends AnyFunSuite with Matchers {
  test("Dealer has 60 cards initially and dealCards returns requested amount") {
    // 15 values * 4 colors = 60
    Dealer.index = 0
    Dealer.allCards.size should be (15 * Color.values.size)
    val hand = Dealer.dealCards(5, None)
    hand.cards.length should be (5)
  }

  test("dealCards excludes specified card") {
    Dealer.index = 0
    val cardToExclude = Dealer.allCards.head
    val hand = Dealer.dealCards(1, Some(cardToExclude))
    // ensure the dealt card is not the excluded one
    hand.cards.head should not be (cardToExclude)
  }
}

