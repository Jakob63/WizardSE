package wizard.undo

/** A shared UndoManager instance for the whole application.
  * Controllers and views can import UndoService.manager to push commands
  * so that undo/redo affects the unified game state.
  */
object UndoService {
  val manager: UndoManager = new UndoManager
}
