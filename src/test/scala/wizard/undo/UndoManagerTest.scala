package wizard.undo

import org.scalatest.funsuite.AnyFunSuite
import wizard.model.player.{Player, PlayerFactory, PlayerType}

class UndoManagerTest extends AnyFunSuite {

    test("doStep for setting player name") {
        val undoManager = new UndoManager

        // Create player
        val player1 = PlayerFactory.createPlayer(Some("sp1"), PlayerType.Human)

        // Set player1 name to Fred
        val setNameCommand = new SetPlayerNameCommand(player1, "Fred")
        undoManager.doStep(setNameCommand)
        assert(player1.name == "Fred")
    }

    test("undoStep for setting player name") {
        val undoManager = new UndoManager

        // Create player
        val player1 = PlayerFactory.createPlayer(Some("sp1"), PlayerType.Human)

        // Set player1 name to Fred
        val setNameCommand = new SetPlayerNameCommand(player1, "Fred")
        undoManager.doStep(setNameCommand)
        // Undo setting player1 name
        undoManager.undoStep()
        assert(player1.name == "sp1")
    }

    test("redoStep for setting player name") {
        val undoManager = new UndoManager

        // Create player
        val player1 = PlayerFactory.createPlayer(Some("sp1"), PlayerType.Human)

        // Set player1 name to Fred
        val setNameCommand = new SetPlayerNameCommand(player1, "Fred")
        undoManager.doStep(setNameCommand)

        // Undo setting player1 name
        undoManager.undoStep()

        // Redo setting player1 name to Fred
        undoManager.redoStep()
        assert(player1.name == "Fred")
    }

    test("Handle empty list case") {
        val undoManager = new UndoManager

        // Create an empty list of players
        val players: List[Player] = Nil

        // Perform an operation that should handle the empty list
        // For example, trying to undo or redo with an empty list
        assertThrows[NoSuchElementException] {
            undoManager.undoStep()
        }

        assertThrows[NoSuchElementException] {
            undoManager.redoStep()
        }

        // Ensure no players were modified
        assert(players.isEmpty)
    }

    test("redoStep in SetPlayerNameCommand") {
        // Create player
        val player1 = PlayerFactory.createPlayer(Some("sp1"), PlayerType.Human)

        // Set player1 name to Fred
        val setNameCommand = new SetPlayerNameCommand(player1, "Fred")
        setNameCommand.doStep()
        assert(player1.name == "Fred")

        // Undo setting player1 name
        setNameCommand.undoStep()
        assert(player1.name == "sp1")

        // Redo setting player1 name to Fred
        setNameCommand.redoStep()
        assert(player1.name == "Fred")
    }

    test("undo twice reverts from player2 to player1 original names") {
        val undoManager = new UndoManager
        val p1 = PlayerFactory.createPlayer(Some("Alice"), PlayerType.Human)
        val p2 = PlayerFactory.createPlayer(Some("Bob"), PlayerType.Human)
        val c1 = new SetPlayerNameCommand(p1, "AliceNew")
        val c2 = new SetPlayerNameCommand(p2, "BobNew")
        undoManager.doStep(c1)
        undoManager.doStep(c2)
        assert(p1.name == "AliceNew")
        assert(p2.name == "BobNew")
        // undo last (player2)
        undoManager.undoStep()
        assert(p2.name == "Bob")
        // undo again (player1)
        undoManager.undoStep()
        assert(p1.name == "Alice")
    }
}