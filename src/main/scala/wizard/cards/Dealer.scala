package wizard.cards

object Dealer {
    //erstelle eine liste mit allen karten eine karte besteht aus einer color und einem value
    val allCards: List[Card] = for {
        color <- Color.values.toList
        value <- Value.values.toList
    } yield Card(value, color)
    // gib den string der obersten karte in einem println aus
    //println(allCards.head.toString)
    //schreibe eine methode die alle karten in eine zufällige reihenfolge bringt
    def shuffleCards(cards: List[Card]): List[Card] = {
        scala.util.Random.shuffle(cards)
    }
    // mische alle karten
    val shuffledCards = shuffleCards(allCards)

    // deal cards to players but in each round the number of cards is different and return the rest of the crads
    def dealCards(players: Int, cards: List[Card]): (List[Card], List[Card]) = {
        val numCards = cards.length / players
        val (dealt, rest) = cards.splitAt(numCards * players)
        (dealt, rest)
    }





}
