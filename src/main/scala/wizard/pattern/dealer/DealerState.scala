package wizard.pattern.dealer

import wizard.model.cards.{Card, Dealer, Hand}

trait DealerState {
    def shuffleCards(dealer: Dealer.type): Boolean
    def dealCards(dealer: Dealer.type, cards_amount: Int, excludeCard: Option[Card]): Hand
}