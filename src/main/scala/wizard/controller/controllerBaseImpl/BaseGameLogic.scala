package wizard.controller.controllerBaseImpl

import wizard.actionmanagement.Observable
import wizard.controller.{aGameLogic, aRoundLogic}
import wizard.model.player.Player
import wizard.model.rounds.{Game, Round}

class BaseGameLogic extends Observable with aGameLogic{

  // Logics
  var roundLogic: aRoundLogic = _

  // Entry point used by views to kick off the flow
  override def start(): Unit = startGame()

  override def startGame() = {
    askPlayerNumber()
  }

  override def askPlayerNumber(): Unit = {
    notifyObservers("input players")
    val input = wizard.actionmanagement.InputRouter.readLine()
    val numPlayers = try { input.toInt } catch { case _: Exception => -1 }
    if (validGame(numPlayers)) {
      createPlayers(numPlayers, 0, List())
    } else {
      askPlayerNumber()
    }
  }

  override def createGame(players: List[Player]) = {
    val game = Game(players)
    notifyObservers("game started")
    playGame(game, players)
  }

  override def createPlayers(numPlayers: Int, current: Int = 0, players: List[Player] = List()) = {
    if (current < numPlayers) {
      notifyObservers("player names", numPlayers, current, players)
      val name = wizard.actionmanagement.InputRouter.readLine().trim
      val pattern = "^[a-zA-Z0-9]+$".r
      if (name.nonEmpty && pattern.pattern.matcher(name).matches()) {
        val player = wizard.model.player.Player(name)
        createPlayers(numPlayers, current + 1, players.appended(player))
      } else {
        notifyObservers("invalid name") // New event for feedback
        createPlayers(numPlayers, current, players)
      }
    } else {
      createGame(players)
    }

  }

  // Called by GUI/TUI when the user selected the number of players
  override def playerCountSelected(numPlayers: Int): Unit = {
    wizard.actionmanagement.InputRouter.offer(numPlayers.toString)
  }

  // Called by GUI/TUI when the list of players has been entered
  override def setPlayers(players: List[Player]): Unit = {
    players.foreach(p => wizard.actionmanagement.InputRouter.offer(p.name))
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

  // Navigate back to player count input (used by GUI/TUI undo flow)
  override def resetPlayerCountSelection(): Unit = {
    notifyObservers("input players")
  }
}