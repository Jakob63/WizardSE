package wizard.controller.controllerBaseImpl

import wizard.actionmanagement.Observable
import wizard.controller.GameState.Menu
import wizard.controller.{GameState, aGameLogic, aRoundLogic}
import wizard.model.player.Player
import wizard.model.rounds.{Game, Round}
import wizard.model.cards.{Card, Dealer}

class BaseGameLogic extends Observable with aGameLogic{

  // Logics
  var roundLogic: aRoundLogic = _
  
  var varchoice: Option[Int] = None
  var lastplayer: Option[List[Player]] = None
  var trumpcard: Option[Card] = None
  var trickCards: Option[List[Card]] = None
  var state: Option[GameState] = Some(GameState.Menu)
  var playerNumber: Option[Int] = None

  private var lastIllegalReason: Option[String] = None


  override def startGame() = {
    varchoice = Some(1)
    askPlayerNumber()
  }
  
  override def handleChoice(choice: Int) = {
    varchoice = Some(choice)
    if (choice == 2) {
      notifyObservers("main menu exit")
      System.exit(0)
    } else if (choice != 1) {
      notifyObservers("main menu wrong input")
      notifyObservers("main menu")
    } else {
      println("Starting the game...")
      askPlayerNumber()
    }
  }

  override def enterPlayerNumber(playernumber: Int, current: Int, list: List[Player]): Unit = {
    notifyObservers("player names", playernumber, current, list)
  }

  override def askPlayerNumber(): Unit = {
    notifyObservers("input players")
  }

  override def createGame(players: List[Player]) = {
    val game = Game(players)
    state = Some(GameState.Ingame)
    notifyObservers("game started")
    playGame(game, players)
  }

  override def createPlayers(numPlayers: Int, current: Int = 0, players: List[Player] = List()) = {
    playerNumber = Some(numPlayers)
    if (current < numPlayers) {
      notifyObservers("player names", numPlayers, current, players)
    } else {
      createGame(players)
    }

  }

  override def validGame(number: Int): Boolean = {
    number >= 3 && number <= 6
  }

  override def playGame(game: Game, players: List[Player]): Unit = {
    for (i <- 1 to game.rounds) { // i = 1, 2, 3, ..., rounds
      game.currentround = i
      val round = new Round(players)
      roundLogic.playRound(game.currentround, players)
    }
  }

  override def isOver(game: Game): Boolean = {
    state = Some(GameState.Endscreen)
    game.rounds == 0
  }
  
  override def playersHands(players: List[Player]): Unit = {
    lastplayer = Some(players)
  }
  override def trumpCard(card: Card): Unit = {
      trumpcard = Some(card)
  }

  override def trickCardsList(playedCard: Card): Unit = {
    if (trickCards.isEmpty) {
      trickCards = Some(List(playedCard))
    } else {
      val updatedTrickCards = trickCards.get :+ playedCard
      trickCards = Some(updatedTrickCards)
    }
  }
  override def resetTrickCards(): Unit = {
    trickCards = None
  }

  override def playRound(currentround: Int, players: List[Player]): Unit = {
    roundLogic.playRound(currentround, players)
  }

  override def getChoice: Option[Int] = varchoice
  override def getState: Option[GameState] = state
  override def getPlayer: Option[List[Player]] = lastplayer
  override def getTrumpCard: Option[Card] = trumpcard
  override def getTrickCards: Option[List[Card]] = trickCards
  override def getPlayerNumber: Option[Int] = playerNumber

  override def setLastIllegalReason(reason: String): Unit = {
    lastIllegalReason = Option(reason)
  }

  override def getLastIllegalReason: Option[String] = lastIllegalReason

  override def clearLastIllegalReason(): Unit = {
    lastIllegalReason = None
  }

  override def consumeLastIllegalReason(): Option[String] = {
    val tmp = lastIllegalReason
    lastIllegalReason = None
    tmp
  }
}