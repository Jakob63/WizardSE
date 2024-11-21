package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Color, Dealer, Value}
import wizard.model.player.Player
import wizard.aView.TextUI.{printCardAtIndex, showcard}

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

        "have 4 chester cards" in {
            Dealer.allCards.filter(_.value == Value.Chester) should have size 4
        }

        "have 4 wizard cards" in {
            Dealer.allCards.filter(_.value == Value.WizardKarte) should have size 4
        }

        "have 4 one cards" in {
            Dealer.allCards.filter(_.value == Value.One) should have size 4
        }
        "correct shuffleCards" in {
            val cards = Dealer.allCards
            Dealer.shuffleCards()
            Dealer.allCards should not be cards
        }
        
        "correct dealCards with 0 cards" in {
            val hand = Dealer.dealCards(0)
            hand.cards should have size 0
        }

        "ensure trump card is not in any player's hand" in {
            val players = List(Player("Player1"), Player("Player2"), Player("Player3"))
            val trumpCard = Dealer.allCards.head // Beispiel-Trumpfkarte
            Dealer.shuffleCards()
            players.foreach { player =>
                val hand = Dealer.dealCards(10, Some(trumpCard))
                player.addHand(hand)
                hand.cards should not contain trumpCard
            }
        }
        "correct dealCards" in {
            val hand = Dealer.dealCards(10)
            hand.cards should have size 10
        }

        "correctly print card at index" in {
            // hole die Karte an Index 0
            val card = Dealer.allCards(0)
            // hole die Karte an Index 0 mit der Methode
            val printedCard = printCardAtIndex(0)
            // überprüfe ob die Karte an Index 0 die gleiche ist
            printedCard shouldBe showcard(card)
        }

        "assign the correct card at the given index" in {
            // Initialize the dealer and shuffle the cards
            Dealer.shuffleCards()
            // Get the card at the current index
            val expectedCard = Dealer.allCards(Dealer.index)
            // Deal one card
            val hand = Dealer.dealCards(1)
            // The dealt card should be the same as the card at the current index
            hand.cards.head shouldBe expectedCard
        }
    }
}