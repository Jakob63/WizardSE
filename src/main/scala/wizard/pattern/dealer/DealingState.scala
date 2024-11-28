package wizard.pattern.dealer

import wizard.model.cards.{Card, Dealer, Hand}
import wizard.pattern.dealer.DealerState

import scala.collection.mutable.ListBuffer

object DealingState extends DealerState {
    override def shuffleCards(dealer: Dealer.type): Boolean = {
        throw new IllegalStateException("Cannot shuffle cards while dealing")
    }

    // .type is a singleton type, which means that it can only have one instance
    // referenziert auf Dealer object selbst und keine Instanz
    override def dealCards(dealer: Dealer.type, cards_amount: Int, excludeCard: Option[Card]): Hand = {
        val listbuffer = ListBuffer[Card]()
        for (i <- 1 to cards_amount) {
            var card = dealer.allCards(dealer.index)
            while (excludeCard.contains(card)) {
                dealer.index += 1
                card = dealer.allCards(dealer.index)
            }
            listbuffer.addOne(card)
            dealer.index += 1
        }
        dealer.setState(IdleState)
        Hand(listbuffer.toList)
    }
}