package wizard.model.model_TUI.rounds

import wizard.aView.aView_TUI.TextUI

import scala.compiletime.uninitialized
import wizard.actionmanagement.Observable
import wizard.controller.controller_TUI.RoundState
import wizard.model.model_TUI.cards.{Card, Color, Dealer, Value}
import wizard.model.model_TUI.player.Player

class Round(players: List[Player]) extends Observable {
    // Aktueller Trumpf
    var trump: Option[Color] = None
    var leadColor: Option[Color] = None
    var currentPlayerIndex = 0
    private var state: RoundState = _

    add(TextUI) // Added den Observer

    def setTrump(trump: Option[Color]): Unit = {
        this.trump = trump
    }

    def setState(state: RoundState): Unit = {
        this.state = state
    }

    def handleTrump(trumpCard: Card, players: List[Player]): Unit = {
        state.handleTrump(this, trumpCard, players)
    }

//    def determineTrump(players: List[Player]): Unit = {
//        for (player <- players) {
//            val trumpCard = player.hand.cards.find(_.value == Value.WizardKarte)
//            if (trumpCard.isEmpty) {
//                val input = TextUI.update("which trump", player).asInstanceOf[String]
//                setTrump(Some(Color.valueOf(input)))
//                notifyObservers("print trump card", Card(Value.valueOf(input), Color.valueOf(input)))
//                return
//            }
//        }
//        setTrump(None)
//    }

    def nextPlayer(): Player = {
        val player = players(currentPlayerIndex)
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length
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

    override def toString: String = {
        s"Trump: $trump, LeadColor: $leadColor, CurrentPlayerIndex: $currentPlayerIndex, Players: $players"
    }

}