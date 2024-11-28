// Game.scala
package wizard.model.rounds

import wizard.model.player.Player
import wizard.pattern.GameState
import wizard.pattern.template.{GameRound, LastGameRound}

case class Game(players: List[Player]) {

    var rounds = 20 // Ensure the game has 20 rounds
    var currentround = 0

    initializePlayers()

    private var state: GameState = _
    private var gameRound: GameRound = _
    
    def setState(newState: GameState): Unit = {
        state = newState
    }

    def startGame(): Unit = {
        state.startGame(this)
    }

    def playGame(): Unit = {
        state.playGame(this)
    }

    def endGame(): Unit = {
        state.endGame(this)
    }

    def playRound(): Unit = {
        if (currentround == rounds - 1) {
            gameRound = new LastGameRound
        }
        gameRound.playRound(this)
        currentround += 1
    }

    private def initializePlayers(): Unit = {
        players.foreach { player =>
            player.points = 0
            player.tricks = 0
            player.bids = 0
            player.roundPoints = 0
            player.roundBids = 0
            player.roundTricks = 0
        }
    }
}