package wizard.undo

import wizard.model.player.Player

trait Command {
    def doStep(): Unit
    def undoStep(): Unit
    def redoStep(): Unit
}

class SetPlayerNameCommand(player: Player, newName: String) extends Command {
    private var oldName: String = player.name

    override def doStep(): Unit = {
        oldName = player.name
        player.name = newName
    }

    override def undoStep(): Unit = {
        player.name = oldName
    }

    override def redoStep(): Unit = {
        player.name = newName
    }
}
