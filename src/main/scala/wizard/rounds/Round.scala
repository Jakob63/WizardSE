// Round.scala
package wizard.rounds

import wizard.player.Player
import wizard.cards.Color
import wizard.cards.Value
import wizard.cards.Dealer
import wizard.rounds.Game

class Round(players: List[Player]) {
    // Aktueller Trumpf
    var trump: Color = _
    var leadColor: Option[Color] = None
    var currentPlayerIndex = 0
    
    // Methode zum Setzen des Trumpfs
    def setTrump(trump: Color): Unit = {
        this.trump = trump
    }

    def nextPlayer(): Player = {
        val player = players(currentPlayerIndex)
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length
        // next player
        player
    }

    // is game over
    def isOver(): Boolean = {
        players.forall(player => player.hand.isEmpty)
    }

    // finalize round
    def finalizeRound(): Unit = {
        players.foreach(player => player.points += player.roundPoints)
        players.foreach(player => player.tricks += player.roundTricks)
        players.foreach(player => player.bids += player.roundBids)
        players.foreach(player => player.roundPoints = 0)
        players.foreach(player => player.roundTricks = 0)
        players.foreach(player => player.roundBids = 0)
    }

    
}