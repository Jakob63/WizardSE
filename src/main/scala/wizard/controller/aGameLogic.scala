package wizard.controller

import wizard.model.player.Player
import wizard.model.rounds.Game

trait aGameLogic {
  def startGame(): Unit
  def handleChoice(choice: Int): Unit
  def enterPlayerNumber(playernumber: Int, current: Int, list: List[Player]): Unit
  def askPlayerNumber(): Unit
  def createGame(players: List[Player]): Unit
  def createPlayers(numPlayers: Int, current: Int = 0, players: List[Player] = List()): Unit
  def validGame(number: Int): Boolean
  def playGame(game: Game, players: List[Player]): Unit
  def isOver(game: Game): Boolean
  
  def getChoice: Option[Int]
}
