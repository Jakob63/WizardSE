package wizard.controller.controllerBaseImpl

import wizard.aView.TextUI.showHand
import wizard.actionmanagement.Observable
import wizard.controller.{aGameLogic, aPlayerLogic, aRoundLogic}
import wizard.model.cards.{Card, Dealer, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round
import org.apache.pekko.actor.ActorRef

class BaseRoundLogic extends Observable with aRoundLogic {

  var playerLogic: aPlayerLogic = _
  var gameLogic: aGameLogic = _
  var gameSocketActor: Option[ActorRef] = None

  def setGameSocketActor(actor: ActorRef): Unit = {
    gameSocketActor = Some(actor)
  }

  override def playRound(currentround: Int, players: List[Player]): Unit = {
    val round = new Round(players)
    Dealer.shuffleCards()

    val trumpCardIndex = currentround * players.length
    val trumpCard = if (trumpCardIndex < Dealer.allCards.length) {
      Dealer.allCards(trumpCardIndex)
    } else {
      throw new IndexOutOfBoundsException("No trump card available.")
    }
    round.setTrump(trumpCard.color)
    gameLogic.trumpCard(trumpCard)
    notifyObservers("print trump card", trumpCard)
    gameSocketActor.foreach { actor =>
      actor ! s"Trump card for round $currentround: ${trumpCard.value} of ${trumpCard.color}"
    }

    players.foreach { player =>
      val hand = Dealer.dealCards(currentround, Some(trumpCard))
      player.addHand(hand)
    }
    gameLogic.playersHands(players)

    val startIdx = (currentround - 1) % players.length
    val orderPlayers: List[Player] = players.drop(startIdx) ++ players.take(startIdx)

    playersTurn(currentround, players, startIdx)

    for (_ <- 1 to currentround) {
      val winner = playTrick(orderPlayers, round)
      notifyObservers("trick winner", winner)
      gameSocketActor.foreach { actor =>
        actor ! s"Trick winner: ${winner.name}"
      }
      gameLogic.resetTrickCards()
      winner.roundTricks += 1
    }

    players.foreach { player =>
      player.addTricks(player.roundTricks)
      playerLogic.addPoints(player)
    }

    notifyObservers("points after round")
    notifyObservers("print points all players", players)
    players.foreach { player =>
      gameSocketActor.foreach { actor =>
        actor ! s"Player ${player.name} has ${player.points} points."
      }
    }
  }

  override def playTrick(orderPlayers: List[Player], round: Round): Player = {
    round.leadColor = None
    var trick = List[(Player, Card)]()
    var firstPlayerIndex = 0

    while (round.leadColor.isEmpty && firstPlayerIndex < orderPlayers.length) {
      val player = orderPlayers(firstPlayerIndex)
      showHand(player)
      val card = playerLogic.playCard(null, round.trump, firstPlayerIndex, player)
      if (card.value != Value.WizardKarte && card.value != Value.Chester) {
        round.leadColor = Some(card.color)
      }
      trick = trick :+ (player, card)
      firstPlayerIndex += 1
    }

    for (j <- firstPlayerIndex until orderPlayers.length) {
      val player = orderPlayers(j)
      showHand(player)
      val card = playerLogic.playCard(round.leadColor.getOrElse(null), round.trump, j, player)
      trick = trick :+ (player, card)
    }

    trickwinner(trick, round)
  }

  override def playersTurn(currentround: Int, players: List[Player], startIdx: Int): Unit = {
    val orderPlayers: List[Player] = players.drop(startIdx) ++ players.take(startIdx)
    var idx = 0
    while (idx < orderPlayers.length) {
      val player = orderPlayers(idx)
      notifyObservers("show hand of Player x", player)
      gameSocketActor.foreach { actor =>
        actor ! s"Player ${player.name}, please place your bid."
      }
      playerLogic.bid(player)
      idx += 1
    }
  }


  override def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
    val wizards = trick.zipWithIndex.filter { case ((_, card), _) => card.value == Value.WizardKarte }
    if (wizards.nonEmpty) {
      val ((player, _), _) = wizards.minBy(_._2)
      return player
    }

    val leadColorOpt = trick.collectFirst {
      case ((_, card)) if card.value != Value.WizardKarte && card.value != Value.Chester => card.color
    }
    val trump = round.trump

    val trumpCards = trick.filter { case (_, c) => c.color == trump && c.value != Value.Chester }
    if (trumpCards.nonEmpty) {
      return trumpCards.maxBy(_._2.value.ordinal)._1
    }

    val leadColorCards = leadColorOpt.toList.flatMap { lc =>
      trick.filter { case (_, c) => c.color == lc && c.value != Value.Chester }
    }
    if (leadColorCards.nonEmpty) {
      return leadColorCards.maxBy(_._2.value.ordinal)._1
    }

    val firstJester = trick.find { case (_, c) => c.value == Value.Chester }
    firstJester.map(_._1).getOrElse(trick.head._1)
  }
}
