package wizard.undo

import org.scalatest.funsuite.AnyFunSuite
import wizard.model.player.{Player, PlayerFactory, PlayerType}

class SetPlayerNameCommandTests extends AnyFunSuite {

  test("doStep changes the player's name, undo restores it, redo reapplies it") {
    val player = PlayerFactory.createPlayer(Some("Alice"), PlayerType.Human)
    val cmd = new SetPlayerNameCommand(player, "Bob")

    // initial
    assert(player.name == "Alice")

    // doStep
    cmd.doStep()
    assert(player.name == "Bob")

    // undoStep
    cmd.undoStep()
    assert(player.name == "Alice")

    // redoStep
    cmd.redoStep()
    assert(player.name == "Bob")
  }

  test("UndoManager integrates with SetPlayerNameCommand") {
    val player = PlayerFactory.createPlayer(Some("Carol"), PlayerType.Human)
    val mgr = new UndoManager
    val cmd = new SetPlayerNameCommand(player, "Dave")

    mgr.doStep(cmd)
    assert(player.name == "Dave")

    mgr.undoStep()
    assert(player.name == "Carol")

    mgr.redoStep()
    assert(player.name == "Dave")
  }
}
