package wizard.controller

import wizard.model.player.Player
import wizard.model.rounds.Game
import wizard.model.cards.Card

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
  
  def playersHands(player: List[Player]): Unit
  def trumpCard(trumpCard: Card): Unit
  def trickCardsList(card: Card): Unit
  def resetTrickCards(): Unit
  
  def getChoice: Option[Int]
  def getPlayer: Option[List[Player]]
  def getTrumpCard: Option[Card]
  def getTrickCards: Option[List[Card]]
  def getState: Option[GameState]
  def getPlayerNumber: Option[Int]

  def setLastIllegalReason(reason: String): Unit
  def getLastIllegalReason: Option[String]
  def clearLastIllegalReason(): Unit
  def consumeLastIllegalReason(): Option[String]

  def playRound(currentround: Int, players: List[Player]): Unit
}
