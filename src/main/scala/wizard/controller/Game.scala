package wizard.controller

import wizard.model.Game as ModelGame
import wizard.model.player.Player

object Game {
  def apply(players: List[Player]): ModelGame = ModelGame(players)
}