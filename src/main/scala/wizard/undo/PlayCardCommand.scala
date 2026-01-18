package wizard.undo

import wizard.model.cards.Hand
import wizard.model.player.Player

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
