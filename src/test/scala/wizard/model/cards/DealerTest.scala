package wizard.model.cards

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import org.scalatest.BeforeAndAfterEach

class DealerTest extends AnyWordSpec with Matchers with TimeLimitedTests with BeforeAndAfterEach {

  val timeLimit = 30.seconds

  override def beforeEach(): Unit = {
    Dealer.index = 0
  }

  "The Dealer" should {
    "have all 60 cards initially (15 values * 4 colors)" in {
      Dealer.allCards.size should be (60)
    }

    "allow shuffling cards when WIZARD_INTERACTIVE is set" in {
      val beforeShuffle = Dealer.allCards
      sys.props("WIZARD_INTERACTIVE") = "true"
      try {
        Dealer.shuffleCards() should be (true)
        Dealer.allCards should not be (beforeShuffle)
        Dealer.index should be (0)
      } finally {
        sys.props.remove("WIZARD_INTERACTIVE")
      }
    }

    "not shuffle cards when WIZARD_INTERACTIVE is false" in {
      sys.props("WIZARD_INTERACTIVE") = "false"
      
      val originalAllCards = Dealer.allCards
      Dealer.allCards = (for {
            color <- Color.values.toList
            value <- Value.values.toList
        } yield Card(value, color))

      val beforeShuffle = Dealer.allCards
      
      try {
        Dealer.shuffleCards() should be (true)
        if (System.console() == null) {
          Dealer.allCards should be (beforeShuffle)
        }
      } finally {
        sys.props.remove("WIZARD_INTERACTIVE")
        Dealer.allCards = originalAllCards
      }
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
