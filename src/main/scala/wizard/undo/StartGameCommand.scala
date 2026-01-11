package wizard.undo

import wizard.controller.GameLogic
import wizard.model.player.Player

/** Command that represents the transition from player naming to starting the game.
  * Undoing this command should bring the user back to the player naming screen.
  */
class StartGameCommand(gameLogic: GameLogic, players: List[Player]) extends Command {
  override def doStep(): Unit = {
    // Starting the game is handled by the controller when this command is first executed
  }

  override def undoStep(): Unit = {
    // When undoing the game start, we want to go back to the player names screen.
    // First, signal the current game thread to stop.
    gameLogic.stopGame()
    // Unblock any pending InputRouter.readLine in game thread
    try { wizard.actionmanagement.InputRouter.offer("__GAME_STOPPED__") } catch { case _: Throwable => () }
    // We notify the observers so they can switch their UI state.
    gameLogic.notifyObservers("AskForPlayerNames", wizard.actionmanagement.AskForPlayerNames)
  }

  override def redoStep(): Unit = {
    // Redoing the game start would ideally restart the game thread with the same players.
    // Use a flag to avoid infinite recursion if redoStep calls setPlayers which calls doStep again.
    gameLogic.setPlayersFromRedo(players)
  }
}
