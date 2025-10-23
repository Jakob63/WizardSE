package wizard.controller.controllerBaseImpl

import wizard.actionmanagement.Observable
import wizard.controller.{aGameLogic, aRoundLogic}
import wizard.model.player.Player
import wizard.model.rounds.{Game, Round}
import wizard.model.cards.{Card, Dealer}

class BaseGameLogic extends Observable with aGameLogic{

  // Logics
  var roundLogic: aRoundLogic = _
  
  var varchoice: Option[Int] = None
  var lastplayer: Option[List[Player]] = None
  var trumpcard: Option[Card] = None
  var trickCards: Option[List[Card]] = None

  override def startGame() = {
    notifyObservers("main menu")
  }
  
  override def handleChoice(choice: Int) = {
    varchoice = Some(choice)
    if (choice == 2) {
      notifyObservers("main menu exit")
      System.exit(0)
    } else if (choice != 1) {
      notifyObservers("main menu wrong input")
      notifyObservers("main menu")
    } else {
      println("Starting the game...")
      askPlayerNumber()
    }
  }

  override def enterPlayerNumber(playernumber: Int, current: Int, list: List[Player]): Unit = {
    notifyObservers("player names", playernumber, current, list)
  }

  override def askPlayerNumber(): Unit = {
    notifyObservers("input players")
  }

  override def createGame(players: List[Player]) = {
    val game = Game(players)
    notifyObservers("game started")
    playGame(game, players)
  }

  override def createPlayers(numPlayers: Int, current: Int = 0, players: List[Player] = List()) = {
    if (current < numPlayers) {
      notifyObservers("player names", numPlayers, current, players)
    } else {
      createGame(players)
    }

  }

  override def validGame(number: Int): Boolean = {
    number >= 3 && number <= 6
  }

  override def playGame(game: Game, players: List[Player]): Unit = {
    for (i <- 1 to game.rounds) { // i = 1, 2, 3, ..., rounds
      game.currentround = i
      val round = new Round(players)
      roundLogic.playRound(game.currentround, players)
    }
  }

  // game is over if all rounds are played
  override def isOver(game: Game): Boolean = {
    game.rounds == 0
  }
  
  override def playersHands(players: List[Player]): Unit = {
    lastplayer = Some(players)
  }
  override def trumpCard(card: Card): Unit = {
      trumpcard = Some(card)
  }

  override def trickCardsList(playedCard: Card): Unit = {
    if (trickCards.isEmpty) {
      trickCards = Some(List(playedCard))
    } else {
      val updatedTrickCards = trickCards.get :+ playedCard
      trickCards = Some(updatedTrickCards)
    }
  }
  override def resetTrickCards(): Unit = {
    trickCards = None
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
    trumpcard = Some(trumpCard)
    notifyObservers("print trump card", trumpCard)

    players.foreach { player =>
      val hand = Dealer.dealCards(currentround, Some(trumpCard))
      player.addHand(hand)
    }
    playersHands(players)

    // Determine the starting player index for this round (rotates each round)
    val startIdx = (currentround - 1) % players.length
    val orderPlayers: List[Player] = players.drop(startIdx) ++ players.take(startIdx)

    // Bidding phase: each player (starting from rotating start) sees only their own hand and bids
    roundLogic.playersTurn(currentround, players, startIdx)

    // Playing phase: play tricks
    for (_ <- 1 to currentround) {
      val winner = roundLogic.playTrick(orderPlayers, round)
      notifyObservers("trick winner", winner)
      winner.roundTricks += 1
    }

    players.foreach(player => {
      player.addTricks(player.roundTricks)
    })
    notifyObservers("points after round")
    notifyObservers("print points all players", players)
  }

  override def getChoice: Option[Int] = varchoice
  override def getPlayer: Option[List[Player]] = lastplayer
  override def getTrumpCard: Option[Card] = trumpcard
  override def getTrickCards: Option[List[Card]] = trickCards
}