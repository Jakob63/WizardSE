package wizard.controller

import wizard.aView.TextUI
import wizard.actionmanagement.{Observable, Observer}
import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round

trait RoundState extends Observable {
    def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit
}

class NormalCardState extends RoundState {
    override def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit = {
        round.setTrump(trumpCard.color)
        round.notifyObservers("print trump card", trumpCard)
    }
}

class ChesterCardState extends RoundState {
    override def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit = {
        round.setTrump(null)
        round.notifyObservers("print trump card", trumpCard)
    }
}

class WizardCardState extends RoundState {
    override def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit = {
        round.setTrump(null)
        round.notifyObservers("print trump card", trumpCard)
        def determineTrump(players: List[Player]): Color = {
            for (player <- players) {
                val trumpCard = player.hand.cards.find(_.value == Value.WizardKarte)
                if (trumpCard.isEmpty) {
                    val input = TextUI.update("which trump", player).asInstanceOf[String]
                    return Color.valueOf(input)
                }
            }
            null
        }
    }
}