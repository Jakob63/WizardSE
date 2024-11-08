package wizard.cards

import scala.collection.mutable.ListBuffer

object Dealer {
    //erstelle eine liste mit allen karten eine karte besteht aus einer color und einem value
    var allCards: List[Card] = {
        val buffer = ListBuffer[Card]()
        for {
            color <- Color.values.toList
            value <- Value.values.toList
        } buffer += Card(value, color)
        buffer.toList
    }
    var index = 0

    // gib den string der obersten karte in einem println aus
    //println(allCards.head.toString)
    //schreibe eine methode die alle karten in eine zufällige reihenfolge bringt
    def shuffleCards(): Boolean = {
        index = 0
        allCards = scala.util.Random.shuffle(allCards)
        true
    }
    // mische alle karten
    //val shuffledCards = shuffleCards(allCards)

    // Methode zum Austeilen der Karten an die Spieler
    def dealCards(cards_amount: Int): Hand = {
        val listbuffer = ListBuffer[Card]()
        // Karten mischen
        if (index + 1 > 59) {
            throw new IndexOutOfBoundsException("No cards left in the deck.")
        }
        for (i <- 1 to cards_amount) {
            listbuffer.addOne(allCards(index))
            index += 1
        }
        listbuffer.toList
        Hand(listbuffer.toList)
    }

    def printCardAtIndex(index: Int): Unit = {
        if (index >= 0 && index < allCards.length) {
            println(allCards(index).showcard())
        } else {
            println(s"Index $index is out of bounds.")
        }
    }

}
