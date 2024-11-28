package wizard.pattern.dealer

import wizard.model.cards.{Card, Dealer, Hand}
import wizard.pattern.dealer.{DealerState, DealingState}

object IdleState extends DealerState {
    override def shuffleCards(dealer: Dealer.type): Boolean = {
        dealer.setState(ShufflingState)
        dealer.shuffleCards()
    }

    override def dealCards(dealer: Dealer.type, cards_amount: Int, excludeCard: Option[Card]): Hand = {
        dealer.setState(DealingState)
        dealer.dealCards(cards_amount, excludeCard)
    }
}