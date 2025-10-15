package wizard.controller

import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.model.player.Player
import wizard.aView.TextUI

import wizard.actionmanagement.{Observable, Observer}

object PlayerLogic extends Observable {
    
    // Method to play a card
    def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int, player: Player): Card = {
        notifyObservers("which card", player)
        val input = scala.io.StdIn.readLine()
        val cardIndex = try {
            input.toInt
        } catch {
            case _: NumberFormatException => -1
        }
        if (cardIndex < 1 || cardIndex > player.hand.cards.length) {
            notifyObservers("invalid card")
            return playCard(leadColor, trump, currentPlayerIndex, player)
        }
        val cardToPlay = player.hand.cards(cardIndex - 1)
        if (leadColor != null && cardToPlay.color != leadColor && player.hand.hasColor(leadColor) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester) {
            notifyObservers("follow lead", leadColor)
            return playCard(leadColor, trump, currentPlayerIndex, player)
        } else {
            player.hand = player.hand.removeCard(cardToPlay)
            cardToPlay
        }
    }

    // Method to bid
    def bid(player: Player): Int = {
        notifyObservers("which bid", player)
        val input = scala.io.StdIn.readLine()
        if (input == "" || input.trim.isEmpty || !input.forall(_.isDigit)) {
            notifyObservers("invalid input, bid again")
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
