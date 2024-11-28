package wizard.pattern

import wizard.model.rounds.Game

trait GameState {
    def startGame(game: Game): Unit
    def playGame(game: Game): Unit
    def endGame(game: Game): Unit
}