package wizard.pattern

import wizard.model.rounds.Game

object EndingState extends GameState {
    override def startGame(game: Game): Unit = {
        throw new IllegalStateException("Cannot start game while ending")
    }

    override def playGame(game: Game): Unit = {
        throw new IllegalStateException("Cannot play game while ending")
    }

    override def endGame(game: Game): Unit = {
        println("Game is already ending.")
    }
}