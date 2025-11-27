package wizard.controller.controllerBaseImpl

import org.apache.pekko.actor.ActorRef
import wizard.actionmanagement.Observable
import wizard.controller.{aGameLogic, aPlayerLogic}
import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Player

class BasePlayerLogic extends Observable with aPlayerLogic {

  var gameLogic: aGameLogic = _
  var gameSocketActor: Option[ActorRef] = None

  def setGameSocketActor(actor: ActorRef): Unit = {
    gameSocketActor = Some(actor)
  }

  override def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int, player: Player): Card = {
    notifyObservers("which card", player)
    gameSocketActor.foreach { actor =>
      actor ! s"player:${player.name} choose card to play"
    }

    val input = userInput.readLine()
    val cardIndex = try {
      input.toInt
    } catch {
      case _: NumberFormatException => -1
    }

    if (cardIndex < 1 || cardIndex > player.hand.cards.length) {
      notifyObservers("invalid card")
      return playCard(leadColor, trump, currentPlayerIndex, player)
    }

    val cardToPlay = player.hand.cards(cardIndex - 1)
    gameLogic.trickCardsList(cardToPlay)

    if (leadColor != null && cardToPlay.color != leadColor && player.hand.hasColor(leadColor) && cardToPlay.value != Value.WizardKarte && cardToPlay.value != Value.Chester) {
      gameLogic.setLastIllegalReason(s"You have to follow the lead suit $leadColor.")
      notifyObservers("follow lead", leadColor)
      return playCard(leadColor, trump, currentPlayerIndex, player)
    } else {
      player.hand = player.hand.removeCard(cardToPlay)
      gameSocketActor.foreach { actor =>
        actor ! s"player:${player.name} played card: ${cardToPlay.value} of ${cardToPlay.color}"
      }
      cardToPlay
    }
  }

  override def bid(player: Player): Int = {
    notifyObservers("which bid", player)
    gameSocketActor.foreach { actor =>
      actor ! s"player:${player.name} place bid"
    }

    val input = userInput.readLine()
    if (input == "" || input.trim.isEmpty || !input.forall(_.isDigit)) {
      notifyObservers("invalid input, bid again")
      return bid(player)
    }

    val playersBid = input.toInt
    player.roundBids = playersBid
    gameSocketActor.foreach { actor =>
      actor ! s"player:${player.name} bid $playersBid"
    }
    playersBid
  }

  override def addPoints(player: Player): Unit = {
    if (player.roundBids == player.roundTricks) {
      player.points += 20 + 10 * player.roundBids
    } else {
      player.points -= 10 * Math.abs(player.roundBids - player.roundTricks)
    }
    gameSocketActor.foreach { actor =>
      actor ! s"player:${player.name} points updated: ${player.points}"
    }
  }

  override def calculatePoints(player: Player): Int = {
    val points = player.roundPoints
    val bids = player.roundBids
    val tricks = player.roundTricks
    if (bids == tricks) {
      points + 20 + tricks * 10
    } else {
      points - Math.abs(bids - tricks) * 10
    }
  }
}