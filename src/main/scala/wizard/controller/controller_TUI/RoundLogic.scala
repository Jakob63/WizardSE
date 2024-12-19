package wizard.controller.controller_TUI

import wizard.aView.aView_TUI.TextUI
import wizard.aView.aView_TUI.TextUI.showHand
import wizard.actionmanagement.{Observable, Observer}
import wizard.controller.controller_TUI.{ChesterCardState, NormalCardState, WizardCardState}
import wizard.model.model_TUI.cards.{Card, Color, Dealer, Value}
import wizard.model.model_TUI.player.Player
import wizard.model.model_TUI.rounds.Round

object RoundLogic extends Observable {
    add(TextUI)

    def playRound(currentround: Int, players: List[Player]): Unit = {
        val round = new Round(players)
        val trumpCardIndex = currentround * players.length
        val trumpCard = if (trumpCardIndex < Dealer.allCards.length) {
            Dealer.allCards(trumpCardIndex)
        } else {
            throw new IndexOutOfBoundsException("No trump card available.")
        }

        trumpCard.value match {
            case Value.Chester => round.setState(new ChesterCardState)
            case Value.WizardKarte => round.setState(new WizardCardState)
            case _ => round.setState(new NormalCardState)
        }

        Dealer.shuffleCards()
        players.foreach { player =>
            val hand = Dealer.dealCards(currentround, Some(trumpCard))
            player.addHand(hand)
        }
        notifyObservers("cards dealt")
        players.foreach(showHand)

        round.handleTrump(trumpCard, players)

        players.foreach(player => PlayerLogic.bid(player))
        for (i <- 1 to currentround) {
            round.leadColor = None
            var trick = List[(Player, Card)]()
            var firstPlayerIndex = 0

            while (round.leadColor.isEmpty && firstPlayerIndex < players.length) {
                val player = players(firstPlayerIndex)
                val card = PlayerLogic.playCard(None, round.trump, firstPlayerIndex, player)
                if (card.value != Value.WizardKarte && card.value != Value.Chester) {
                    round.leadColor = Some(card.color)
                }
                trick = trick :+ (player, card)
                firstPlayerIndex += 1
            }

            for (j <- firstPlayerIndex until players.length) {
                val player = players(j)
                val card = PlayerLogic.playCard(round.leadColor, round.trump, j, player)
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
            PlayerLogic.addPoints(player)
        })
        notifyObservers("points after round")
        notifyObservers("print points all players", players)
    }

    def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
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