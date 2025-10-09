package wizard.undo

import wizard.model.cards.Hand
import wizard.model.player.Player

/** Command to remove a specific card from a player's hand (on play),
  * allowing the hand to be restored on undo.
  */
class PlayCardCommand(player: Player, beforeHand: Hand, afterHand: Hand) extends Command {
  override def doStep(): Unit = {
    player.hand = afterHand
  }
  override def undoStep(): Unit = {
    player.hand = beforeHand
  }
  override def redoStep(): Unit = doStep()
}
