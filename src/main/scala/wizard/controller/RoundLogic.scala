package wizard.controller

import wizard.model.cards.{Card, Color, Dealer, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round
import wizard.actionmanagement.{CardsDealt, Observable, Observer, ShowHand, Debug}


case class PlayerSnapshot(name: String, roundBids: Int, points: Int)

class RoundLogic extends Observable {
    
    private val playerLogic = new PlayerLogic
    @volatile var stopGame: Boolean = false
    var lastTrumpCard: Option[Card] = None
    var currentTrickCards: List[Card] = Nil
    var currentFirstPlayerIdx: Int = 0
    var gameLogic: Option[GameLogic] = None

    override def add(s: Observer): Unit = {
        super.add(s)
        playerLogic.add(s)
    }
    
    def playRound(currentround: Int, players: List[Player], isResumed: Boolean = false, initialFirstPlayerIdx: Int = 0): Unit = {
        if (!isResumed) {
            players.foreach { player =>
                player.roundBids = 0
                player.roundTricks = 0
            }
        }
        gameLogic.foreach(_.setCanSave(true))
        val round = new Round(players)

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
        gameLogic.foreach(_.currentTrumpCard = trumpCardOpt)

        trumpCardOpt match {
            case Some(trumpCard) =>
                trumpCard.value match {
                    case Value.Chester => round.setState(new ChesterCardState)
                    case Value.WizardKarte => round.setState(new WizardCardState)
                    case _ => round.setState(new NormalCardState)
                }
            case None =>
                round.setTrump(None)
                round.setState(new NormalCardState)
        }

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
            notifyObservers("print trump card", trumpCard)
        }

        val biddingDone = isResumed && players.forall(_.roundBids >= 0)
        
        if (!biddingDone) {
            Debug.log(s"Starting bidding phase. isResumed=$isResumed, bids=${players.map(_.roundBids).mkString(",")}")
            var pIdx = 0
            while (pIdx < players.length && !stopGame) {
                val player = players(pIdx)
                if (isResumed && player.roundBids >= 0) {
                    Debug.log(s"Skipping bid for ${player.name} as it's already ${player.roundBids}")
                    pIdx += 1
                } else {
                    try {
                        playerLogic.bid(player)
                        gameLogic.foreach(_.setCanSave(false))
                        pIdx += 1
                    } catch {
                        case _: wizard.actionmanagement.InputRouter.UndoException =>
                            if (pIdx > 0) {
                                pIdx -= 1
                            } else {
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
            players.foreach(p => notifyObservers("ShowHand", ShowHand(p)))
        }

        val tricksPlayed = players.map(_.roundTricks).sum
        var resumedTrickInProgress = isResumed && currentTrickCards.nonEmpty

        var firstPlayerIdx = initialFirstPlayerIdx

        for (i <- tricksPlayed + 1 to currentround if !stopGame) {
            if (!resumedTrickInProgress) {
                round.leadColor = None
                currentFirstPlayerIdx = firstPlayerIdx
            }

            if (resumedTrickInProgress) {
                currentTrickCards.find(c => c.value != Value.WizardKarte && c.value != Value.Chester).foreach { firstNormalCard =>
                    round.leadColor = Some(firstNormalCard.color)
                }
                Debug.log(s"RoundLogic -> Emitting initial resumed trick state: ${currentTrickCards.size} cards")
                notifyObservers("TrickUpdated", currentTrickCards)
                Thread.sleep(500)
            }

            val trickPlayers = players.drop(firstPlayerIdx) ++ players.take(firstPlayerIdx)

            var trick = List[(Player, Card)]()
            var trickIdx = 0

            Debug.log(s"RoundLogic -> Starting trick $i. currentTrickCards was: ${currentTrickCards.size} cards. First player: ${trickPlayers.head.name}. resumedTrickInProgress: $resumedTrickInProgress")

            while (trickIdx < trickPlayers.length && !stopGame) {
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
                        gameLogic.foreach(_.setCanSave(false))
                        if (round.leadColor.isEmpty && card.value != Value.WizardKarte && card.value != Value.Chester) {
                            round.leadColor = Some(card.color)
                            Debug.log(s"RoundLogic -> leadColor set to ${card.color}")
                        }
                        trick = trick :+ (player, card)
                        currentTrickCards = trick.map(_._2)
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
                currentTrickCards = Nil
                notifyObservers("TrickUpdated", Nil)
                Thread.sleep(300)
                resumedTrickInProgress = false 

                firstPlayerIdx = players.indexOf(winner)
                currentFirstPlayerIdx = firstPlayerIdx
                gameLogic.foreach(_.currentFirstPlayerIdx = firstPlayerIdx)
                if (i == currentround) {
                    gameLogic.foreach(_.setCanSave(true))
                }
            }
        }

        players.foreach(player => {
            player.addTricks(player.roundTricks)
        })
        players.foreach(player => {
            playerLogic.addPoints(player)
        })
        val snapshots = players.map(p => PlayerSnapshot(p.name, p.roundBids, p.points))

        notifyObservers("points after round")
        notifyObservers("print points all players", snapshots)
    }

    def trickwinner(trick: List[(Player, Card)], round: Round): Player = {
        trick.find { case (_, card) => card.value == Value.WizardKarte } match {
            case Some((player, _)) => return player
            case None => ()
        }

        val trumpColorOpt = round.trump
        trumpColorOpt match {
            case Some(trumpColor) =>
                val trumpCards = trick.filter { case (_, card) => card.color == trumpColor && card.value != Value.Chester }
                if (trumpCards.nonEmpty) {
                    return trumpCards.maxBy(_._2.value.ordinal)._1
                }
            case None => ()
        }

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

        trick.find { case (_, card) => card.value == Value.Chester } match {
            case Some((player, _)) => player
            case None =>
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