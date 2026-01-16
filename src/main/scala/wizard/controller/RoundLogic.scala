package wizard.controller

import wizard.aView.TextUI
import wizard.controller.PlayerLogic
import wizard.model.cards.{Card, Color, Dealer, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round
import wizard.actionmanagement.{CardsDealt, Observable, Observer, ShowHand, Debug}


class RoundLogic extends Observable {
    
    private val playerLogic = new PlayerLogic
    @volatile var stopGame: Boolean = false
    var lastTrumpCard: Option[Card] = None
    var currentTrickCards: List[Card] = Nil
    var currentFirstPlayerIdx: Int = 0

    // Make sure observers added to RoundLogic also observe PlayerLogic events
    override def add(s: Observer): Unit = {
        super.add(s)
        playerLogic.add(s)
    }
    
    def playRound(currentround: Int, players: List[Player], isResumed: Boolean = false, initialFirstPlayerIdx: Int = 0): Unit = {
        val round = new Round(players)
        
        // Trumpfkarte bestimmen oder geladene verwenden
        val trumpCardOpt: Option[Card] = if (isResumed && lastTrumpCard.isDefined) {
            lastTrumpCard
        } else {
            val trumpCardIndex = currentround * players.length
            if (trumpCardIndex < Dealer.allCards.length) {
                val card = Dealer.allCards(trumpCardIndex)
                Debug.log(s"RoundLogic.playRound -> Round $currentround, trump index $trumpCardIndex, trump card: $card")
                Some(card)
            } else {
                Debug.log(s"RoundLogic.playRound -> Round $currentround, no trump card left at index $trumpCardIndex")
                None
            }
        }
        lastTrumpCard = trumpCardOpt

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

        // Nur mischen und austeilen, wenn wir nicht resumieren oder keine Karten da sind
        val playersHaveCards = players.exists(_.hand.cards.nonEmpty)
        if (!isResumed || !playersHaveCards) {
            Dealer.shuffleCards()
            players.foreach { player =>
                val hand = Dealer.dealCards(currentround, trumpCardOpt)
                player.addHand(hand)
            }
        }
        
        notifyObservers("CardsDealt", CardsDealt(players))

        trumpCardOpt.foreach { trumpCard =>
            round.handleTrump(trumpCard, players)
            // Ensure trump is displayed to observers (TUI/GUI) before bidding
            notifyObservers("print trump card", trumpCard)
        }

        // Bietphase: überspringen falls resumt und alle haben schon geboten
        // (Wir nehmen an, wenn die Summe der roundTricks + verbleibende Karten == currentround, 
        //  dann ist die Bietphase schon vorbei. Oder einfacher: wenn roundBids != 0)
        // Aber Vorsicht: ein Bid von 0 ist valide.
        // Wir prüfen, ob wir resumieren und ob die Spieler Karten auf der Hand haben, die zu den bereits gemachten Tricks passen.
        val totalTricksPossible = currentround
        val currentTotalTricks = players.map(_.roundTricks).sum
        val cardsInHands = players.map(_.hand.cards.size).sum
        
        val biddingDone = isResumed && (currentTotalTricks + (cardsInHands / (if(players.isEmpty) 1 else players.length)) <= totalTricksPossible) && players.exists(_.roundBids != 0)
        // Wenn wir resumieren und Hände haben, aber keine Bids... dann müssen wir wohl bieten.
        
        if (!biddingDone) {
            var pIdx = 0
            while (pIdx < players.length && !stopGame) {
                val player = players(pIdx)
                // If resuming, only bid if not already bid (checking roundBids != 0 is risky if someone bid 0)
                if (isResumed && player.roundBids != 0) {
                    pIdx += 1
                } else {
                    try {
                        playerLogic.bid(player)
                        pIdx += 1
                    } catch {
                        case _: wizard.actionmanagement.InputRouter.UndoException =>
                            if (pIdx > 0) {
                                pIdx -= 1
                            } else {
                                // Undo at the very first bid: jump back to naming
                                throw new wizard.actionmanagement.GameStoppedException("Undo to naming")
                            }
                        case _: wizard.actionmanagement.InputRouter.RedoException =>
                            if (pIdx < players.length - 1) pIdx += 1
                            notifyObservers("RedoPerformed")
                    }
                }
            }
        } else {
            Debug.log("Bidding phase skipped during resume")
            // Wir müssen trotzdem die Hands anzeigen
            players.foreach(p => notifyObservers("ShowHand", ShowHand(p)))
        }

        // Startpunkt für die Tricks bestimmen.
        // Falls wir resumieren, haben wir eventuell schon Tricks gespielt in dieser Runde.
        val tricksPlayed = currentTotalTricks
        var resumedTrickInProgress = isResumed && currentTrickCards.nonEmpty
        
        // Der erste Spieler der Runde ist (currentround - 1) % players.length
        // Wir nutzen den von GameLogic übergebenen initialFirstPlayerIdx, da dieser die Dealer-Rotation korrekt abbildet
        var firstPlayerIdx = initialFirstPlayerIdx

        for (i <- tricksPlayed + 1 to currentround if !stopGame) {
            if (!resumedTrickInProgress) {
                round.leadColor = None
                currentFirstPlayerIdx = firstPlayerIdx // Speichere wer den Trick beginnt
            }
            
            // Falls wir mitten im Trick resumieren, berechnen wir die leadColor aus den bereits gespielten Karten
            if (resumedTrickInProgress) {
                currentTrickCards.find(c => c.value != Value.WizardKarte && c.value != Value.Chester).foreach { firstNormalCard =>
                    round.leadColor = Some(firstNormalCard.color)
                }
                // Informiere GUI über den aktuellen Trickzustand
                Debug.log(s"RoundLogic -> Emitting initial resumed trick state: ${currentTrickCards.size} cards")
                notifyObservers("TrickUpdated", currentTrickCards)
                Thread.sleep(500) // GUI Zeit geben zum Rendern des geladenen Stands
            }

            // Die Spielerliste für diesen Trick rotieren, basierend auf firstPlayerIdx
            val trickPlayers = players.drop(firstPlayerIdx) ++ players.take(firstPlayerIdx)

            var trick = List[(Player, Card)]()
            var trickIdx = 0
            
            // Debug: log trick state
            Debug.log(s"RoundLogic -> Starting trick $i. currentTrickCards was: ${currentTrickCards.size} cards. First player: ${trickPlayers.head.name}. resumedTrickInProgress: $resumedTrickInProgress")

            while (trickIdx < trickPlayers.length && !stopGame) {
                // Falls wir mitten im Trick resumieren, überspringen wir bereits gespielte Karten
                if (resumedTrickInProgress && trickIdx < currentTrickCards.size) {
                    val resumedPlayer = trickPlayers(trickIdx)
                    val resumedCard = currentTrickCards(trickIdx)
                    Debug.log(s"RoundLogic -> resuming card at trickIdx $trickIdx for ${resumedPlayer.name}: $resumedCard")
                    trick = trick :+ (resumedPlayer, resumedCard)
                    trickIdx += 1
                } else {
                    val player = trickPlayers(trickIdx)
                    Debug.log(s"RoundLogic -> Waiting for card from ${player.name} at trickIdx $trickIdx")
                    try {
                        val card = playerLogic.playCard(round.leadColor, round.trump, trickIdx, player)
                        if (round.leadColor.isEmpty && card.value != Value.WizardKarte && card.value != Value.Chester) {
                            round.leadColor = Some(card.color)
                            Debug.log(s"RoundLogic -> leadColor set to ${card.color}")
                        }
                        trick = trick :+ (player, card)
                        currentTrickCards = trick.map(_._2) // Update für Save
                        Debug.log(s"RoundLogic -> card played by ${player.name}: $card. Trick now has ${currentTrickCards.size} cards")
                        notifyObservers("TrickUpdated", currentTrickCards)
                        Thread.sleep(500)
                        trickIdx += 1
                    } catch {
                        case _: wizard.actionmanagement.InputRouter.UndoException =>
                            if (trickIdx > 0) {
                                if (trick.nonEmpty) {
                                    val removedCard = trick.last._2
                                    trick = trick.dropRight(1)
                                    currentTrickCards = trick.map(_._2)
                                    if (round.leadColor.contains(removedCard.color)) {
                                         if (!trick.exists(t => t._2.value != Value.WizardKarte && t._2.value != Value.Chester)) {
                                             round.leadColor = None
                                         }
                                    }
                                }
                                trickIdx -= 1
                                notifyObservers("TrickUpdated", trick.map(_._2))
                            }
                        case _: wizard.actionmanagement.InputRouter.RedoException =>
                             if (trickIdx < trickPlayers.length - 1) {
                                 trickIdx += 1
                             }
                             notifyObservers("RedoPerformed")
                    }
                }
            }
            if (!stopGame) {
                val winner = trickwinner(trick, round)
                Debug.log(s"RoundLogic -> trick winner: ${winner.name}")
                notifyObservers("trick winner", winner)
                Thread.sleep(800)
                winner.roundTricks += 1
                currentTrickCards = Nil // Trick beendet
                notifyObservers("TrickUpdated", Nil)
                Thread.sleep(300)
                resumedTrickInProgress = false 
                
                // Der Gewinner des Tricks ist der neue firstPlayerIdx
                firstPlayerIdx = players.indexOf(winner)
                currentFirstPlayerIdx = firstPlayerIdx
                // Wichtig: für das nächste Save den firstPlayerIdx im GameLogic-Kontext aktuell halten?
                // Der firstPlayerIdx für den NÄCHSTEN Trick innerhalb dieser Runde ist der winner.
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
  
  def playRound(currentround: Int, players: List[Player], isResumed: Boolean = false, initialFirstPlayerIdx: Int = 0): Unit =
    instance.playRound(currentround, players, isResumed, initialFirstPlayerIdx)

  def trickwinner(trick: List[(Player, Card)], round: Round): Player =
    instance.trickwinner(trick, round)
}