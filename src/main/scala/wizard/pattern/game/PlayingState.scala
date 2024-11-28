package wizard.pattern

import wizard.model.rounds.Game

object PlayingState extends GameState {
    override def startGame(game: Game): Unit = {
        throw new IllegalStateException("Cannot start game while playing")
    }

    override def playGame(game: Game): Unit = {
        println("Game is already playing.")
    }

    override def endGame(game: Game): Unit = {
        println("Game is now ending.")
        game.setState(EndingState)
    }
}