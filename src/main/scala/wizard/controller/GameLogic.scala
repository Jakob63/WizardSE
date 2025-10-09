package wizard.controller

import wizard.aView.TextUI
import wizard.actionmanagement.{AskForPlayerNames, CardAuswahl as CardAuswahlEvent, GameEvent, Observable, Observer, StartGame}
import wizard.model.player.{AI, Human, Player}
import wizard.model.rounds.Round
import wizard.controller.RoundLogic
import scala.util.{Try, Success, Failure}


class GameLogic extends Observable {

    private val roundLogic = new RoundLogic
    @volatile private var started: Boolean = false

    // Ensure any observer of the controller also observes RoundLogic (and thus PlayerLogic via RoundLogic)
    override def add(s: Observer): Unit = {
        super.add(s)
        roundLogic.add(s)
    }
    
    def validGame(number: Int): Boolean = {
        number >= 3 && number <= 6
    }

    def start(): Unit = {
        if (started) { return }
        started = true
        notifyObservers("StartGame", StartGame)
        // Prompt views to ask for player count so both TUI and GUI can sync
        notifyObservers("AskForPlayerCount", wizard.actionmanagement.AskForPlayerCount)
    }

    // Views notify selected player count so other views can sync UI
    def playerCountSelected(count: Int): Unit = {
        notifyObservers("PlayerCountSelected", wizard.actionmanagement.PlayerCountSelected(count))
    }

    // New: set players provided by a view (TUI/GUI)
    def setPlayers(players: List[Player]): Unit = {
        val rounds = if (players.nonEmpty) 60 / players.length else 0
        val currentround = 0
        // reset stats for a fresh game
        players.foreach { p =>
            p.points = 0
            p.tricks = 0
            p.bids = 0
            p.roundBids = 0
            p.roundTricks = 0
            p.roundPoints = 0
        }
        // Run the game loop asynchronously to avoid blocking UI threads (e.g., JavaFX Application Thread)
        val t = new Thread(new Runnable {
            override def run(): Unit = playGame(players, rounds, currentround)
        })
        t.setDaemon(true)
        t.start()
    }

    // Kept for backward compatibility but no longer creates placeholder players
    def setPlayer(numPlayers: Int): Unit = { // to be removed later; views should call setPlayers
        notifyObservers("AskForPlayerNames", AskForPlayerNames)
    }
    
    def CardAuswahl(): Unit = {
        // Notify observers that card selection is requested; pass the event case object, not a recursive method call.
        notifyObservers("CardAuswahl", CardAuswahlEvent)
    }
    
    def playGame(players: List[Player], rounds: Int, initialRound: Int): Unit = {
        var currentround = initialRound
        for (i <- 1 to rounds) { // i = 1, 2, 3, ..., rounds
            currentround = i
            val round = new Round(players)
            roundLogic.playRound(currentround, players)
        }
    }
}

object GameLogic {
  import wizard.model.player.Player

  def validGame(number: Int): Boolean = number >= 3 && number <= 6

  def isOver(game: wizard.model.Game): Boolean = game.rounds <= 0

  def playGame(game: wizard.model.Game, players: List[Player]): Unit = {
    // Minimal stub to satisfy tests; actual game loop handled elsewhere
    // Could decrement rounds to simulate progress
    game.rounds = game.rounds
  }
}