package wizard.player

import wizard.cards.Card

case class Player(name: String) {
    // Liste an Karten die der Spieler auf der Hand hat
    var hand: List[Card] = List()
    // karte in konsole ausgeben
    def showHand(): Unit = {
        println(s"$name's hand:")
        if (hand.isEmpty) {
            println("No cards in hand.")
        } else {
            hand.foreach(card => println(card.showcard()))
        }
    }
    
}
