package wizard.undo

import wizard.model.cards.Hand
import wizard.model.player.Player

/** Command to remove a specific card from a player's hand (on play),
  * allowing the hand to be restored on undo.
  */
class PlayCardCommand(player: Player, afterHand: Hand) extends Command {
  private var oldHand: Hand = player.hand

  override def doStep(): Unit = {
    oldHand = player.hand
    player.hand = afterHand
  }
  override def undoStep(): Unit = {
    player.hand = oldHand
  }
  override def redoStep(): Unit = {
    player.hand = afterHand
  }
}
