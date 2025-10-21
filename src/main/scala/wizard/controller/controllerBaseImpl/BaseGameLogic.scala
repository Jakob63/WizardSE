package wizard.controller.controllerBaseImpl

import wizard.actionmanagement.Observable
import wizard.controller.{aGameLogic, aRoundLogic}
import wizard.model.player.Player
import wizard.model.rounds.{Game, Round}

class BaseGameLogic extends Observable with aGameLogic{
  
  
  
  // Logics
  val roundLogic: aRoundLogic = new BaseRoundLogic()
  
  
  
  override def startGame() = {
    notifyObservers("main menu")
  }

  override def askPlayerNumber(): Unit = {
    notifyObservers("input players")
  }

  override def createGame(players: List[Player]) = {
    val game = Game(players)
    notifyObservers("game started")
    playGame(game, players)
  }

  override def createPlayers(numPlayers: Int, current: Int = 0, players: List[Player] = List()) = {
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

  // game is over if all rounds are played
  override def isOver(game: Game): Boolean = {
    game.rounds == 0
  }
}
