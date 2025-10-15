package wizard.controller

import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Player
import wizard.aView.TextUI
import wizard.actionmanagement.{Observable, Observer}
import wizard.undo.{BidCommand, PlayCardCommand, UndoService}

class PlayerLogic extends Observable {
    //add(TextUI)

    def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int, player: Player): Card = {
        notifyObservers("which card", player)
        val cardToPlay = player.playCard(leadColor, trump, currentPlayerIndex)
        leadColor match {
            case Some(lc) if cardToPlay.color != lc && player.hand.hasColor(lc) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester =>
                notifyObservers("follow lead", lc)
                playCard(leadColor, trump, currentPlayerIndex, player)
            case _ =>
                val before = player.hand
                val after = player.hand.removeCard(cardToPlay)
                UndoService.manager.doStep(new PlayCardCommand(player, before, after))
                cardToPlay
        }
    }

    def bid(player: Player): Int = {
        notifyObservers("which bid", player)
        val playersbid = player.bid()
        UndoService.manager.doStep(new BidCommand(player, playersbid))
        playersbid
    }

    def addPoints(player: Player): Unit = {
        if (player.roundBids == player.roundTricks) {
            player.points += 20 + 10 * player.roundBids
        } else {
            player.points -= 10 * Math.abs(player.roundBids - player.roundTricks)
        }
    }

    def calculatePoints(player: Player): Int = {
        val points = player.points
        val bids = player.roundBids
        val tricks = player.roundTricks
        if (bids == tricks) {
            points + 20 + tricks * 10
        } else {
            points - Math.abs(bids - tricks) * 10
        }
    }
}

object PlayerLogic {
  private val instance = new PlayerLogic
  
  def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int, player: Player): Card =
    instance.playCard(leadColor, trump, currentPlayerIndex, player)

  def bid(player: Player): Int = instance.bid(player)

  def addPoints(player: Player): Unit = instance.addPoints(player)

  def calculatePoints(player: Player): Int = instance.calculatePoints(player)
}