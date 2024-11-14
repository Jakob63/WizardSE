package wizard.controll

import wizard.cards.{Card, Color, Hand, Value}
import wizard.player.Player

object PlayerLogic {
    // Methode zum Spielen einer Karte
    def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int, player: Player): Card = {
        println(s"${player.name}, which card do you want to play?")
        val cardIndex = scala.io.StdIn.readInt()
        // card is valid
        if (cardIndex < 1 || cardIndex > player.hand.cards.length) {
            println("Invalid card. Please try again.")
            return playCard(leadColor, trump, currentPlayerIndex, player)
        }
        //val cardtoplay = card.split(" ")
        val cardToPlay = player.hand.cards(cardIndex - 1)
        //val cardToPlay = player.hand.cards.find(_.toString.equals(card))

        if (leadColor != null && cardToPlay.color != leadColor && player.hand.hasColor(leadColor) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester) {
            println(s"You must follow the lead suit $leadColor.")
            return playCard(leadColor, trump, currentPlayerIndex, player)
        } else {
            player.hand = player.hand.removeCard(cardToPlay)
            cardToPlay
        }
    }

    // Methode zum Bieten
    def bid(player: Player): Int = {
        println(s"${player.name}, how many tricks do you bid?")
        val bid = scala.io.StdIn.readInt()
        // der letzte SPieler darf nicht x bieten wenn die summer der gebote der anderen spieler y ist. Es gilt runde - y= x
        // letzten spieler abfangen: anzahl spieler
        player.roundBids = bid
        bid
    }

    // Methode zum Punkte hinzufügen
    def addPoints(player: Player): Unit = {
        if (player.roundBids == player.roundTricks) {
            player.points += 20 + 10 * player.roundBids
        } else {
            player.points -= 10 * Math.abs(player.roundBids - player.roundTricks)
        }
    }

    def calculatePoints(player: Player): Int = {
        val points = player.roundPoints
        val bids = player.roundBids
        val tricks = player.roundTricks
        if (bids == tricks) {
            points + 20 + tricks * 10
        } else {
            points - Math.abs(bids - tricks) * 10
        }
    }

}
