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
        determineTrump(round, players)
    }

    private def determineTrump(round: Round, players: List[Player]): Unit = {
        val nextPlayer = players(round.currentPlayerIndex)
        val colorOptions = List(Color.Red, Color.Yellow, Color.Green, Color.Blue)
        val colorCards = colorOptions.map(color => Card(Value.One, color))

        // Print color options
        TextUI.printColorOptions(colorCards)

        val input = TextUI.update("which trump", nextPlayer).asInstanceOf[String]
        val chosenColorIndex = input.toInt - 1
        val chosenColor = colorOptions(chosenColorIndex)

        round.setTrump(chosenColor)
        round.notifyObservers("print trump card", Card(Value.One, chosenColor))
    }
}