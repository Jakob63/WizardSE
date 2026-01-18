package wizard.controller

import wizard.aView.TextUI
import wizard.actionmanagement.{Observable, Observer}
import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round
import wizard.controller.GameLogic

trait RoundState extends Observable {
    
    
    
    def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit
}

class NormalCardState extends RoundState {
    override def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit = {
        round.setTrump(Some(trumpCard.color))
        round.notifyObservers("print trump card", trumpCard)
    }
}

class ChesterCardState extends RoundState {
    override def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit = {
        round.setTrump(None)
        round.notifyObservers("print trump card", trumpCard)
    }
}

class WizardCardState extends RoundState {
    override def handleTrump(round: Round, trumpCard: Card, players: List[Player]): Unit = {
        round.setTrump(None)
        round.notifyObservers("print trump card", trumpCard)
        determineTrump(round, players)
    }

    private def determineTrump(round: Round, players: List[Player]): Unit = {
        val nextPlayer = players(round.currentPlayerIndex)
        val colorOptions = List(Color.Red, Color.Green, Color.Blue, Color.Yellow)
        val colorCards = colorOptions.map(color => Card(Value.One, color))

        round.notifyObservers("which trump", nextPlayer)
        val inputStr = wizard.actionmanagement.InputRouter.readLine().trim
        
        wizard.actionmanagement.Debug.log(s"WizardCardState.determineTrump -> inputStr: '$inputStr'")
        val chosenIdx = scala.util.Try(inputStr.toInt).toOption.getOrElse(1) - 1
        val chosenColor = colorOptions.lift(chosenIdx).getOrElse(Color.Red)

        round.setTrump(Some(chosenColor))
        round.notifyObservers("print trump card", Card(Value.One, chosenColor))
    }
}