package wizard.model.cards

case class Hand(cards: List[Card]) {
    // Methode zum Hinzufügen von Karten
    def addCards(newCards: List[Card]): Hand = {
        Hand(cards ++ newCards)
    }
    // methode zum removen von Karten
    def removeCard(card: Card): Hand = {
        Hand(cards.filterNot(_ == card))
    }
    // methode zum suit checken — Wizards/Chester haben keine Farbe im Sinne der Regeln
    def hasColor(color: Color): Boolean = {
        cards.exists(c => c.color == color && c.value != Value.WizardKarte && c.value != Value.Chester)
    }
    // methode zum value checken
    def hasValue(value: Value): Boolean = {
        cards.exists(_.value == value)
    }
    // methode zum checken ob trumpcard — Wizards/Chester zählen nicht als Trumpf-Farbe
    def hasTrumpColor(trump: Color): Boolean = {
        cards.exists(c => c.color == trump && c.value != Value.WizardKarte && c.value != Value.Chester)
    }
    // leere hand
    def isEmpty: Boolean = {
        cards.isEmpty
    }
    // get card by index
    def getCard(index: Int): Card = {
        cards(index)
    }

}
