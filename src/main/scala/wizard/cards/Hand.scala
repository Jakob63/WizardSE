package wizard.cards

case class Hand(cards: List[Card]) {
    // methode zum removen von Karten
    def removeCard(card: Card): Hand = {
        Hand(cards.filterNot(_ == card))
    }
    // methode zum suit checken
    def hasColor(suit: Color): Boolean = {
        cards.exists(_.color == suit)
    }
    // methode zum value checken
    def hasValue(value: Value): Boolean = {
        cards.exists(_.value == value)
    }
    // methode zum checken ob trumpcard
    def hasTrumpColor(trump: Color): Boolean = {
        cards.exists(_.color == trump)
    }
    
}
