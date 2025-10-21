package wizard.controller.controllerBaseImpl

import wizard.aView.TextUI.showHand
import wizard.actionmanagement.Observable
import wizard.controller.{aPlayerLogic, aRoundLogic}
import wizard.model.cards.{Card, Dealer, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round;

class BaseRoundLogic extends Observable with aRoundLogic {
  
  // Logics
  var playerLogic: aPlayerLogic = _
  
  

  override def playRound(currentround: Int, players: List[Player]): Unit = {
    val round = new Round(players)
    val trumpCardIndex = currentround * players.length
    val trumpCard = if (trumpCardIndex < Dealer.allCards.length) {
      Dealer.allCards(trumpCardIndex)
    } else {
      throw new IndexOutOfBoundsException("No trump card available.")
    }
    round.setTrump(trumpCard.color)
    notifyObservers("print trump card", trumpCard)

    Dealer.shuffleCards()
    players.foreach { player =>
      val hand = Dealer.dealCards(currentround, Some(trumpCard))
      player.addHand(hand)
    }
    notifyObservers("cards dealt")
    players.foreach(showHand)

    players.foreach(player => playerLogic.bid(player))
    for (i <- 1 to currentround) {
      round.leadColor = None
      var trick = List[(Player, Card)]()
      var firstPlayerIndex = 0

      while (round.leadColor.isEmpty && firstPlayerIndex < players.length) {
        val player = players(firstPlayerIndex)
        val card = playerLogic.playCard(null, round.trump, firstPlayerIndex, player)
        if (card.value != Value.WizardKarte && card.value != Value.Chester) {
          round.leadColor = Some(card.color)
        }
        trick = trick :+ (player, card)
        firstPlayerIndex += 1
      }

      for (j <- firstPlayerIndex until players.length) {
        val player = players(j)
        val card = playerLogic.playCard(round.leadColor.getOrElse(null), round.trump, j, player)
        trick = trick :+ (player, card)
      }

      trick.foreach { case (player, _) =>
        if (!player.hand.isEmpty) {
          showHand(player)
        }
      }
      val winner = trickwinner(trick, round)
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

  override def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
    val leadColor = trick.head._2.color
    val trump = round.trump
    val leadColorCards = trick.filter(_._2.color == leadColor)
    val trumpCards = trick.filter(_._2.color == trump)
    val winningCard = if (trumpCards.nonEmpty) {
      trumpCards.maxBy(_._2.value.ordinal)
    } else {
      leadColorCards.maxBy(_._2.value.ordinal)
    }
    winningCard._1
  }
}
