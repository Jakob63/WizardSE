package wizard.model.cards

case class Hand(cards: List[Card]) {
    def addCards(newCards: List[Card]): Hand = {
        Hand(cards ++ newCards)
    }
    def removeCard(card: Card): Hand = {
        Hand(cards.filterNot(_ == card))
    }
    def hasColor(color: Color): Boolean = {
        cards.exists(c => c.color == color && c.value != Value.WizardKarte && c.value != Value.Chester)
    }
    def hasValue(value: Value): Boolean = {
        cards.exists(_.value == value)
    }
    def hasTrumpColor(trump: Color): Boolean = {
        cards.exists(c => c.color == trump && c.value != Value.WizardKarte && c.value != Value.Chester)
    }
    def isEmpty: Boolean = {
        cards.isEmpty
    }
    def getCard(index: Int): Card = {
        cards(index)
    }

}
