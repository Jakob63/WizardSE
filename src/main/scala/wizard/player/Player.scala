package wizard.player

import wizard.cards.{Card, Hand, Color, Value}
import wizard.rounds.Round
import wizard.rounds.Game
import wizard.textUI.TextUI

case class Player(name: String) {
    // Hand-Objekt zur Verwaltung der Karten des Spielers
    var hand: Hand = Hand(List[Card]())
    // Karte in Konsole ausgeben
    def addHand(hand: Hand):Unit = {
        this.hand = hand
        this.hand
    }
    // Punkte des Spielers
    var points: Int = 0
    // gemachte tricks des Spielers
    var tricks: Int = 0
    // Anzahl der angesagten tricks des Spielers
    var bids: Int = 0
    // Anzahl der angesagten tricks des Spielers in der Runde
    var roundBids: Int = 0
    // gemachte tricks des Spielers in der Runde
    var roundTricks: Int = 0
    // Punkte des Spielers in der Runde
    var roundPoints: Int = 0

    // Methode zum Stiche hinzufügen TODO: notwendig aber tricks nur pro runde und bids auch nur pro runde
    def addTricks(tricks: Int): Unit = {
        this.tricks += tricks
    }
//
//    // Methode zum Gebote hinzufügen
//    def addBids(bids: Int): Unit = {
//        this.bids += bids
//    }
    
}
