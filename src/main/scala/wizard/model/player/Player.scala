package wizard.model.player

import wizard.model.cards.{Card, Color, Hand}
import wizard.actionmanagement.{Observable, Observer}
import wizard.aView.TextUI

abstract case class Player(var name: String) extends Observable {
    
    //add(TextUI)
    
    // Hand-Objekt zur Verwaltung der Karten des Spielers
    var hand: Hand = Hand(List[Card]())

    // Punkte des Spielers
    var points: Int = 0

    // gemachte tricks des Spielers
    var tricks: Int = 0

    // Anzahl der angesagten tricks des Spielers
    var bids: Int = 0

    // Punkte des Spielers in der aktuellen Runde
    var roundPoints: Int = 0

    // Anzahl der angesagten tricks des Spielers in der Runde
    var roundBids: Int = 0

    // gemachte tricks des Spielers in der Runde
    var roundTricks: Int = 0

    // Methode zum Hinzufügen einer Hand
    def addHand(hand: Hand): Unit = {
        this.hand = hand
    }

    // Methode zum Hinzufügen von Tricks
    def addTricks(tricks: Int): Unit = {
        this.tricks += tricks
    }
    def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card
    def bid(): Int
}
