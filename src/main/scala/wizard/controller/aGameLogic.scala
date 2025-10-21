package wizard.controller

import wizard.model.player.Player
import wizard.model.rounds.Game

trait aGameLogic {
  def validGame(number: Int): Boolean
  def playGame(game: Game, players: List[Player]): Unit
  def isOver(game: Game): Boolean
}
