package wizard.controller

import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Player
import wizard.aView.TextUI
import wizard.actionmanagement.{Observable, Observer}

object PlayerLogic extends Observable {
    add(TextUI)

    def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int, player: Player): Card = {
        notifyObservers("which card", player)
        val cardToPlay = player.playCard(leadColor, trump, currentPlayerIndex)
        if (leadColor != null && cardToPlay.color != leadColor && player.hand.hasColor(leadColor) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester) {
            notifyObservers("follow lead", leadColor)
            return playCard(leadColor, trump, currentPlayerIndex, player)
        } else if (leadColor != null && cardToPlay.color != leadColor && !player.hand.hasColor(leadColor) && cardToPlay.color != trump && player.hand.hasColor(trump) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester) {
            notifyObservers("follow trump", trump)
            return playCard(leadColor, trump, currentPlayerIndex, player)
        } else {
            player.hand = player.hand.removeCard(cardToPlay)
            cardToPlay
        }
    }

    def bid(player: Player): Int = {
        notifyObservers("which bid", player)
        val playersbid = player.bid()
        player.roundBids = playersbid
        playersbid
    }

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