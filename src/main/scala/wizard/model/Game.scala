package wizard.model

import wizard.model.player.Player

case class Game(players: List[Player]) {
  var rounds: Int = if (players.nonEmpty) 60 / players.length else 0
  var currentround: Int = 0

  // initialize player round-related fields
  players.foreach { player =>
    player.points = 0
    player.tricks = 0
    player.bids = 0
    player.roundPoints = 0
    player.roundBids = 0
    player.roundTricks = 0
  }
}