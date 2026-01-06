package wizard.controller.controllerBaseImpl

import wizard.aView.TextUI.showHand
import wizard.actionmanagement.Observable
import wizard.controller.{aPlayerLogic, aRoundLogic}
import wizard.model.cards.{Card, Dealer, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round;

class BaseRoundLogic extends Observable with aRoundLogic{

  var playerLogic: aPlayerLogic = _

  override def playRound(currentround: Int, players: List[Player]): Unit = {
    val round = new Round(players)
    val trumpCardIndex = currentround * players.length
    val trumpCard = if (trumpCardIndex < Dealer.allCards.length) {
      Dealer.allCards(trumpCardIndex)
    } else {
      throw new IndexOutOfBoundsException("No trump card available.")
    }
    round.setTrump(Some(trumpCard.color))
    
    // Zuerst Karten verteilen, damit GUI beim "CardsDealt" Event die Spielerliste hat
    Dealer.shuffleCards()
    players.foreach { player =>
      val hand = Dealer.dealCards(currentround, Some(trumpCard))
      player.addHand(hand)
    }
    
    notifyObservers("CardsDealt", wizard.actionmanagement.CardsDealt(players))
    notifyObservers("print trump card", trumpCard)

    // Determine the starting player index for this round (rotates each round)
    val startIdx = (currentround - 1) % players.length
    val orderPlayers: List[Player] = players.drop(startIdx) ++ players.take(startIdx)

    // Bidding phase: each player (starting from rotating start) sees only their own hand and bids
    playersTurn(currentround, players, startIdx)

    // Playing phase: play tricks
    for (_ <- 1 to currentround) {
      val winner = playTrick(orderPlayers, round)
      notifyObservers("trick winner", winner)
      winner.roundTricks += 1
    }

    players.foreach(player => {
      player.addTricks(player.roundTricks)
    })
    players.foreach(player => {
      playerLogic.addPoints(player)
    })
    notifyObservers("points after round")
    notifyObservers("print points all players", players)
  }

  override def playTrick(orderPlayers: List[Player], round: Round): Player = {
    round.leadColor = None
    var trick = List[(Player, Card)]()
    var firstPlayerIndex = 0

    // Players play until a non-Wizard/Jester sets the lead color
    while (round.leadColor.isEmpty && firstPlayerIndex < orderPlayers.length) {
      val player = orderPlayers(firstPlayerIndex)
      // showHand(player) - removed as notifyObservers is used in playerLogic.playCard
      val card = playerLogic.playCard(null, round.trump.getOrElse(null), firstPlayerIndex, player)
      if (card.value != Value.WizardKarte && card.value != Value.Chester) {
        round.leadColor = Some(card.color)
      }
      trick = trick :+ (player, card)
      firstPlayerIndex += 1
    }

    // Remaining players must follow the lead color if possible
    for (j <- firstPlayerIndex until orderPlayers.length) {
      val player = orderPlayers(j)
      // showHand(player) - removed
      val card = playerLogic.playCard(round.leadColor.getOrElse(null), round.trump.getOrElse(null), j, player)
      trick = trick :+ (player, card)
    }

    trickwinner(trick, round)
  }

  override def playersTurn(currentround: Int, players: List[Player], startIdx: Int): Unit = {
    val orderPlayers: List[Player] = players.drop(startIdx) ++ players.take(startIdx)
    var idx = 0
    while (idx < orderPlayers.length) {
      val player = orderPlayers(idx)
      // Show only the current player's hand and ask for the bid
      // showHand(player) - removed
      playerLogic.bid(player)
      idx += 1
    }
  }


  override def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
    // Wizard/Jester resolution (Wizard = highest, Jester = lowest)
    // 1) If any Wizard was played, the first Wizard wins the trick.
    val wizards = trick.zipWithIndex.filter { case ((_, card), _) => card.value == Value.WizardKarte }
    if (wizards.nonEmpty) {
      val ((player, _), _) = wizards.minBy(_._2) // first wizard by play order
      return player
    }

    // Determine the lead color as the color of the first non-Jester, non-Wizard card.
    val leadColorOpt = trick.collectFirst {
      case ((_, card)) if card.value != Value.WizardKarte && card.value != Value.Chester => card.color
    }
    val trumpOpt = round.trump

    // 2) If any trump (non-Jester) was played, highest trump (by value) wins.
    val trumpCards = trick.filter { case (_, c) => trumpOpt.exists(tc => c.color == tc) && c.value != Value.Chester }
    if (trumpCards.nonEmpty) {
      return trumpCards.maxBy(_._2.value.ordinal)._1
    }

    // 3) Otherwise, highest card of the lead color (non-Jester) wins.
    val leadColorCards = leadColorOpt.toList.flatMap { lc =>
      trick.filter { case (_, c) => c.color == lc && c.value != Value.Chester }
    }
    if (leadColorCards.nonEmpty) {
      return leadColorCards.maxBy(_._2.value.ordinal)._1
    }

    // 4) If only Jesters were played, the first Jester wins the trick.
    val firstJester = trick.find { case (_, c) => c.value == Value.Chester }
    firstJester.map(_._1).getOrElse(trick.head._1)
  }
}