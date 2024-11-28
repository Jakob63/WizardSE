package wizard.pattern.template

import wizard.model.rounds.Game

abstract class GameRound {
    def playRound(game: Game): Unit = {
        setupRound(game)
        endRound(game)
    }

    def setupRound(game: Game): Unit
    def endRound(game: Game): Unit
}