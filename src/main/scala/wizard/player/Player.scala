package wizard.player

import wizard.cards.{Card, Hand}

case class Player(name: String) {
    // Hand-Objekt zur Verwaltung der Karten des Spielers
    var hand: Hand = Hand(List[Card]())
    // Karte in Konsole ausgeben
    def showHand(): Unit = {
        println(s"$name's hand:")
        if (hand.cards.isEmpty) {
            println("No cards in hand.")
        } else {
            val cardLines = hand.cards.map(_.showcard().split("\n"))
            for (i <- cardLines.head.indices) {
                println(cardLines.map(_(i)).mkString(" "))
            }
        }
    }
    def addHand(hand: Hand):Unit = {
        this.hand = hand
        this.hand
    }
    
}
