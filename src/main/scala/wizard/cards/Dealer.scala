package wizard.cards

import wizard.player.Player

object Dealer {
    //erstelle eine liste mit allen karten eine karte besteht aus einer color und einem value
    val allCards: List[Card] = for {
        color <- Color.values.toList
        value <- Value.values.toList
    } yield Card(value, color)
    var index = 0

    // gib den string der obersten karte in einem println aus
    //println(allCards.head.toString)
    //schreibe eine methode die alle karten in eine zufällige reihenfolge bringt
    def shuffleCards(cards: List[Card]): List[Card] = {
        index = 0
        scala.util.Random.shuffle(cards)
    }
    // mische alle karten
    //val shuffledCards = shuffleCards(allCards)

    // deal cards to players but in each round the number of cards is different and return the rest of the crads
    def dealCards(cards_amount: Int = 1, players: List[Player]): List[Card] = {
        // Karten Mischen
        val shuffledCards = shuffleCards(allCards)
        // jedem spieler eine Karte auf seine Hand geben
        players.foreach { player =>
            val playerCards = shuffledCards.slice(index, index + cards_amount)
            player.hand = player.hand ++ playerCards
            index += cards_amount
        }
//        val playersCards = shuffledCards.grouped(cards_amount).toList
//        println(playersCards)
        val rest = shuffledCards.drop(players.length * cards_amount)
        // oberste karte aus dem rest in konsole ausgeben
        if (rest.nonEmpty) {
            println(rest.head.toString)
        } else {
            println("No cards left in the rest.")
        }
        rest
    }

}
