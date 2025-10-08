package wizard.controller

import wizard.aView.TextUI
import wizard.controller.PlayerLogic
import wizard.model.cards.{Card, Color, Dealer, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round
import wizard.actionmanagement.{CardsDealt, Observable, Observer, ShowHand}


class RoundLogic extends Observable {
    
    private val playerLogic = new PlayerLogic

    // Make sure observers added to RoundLogic also observe PlayerLogic events
    override def add(s: Observer): Unit = {
        super.add(s)
        playerLogic.add(s)
    }
    
    def playRound(currentround: Int, players: List[Player]): Unit = {
        val round = new Round(players)
        val trumpCardIndex = currentround * players.length
        val trumpCardOpt: Option[Card] = if (trumpCardIndex < Dealer.allCards.length) {
            Some(Dealer.allCards(trumpCardIndex))
        } else {
            None
        }

        trumpCardOpt match {
            case Some(trumpCard) =>
                trumpCard.value match {
                    case Value.Chester => round.setState(new ChesterCardState)
                    case Value.WizardKarte => round.setState(new WizardCardState)
                    case _ => round.setState(new NormalCardState)
                }
            case None =>
                // No trump card available this round (e.g., final round). No trump.
                round.setTrump(None)
                round.setState(new NormalCardState)
        }

        Dealer.shuffleCards()
        players.foreach { player =>
            val hand = Dealer.dealCards(currentround, trumpCardOpt)
            player.addHand(hand)
        }
        notifyObservers("CardsDealt", CardsDealt(players))

        trumpCardOpt.foreach { trumpCard =>
            round.handleTrump(trumpCard, players)
            // Ensure trump is displayed to observers (TUI/GUI) before bidding
            notifyObservers("print trump card", trumpCard)
        }

        players.foreach(player => playerLogic.bid(player))
        for (i <- 1 to currentround) {
            round.leadColor = None
            var trick = List[(Player, Card)]()
            var firstPlayerIndex = 0

            while (round.leadColor.isEmpty && firstPlayerIndex < players.length) {
                val player = players(firstPlayerIndex)
                val card = playerLogic.playCard(None, round.trump, firstPlayerIndex, player)
                if (card.value != Value.WizardKarte && card.value != Value.Chester) {
                    round.leadColor = Some(card.color)
                }
                trick = trick :+ (player, card)
                firstPlayerIndex += 1
            }

            for (j <- firstPlayerIndex until players.length) {
                val player = players(j)
                val card = playerLogic.playCard(round.leadColor, round.trump, j, player)
                trick = trick :+ (player, card)
            }

            val winner = trickwinner(trick, round)
            notifyObservers("trick winner", winner)
            winner.roundTricks += 1

            trick.foreach { case (player, _) =>
                if (!player.hand.isEmpty) {
                    notifyObservers("ShowHand", ShowHand(player))
                }
            }
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

    def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
        // 1) If any Wizard cards are played, the first Wizard wins the trick.
        trick.find { case (_, card) => card.value == Value.WizardKarte } match {
            case Some((player, _)) => return player
            case None => ()
        }

        // 2) Consider trump (if any), ignoring Jesters (Chester) for determining winners
        val trumpColorOpt = round.trump
        trumpColorOpt match {
            case Some(trumpColor) =>
                val trumpCards = trick.filter { case (_, card) => card.color == trumpColor && card.value != Value.Chester }
                if (trumpCards.nonEmpty) {
                    return trumpCards.maxBy(_._2.value.ordinal)._1
                }
            case None => ()
        }

        // 3) Fall back to lead color. Lead color is tracked in round.leadColor and is set to the first non-special (non-Wizard, non-Chester) card played.
        val leadColorOpt: Option[Color] = round.leadColor.orElse {
            trick.collectFirst { case (_, c) if c.value != Value.WizardKarte && c.value != Value.Chester => c.color }
        }

        leadColorOpt match {
            case Some(leadColor) =>
                val leadColorCards = trick.filter { case (_, card) => card.color == leadColor && card.value != Value.Chester }
                if (leadColorCards.nonEmpty) {
                    return leadColorCards.maxBy(_._2.value.ordinal)._1
                }
            case None => ()
        }

        // 4) If we get here, there were no Wizards, no winning trump/lead cards; likely all Jesters were played.
        // According to Wizard rules, the first Jester wins in this scenario.
        trick.find { case (_, card) => card.value == Value.Chester } match {
            case Some((player, _)) => player
            case None =>
                // Fallback: should not happen, but return the first player's card holder to be safe
                trick.head._1
        }
    }
}

object RoundLogic {
  private val instance = new RoundLogic
  
  def playRound(currentround: Int, players: List[Player]): Unit =
    instance.playRound(currentround, players)

  def trickwinner(trick: List[(Player, Card)], round: Round): Player =
    instance.trickwinner(trick, round)
}