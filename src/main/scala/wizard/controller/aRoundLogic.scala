package wizard.controller

import wizard.model.cards.Card
import wizard.model.player.Player
import wizard.model.rounds.Round

trait aRoundLogic {
  def playRound(currentround: Int, players: List[Player]): Unit
  def playTrick(orderPlayers: List[Player], round: Round): Player
  def playersTurn(currentround: Int, players: List[Player], startIdx: Int): Unit
  def trickwinner(trick: List[(Player, Card)], round: Round): Player
  
}