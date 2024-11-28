// LastGameRound.scala
package wizard.pattern.template

import wizard.controller.RoundLogic
import wizard.model.rounds.Game
import wizard.model.cards.Dealer

class LastGameRound extends GameRound {
    override def setupRound(game: Game): Unit = {
        println("Setting up the last round without a trump card.")
        game.players.foreach { player =>
            player.roundPoints = 0
            player.roundTricks = 0
            player.roundBids = 0
        }
        // Reset the dealer index
        Dealer.index = 0
        // Deal the correct number of cards for the last round
        val cardsPerPlayer = 60 / game.players.length
        if (cardsPerPlayer * game.players.length > Dealer.allCards.length) {
            throw new IndexOutOfBoundsException("Not enough cards left in the deck.")
        }
        game.players.foreach { player =>
            val hand = Dealer.dealCards(cardsPerPlayer, None)
            player.addHand(hand)
        }
    }

    override def endRound(game: Game): Unit = {
        println("Ending the last round.")
        game.players.foreach(player => {
            player.points += player.roundPoints
            player.tricks += player.roundTricks
            player.bids += player.roundBids
        })
    }
}