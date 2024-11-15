package wizard.Controller.control

import wizard.Model.player.Player
import wizard.Model.rounds.Round
import wizard.View.textUI.TextUI.showHand
import PlayerLogic.{playCard, bid}
import wizard.Model.cards.{Card, Color, Dealer, Value}
import wizard.View.textUI.TextUI

object RoundLogic {
    def playRound(currentround: Int, players: List[Player]): Unit = {
        val round = new Round(players)
        val trumpCardIndex = currentround * players.length
        val trumpCard = if (trumpCardIndex < Dealer.allCards.length) {
            Dealer.allCards(trumpCardIndex)
        } else {
            throw new IndexOutOfBoundsException("No trump card available.")
        }
        round.setTrump(trumpCard.color)
        println(s"Trump card:\n${TextUI.showcard(trumpCard)}")

        players.foreach { player =>
            val hand = Dealer.dealCards(currentround, Some(trumpCard))
            player.addHand(hand)
        }
        println("Cards dealt to all players.")
        players.foreach(showHand)

        players.foreach(player => PlayerLogic.bid(player))
        for (i <- 1 to currentround) {
            round.leadColor = None
            var trick = List[(Player, Card)]()
            var firstPlayerIndex = 0

            while (round.leadColor.isEmpty && firstPlayerIndex < players.length) {
                val player = players(firstPlayerIndex)
                val card = PlayerLogic.playCard(null, round.trump, firstPlayerIndex, player)
                if (card.value != Value.WizardKarte && card.value != Value.Chester) {
                    round.leadColor = Some(card.color)
                }
                trick = trick :+ (player, card)
                firstPlayerIndex += 1
            }

            for (j <- firstPlayerIndex until players.length) {
                val player = players(j)
                val card = PlayerLogic.playCard(round.leadColor.getOrElse(null), round.trump, j, player)
                trick = trick :+ (player, card)
            }

            trick.foreach { case (player, _) =>
                if (!player.hand.isEmpty) {
                    showHand(player)
                }
            }
            val winner = trickwinner(trick, round)
            println(s"${winner.name} wins the trick.")
            winner.roundTricks += 1
        }

        players.foreach(player => player.addTricks(player.roundTricks))
        players.foreach(PlayerLogic.addPoints)
        println("Points after this round:")
        players.foreach(player => println(s"${player.name}: ${player.points} points"))
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