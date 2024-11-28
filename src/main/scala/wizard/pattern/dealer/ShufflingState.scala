package wizard.pattern.dealer

import wizard.model.cards.{Card, Dealer, Hand}
import wizard.pattern.dealer.{DealerState, IdleState}

object ShufflingState extends DealerState {
    override def shuffleCards(dealer: Dealer.type): Boolean = {
        dealer.index = 0
        dealer.allCards = scala.util.Random.shuffle(dealer.allCards)
        dealer.setState(IdleState)
        true
    }

    override def dealCards(dealer: Dealer.type, cards_amount: Int, excludeCard: Option[Card]): Hand = {
        throw new IllegalStateException("Cannot deal cards while shuffling")
    }
}