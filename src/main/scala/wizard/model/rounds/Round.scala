package wizard.model.rounds

import wizard.model.cards.{Card, Color, Dealer, Value}
import wizard.model.player.Player

import scala.compiletime.uninitialized
import wizard.controller.RoundState
import wizard.aView.TextUI
import wizard.actionmanagement.Observable

class Round(players: List[Player]) extends Observable {
    var trump: Option[Color] = None
    var leadColor: Option[Color] = None
    var currentPlayerIndex = 0
    private var state: RoundState = _

    def setTrump(trump: Option[Color]): Unit = {
        this.trump = trump
    }

    def setState(state: RoundState): Unit = {
        this.state = state
    }

    def handleTrump(trumpCard: Card, players: List[Player]): Unit = {
        state.handleTrump(this, trumpCard, players)
    }

    def nextPlayer(): Player = {
        val player = players(currentPlayerIndex)
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length
        player
    }

    def isOver(): Boolean = {
        players.forall(player => player.hand.isEmpty)
    }

    def finalizeRound(): Unit = {
        players.foreach { player =>
            player.tricks += player.roundTricks
            player.bids += player.roundBids
            player.points += player.roundPoints
            player.roundTricks = 0
            player.roundBids = 0
            player.roundPoints = 0
        }
    }

    override def toString: String = {
        s"Trump: $trump, LeadColor: $leadColor, CurrentPlayerIndex: $currentPlayerIndex, Players: $players"
    }

}