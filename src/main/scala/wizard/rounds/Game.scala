// Game.scala
package wizard.rounds

import wizard.player.Player
import wizard.controll.GameLogic

case class Game(players: List[Player]) {
    
    val rounds = 60 / players.length
    var currentround = 0
    
    players.foreach(player => player.points = 0)
    players.foreach(player => player.tricks = 0)
    players.foreach(player => player.bids = 0)
    players.foreach(player => player.roundPoints = 0)
    players.foreach(player => player.roundBids = 0)
    players.foreach(player => player.roundTricks = 0)
}