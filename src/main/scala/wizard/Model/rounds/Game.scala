// Game.scala
package wizard.model.rounds

import wizard.model.player.Player

case class Game(players: List[Player]) {

    var rounds = 60 / players.length
    var currentround = 0
    
    players.foreach(player => player.points = 0)
    players.foreach(player => player.tricks = 0)
    players.foreach(player => player.bids = 0)
    players.foreach(player => player.roundPoints = 0)
    players.foreach(player => player.roundBids = 0)
    players.foreach(player => player.roundTricks = 0)
}