package wizard.pattern

import wizard.model.rounds.Game

object StartingState extends GameState {
    override def startGame(game: Game): Unit = {
        println("Game is already starting.")
    }

    override def playGame(game: Game): Unit = {
        game.setState(PlayingState)
    }

    override def endGame(game: Game): Unit = {
        throw new IllegalStateException("Cannot end game while starting")
    }
}