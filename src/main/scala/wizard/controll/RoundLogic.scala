package wizard.controll

import wizard.player.Player
import wizard.cards.Dealer
import wizard.textUI.TextUI.showHand
import wizard.controll.PlayerLogic.{playCard, bid}
import wizard.rounds.Round
import wizard.cards.Value
import wizard.cards.Color
import wizard.cards.Card

object RoundLogic {
    // Methode zum Spielen einer Runde
    def playRound(currentround: Int, players: List[Player]): Unit = {
        val round = new Round(players)
        players.foreach { player =>
            val hand = Dealer.dealCards(currentround)
            player.addHand(hand)
        }
        println("Cards dealt to all players.")
        players.foreach(showHand)
        println("Trump card:")
        val trumpCard = Dealer.allCards(currentround * players.length)
        round.setTrump(trumpCard.color)
        // Eigentlich CurrentRound * PlayerCount müssen wir noch machen
        Dealer.printCardAtIndex(currentround * players.length)
        // Bieten bzw angeben wie viele
        players.foreach(player => bid(player))
        // Karten spielen
        for (i <- 1 to currentround) {
            round.leadColor = None
            val trick = players.map { player =>
                val card = playCard(round.leadColor.getOrElse(null), round.trump, round.currentPlayerIndex, player)
                if (round.leadColor.isEmpty && card.value != Value.WizardKarte && card.value != Value.Chester) {
                    round.leadColor = Some(card.color)
                }
                (player, card)
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
        players.foreach(player => PlayerLogic.addPoints(player))
        println("Points after this round:")
        players.foreach(player => println(s"${player.name}: ${player.points} points"))
    
    }

    def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
        // Find the lead color
        val leadColor = trick.head._2.color
        val trump = round.trump

        // Filter cards by lead color and trump color
        val leadColorCards = trick.filter(_._2.color == leadColor)
        val trumpCards = trick.filter(_._2.color == trump)

        // Determine the winning card
        val winningCard = if (trumpCards.nonEmpty) {
            trumpCards.maxBy(_._2.value.ordinal)
        } else {
            leadColorCards.maxBy(_._2.value.ordinal)
        }

        // Return the player who played the winning card
        winningCard._1
    }
}
