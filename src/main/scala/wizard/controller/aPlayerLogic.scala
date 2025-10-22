package wizard.controller

import wizard.model.cards.{Card, Color}
import wizard.model.player.Player

trait aPlayerLogic {
  def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int, player: Player): Card
  def bid(player: Player): Int
  def addPoints(player: Player): Unit
  def calculatePoints(player: Player): Int
}