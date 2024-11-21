package wizard.controller

import wizard.model.player.Player
import wizard.model.rounds.{Game, Round}
import wizard.aView.TextUI

import wizard.actionmanagement.{Observable, Observer}

object GameLogic extends Observable {
    add(TextUI)
    def validGame(number: Int): Boolean = {
        number >= 3 && number <= 6
    }

    def playGame(game: Game, players: List[Player]): Unit = {
        for (i <- 1 to game.rounds) { // i = 1, 2, 3, ..., rounds
            game.currentround = i
            val round = new Round(players)
            RoundLogic.playRound(game.currentround, players)
        }
    }

    // game is over if all rounds are played
    def isOver(game: Game): Boolean = {
        game.rounds == 0
    }
}
