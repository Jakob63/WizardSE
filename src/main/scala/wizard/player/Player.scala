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











    // Punkte des Spielers
    var points: Int = 0
    // Anzahl der Stiche des Spielers
    var tricks: Int = 0
    // Anzahl der Gebote des Spielers
    var bids: Int = 0
    // Punkte des Spielers in der Runde
    var roundPoints: Int = 0
    // Anzahl der Stiche des Spielers in der Runde
    var roundTricks: Int = 0
    // Anzahl der Gebote des Spielers in der Runde
    var roundBids: Int = 0

    // Methode zum Bieten
    def bid(): Int = {
        println(s"$name, how many tricks do you bid?")
        val bid = scala.io.StdIn.readInt()
        // der letzte SPieler darf nicht x bieten wenn die summer der gebote der anderen spieler y ist. Es gilt runde - y= x
        // letzten spieler abfangen: anzahl spieler
        roundBids = bid
        bid
    }
    // Methode zum Spielen einer Karte
    def playCard(leadSuit: Color, trump: Color): Card = {
        println(s"$name, which card do you want to play?")
        val card = scala.io.StdIn.readLine()
        val cardToPlay = hand.find(_.showcard() == card)
        cardToPlay match {
            case Some(card) =>
                if (card.color != leadSuit && hand.exists(_.color == leadSuit) && card.value != "Wizard" && card.value != "Fool") {
                    println(s"You must follow the lead suit $leadSuit.")
                    playCard(leadSuit, trump)
                } else {
                    hand = hand.filterNot(_ == card)
                    card
                }
            case None =>
                println("Invalid card. Please try again.")
                playCard(leadSuit, trump)
        }
    }

    // Methode zum Punkte hinzufügen
    def addPoints(points: Int): Unit = {
        this.points += points
    }
    // Methode zum Stiche hinzufügen
    def addTricks(tricks: Int): Unit = {
        this.tricks += tricks
    }
    // Methode zum Gebote hinzufügen
    def addBids(bids: Int): Unit = {
        this.bids += bids
    }


}
