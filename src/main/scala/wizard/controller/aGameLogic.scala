package wizard.controller

import wizard.model.player.Player
import wizard.model.rounds.Game
import wizard.actionmanagement.Observer

trait aGameLogic {
  // High-level entry point used by GUI/TUI to kick off the flow
  def start(): Unit
  def startGame(): Unit
  def askPlayerNumber(): Unit
  def createGame(players: List[Player]): Unit
  def createPlayers(numPlayers: Int, current: Int = 0, players: List[Player] = List()): Unit
  // Called by views when the player count has been selected by the user
  def playerCountSelected(numPlayers: Int): Unit
  // Called by views after collecting player names
  def setPlayers(players: List[Player]): Unit
  def validGame(number: Int): Boolean
  def playGame(game: Game, players: List[Player]): Unit
  def isOver(game: Game): Boolean
  // Reset any temporary selection related to player count/name entry and navigate back to player-count input
  def resetPlayerCountSelection(): Unit
  // Observer pattern API exposed so views (TUI/GUI) can register on the controller
  def add(s: Observer): Unit
  def remove(s: Observer): Unit
}