package wizard.model

import wizard.model.player.Player
import wizard.model.cards.Card

case class Game(players: List[Player]) {
  var rounds: Int = if (players.nonEmpty) 60 / players.length else 0
  var currentround: Int = 0
  var currentTrick: List[Card] = Nil
  var firstPlayerIdx: Int = 0
}