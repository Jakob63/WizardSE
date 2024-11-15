package wizard.Controller.control

import wizard.Model.cards.{Card, Color, Hand, Value}
import wizard.Model.player.Player

object PlayerLogic {
    // Method to play a card
    def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int, player: Player): Card = {
        println(s"${player.name}, which card do you want to play?")
        val cardIndex = scala.io.StdIn.readInt()
        if (cardIndex < 1 || cardIndex > player.hand.cards.length) {
            println("Invalid card. Please try again.")
            return playCard(leadColor, trump, currentPlayerIndex, player)
        }
        val cardToPlay = player.hand.cards(cardIndex - 1)
        if (leadColor != null && cardToPlay.color != leadColor && player.hand.hasColor(leadColor) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester) {
            println(s"You must follow the lead suit $leadColor.")
            return playCard(leadColor, trump, currentPlayerIndex, player)
        } else {
            player.hand = player.hand.removeCard(cardToPlay)
            cardToPlay
        }
    }

    // Method to bid
    def bid(player: Player): Int = {
        println(s"${player.name}, how many tricks do you bid?")
        val input = scala.io.StdIn.readLine()
        if (input == null || input.trim.isEmpty || !input.forall(_.isDigit)) {
            println("Invalid input. Please enter a valid number.")
            return bid(player)
        }
        val playersbid = input.toInt
        player.roundBids = playersbid
        playersbid
    }

    // Method to add points
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
