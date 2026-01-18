package wizard.undo

import wizard.controller.GameLogic
import wizard.model.player.Player

class StartGameCommand(gameLogic: GameLogic, players: List[Player]) extends Command {
  override def doStep(): Unit = {
  }

  override def undoStep(): Unit = {
    gameLogic.stopGame()
    wizard.actionmanagement.InputRouter.offer("__GAME_STOPPED__")
    gameLogic.notifyObservers("AskForPlayerNames", wizard.actionmanagement.AskForPlayerNames)
  }

  override def redoStep(): Unit = {
    gameLogic.setPlayersFromRedo(players)
  }
}
