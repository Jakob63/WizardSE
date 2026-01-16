package wizard.model.fileIoComponent

import wizard.model.Game
import wizard.model.cards.Card

trait FileIOInterface {
  def load(path: String): (Game, Int, Option[Card], Int, Int)
  def save(game: Game, currentRound: Int, trumpCard: Option[Card], dealerIndex: Int, firstPlayerIdx: Int, path: String): Unit
}
