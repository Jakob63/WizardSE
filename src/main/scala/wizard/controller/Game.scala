package wizard.controller

import wizard.model.player.Player
import wizard.model.rounds.Game as ModelGame

object Game {
  def apply(players: List[Player]): ModelGame = ModelGame(players)
}