package wizard.model.cards

import scala.collection.mutable.ListBuffer
import wizard.actionmanagement.Observable

object Dealer extends Observable {

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
    def dealCards(cards_amount: Int, excludeCard: Option[Card] = None): Hand = {
        //shuffleCards()
        val listbuffer = ListBuffer[Card]()
//        if (index + 1 > 59) {
//            throw new IndexOutOfBoundsException("No cards left in the deck.")
//        }
        for (i <- 1 to cards_amount) {
            var card = allCards(index)
            while (excludeCard.contains(card)) {
                index += 1
                card = allCards(index)
            }
            listbuffer.addOne(card)
            index += 1
        }
        Hand(listbuffer.toList)
    }
}
