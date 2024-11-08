// Game.scala
package wizard.rounds

import wizard.player.Player

case class Game(players: List[Player], rounds: Int) {
    val playerCount = players.length
    if (playerCount < 3 || playerCount > 6) {
        throw new IllegalArgumentException("Invalid Player Count: The number of players must be between 3 and 6.")
    }
    val rounds = 60 / playerCount
    var currentround = 0
    var currentPlayerIndex = 0

    players.foreach(player => player.hand = List())
    players.foreach(player => player.points = 0)
    players.foreach(player => player.tricks = 0)
    players.foreach(player => player.bids = 0)
    players.foreach(player => player.roundPoints = 0)
    players.foreach(player => player.roundBids = 0)
    players.foreach(player => player.roundTricks = 0)

    def playGame(): Unit = {
        for (i <- 1 to rounds) { // i = 1, 2, 3, ..., rounds
            currentround = i
            val round = new Round(players)
            round.playRound(currentround)
        }
    }

    def nextPlayer(): Player = {
        val player = players(currentPlayerIndex)
        currentPlayerIndex = (currentPlayerIndex + 1) % playerCount
        // next player
        player
    }

    def createGame(): Game = {
        new Game(players, rounds)
    }

    def createRound(): Round = {
        new Round(players)
    }

    // game is over if all rounds are played
    def isOver(): Boolean = {
        rounds == 0
    }
}