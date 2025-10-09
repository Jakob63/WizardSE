package wizard.undo

import wizard.model.player.Player

/** Command to set a player's bid for the current round. */
class BidCommand(player: Player, newBid: Int) extends Command {
  private var oldBid: Int = player.roundBids

  override def doStep(): Unit = {
    oldBid = player.roundBids
    player.roundBids = newBid
  }

  override def undoStep(): Unit = {
    player.roundBids = oldBid
  }

  override def redoStep(): Unit = {
    player.roundBids = newBid
  }
}
