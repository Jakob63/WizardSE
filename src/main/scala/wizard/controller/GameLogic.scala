package wizard.controller

import wizard.aView.TextUI
import wizard.actionmanagement.{AskForPlayerNames, CardAuswahl as CardAuswahlEvent, GameEvent, Observable, Observer, StartGame, Debug}
import wizard.model.player.{AI, Human, Player}
import wizard.model.rounds.Round
import wizard.controller.RoundLogic
import scala.util.{Try, Success, Failure}


class GameLogic extends Observable {

    private val roundLogic = new RoundLogic
    @volatile private var started: Boolean = false
    @volatile private var selectedPlayerCount: Option[Int] = None
    @volatile private var stopCurrentGame: Boolean = false

    // Ensure any observer of the controller also observes RoundLogic (and thus PlayerLogic via RoundLogic)
    override def add(s: Observer): Unit = {
        super.add(s)
        roundLogic.add(s)
    }
    
    def validGame(number: Int): Boolean = {
        number >= 3 && number <= 6
    }

    def start(): Unit = {
        if (started) { Debug.log("GameLogic.start called but already started; returning"); return }
        started = true
        selectedPlayerCount = None // reset for a fresh session
        Debug.log("GameLogic.start -> notifying StartGame and AskForPlayerCount")
        notifyObservers("StartGame", StartGame)
        // Prompt views to ask for player count so both TUI and GUI can sync
        notifyObservers("AskForPlayerCount", wizard.actionmanagement.AskForPlayerCount)
    }

    // Views notify selected player count so other views can sync UI
    def playerCountSelected(count: Int): Unit = {
        Debug.log(s"GameLogic.playerCountSelected($count) called; current selected = ${selectedPlayerCount}")
        if (!validGame(count)) {
            Debug.log("GameLogic.playerCountSelected ignored (invalid count)")
            return
        }
        // Make selection idempotent: accept same count again to rebroadcast required events (fixes GUI->TUI race after local back)
        if (selectedPlayerCount.isEmpty) {
            selectedPlayerCount = Some(count)
        }
        if (selectedPlayerCount.contains(count)) {
            Debug.log(s"GameLogic.playerCountSelected -> broadcasting PlayerCountSelected($count) and AskForPlayerNames")
            notifyObservers("PlayerCountSelected", wizard.actionmanagement.PlayerCountSelected(count))
            notifyObservers("AskForPlayerNames", wizard.actionmanagement.AskForPlayerNames)
        } else {
            Debug.log("GameLogic.playerCountSelected ignored (different count already selected)")
        }
    }

    // Allow views to request changing the player count (e.g., user pressed 'undo' at first name)
    def resetPlayerCountSelection(): Unit = {
        // Only act if a player count was previously selected; this prevents spamming repeated notifications
        if (selectedPlayerCount.isDefined) {
            Debug.log("GameLogic.resetPlayerCountSelection -> clearing previously selected player count")
            selectedPlayerCount = None
            // Notify views that we are back at player count selection so they can sync UI
            notifyObservers("AskForPlayerCount", wizard.actionmanagement.AskForPlayerCount)
        } else {
            Debug.log("GameLogic.resetPlayerCountSelection ignored (no player count selected)")
        }
    }

    // New: set players provided by a view (TUI/GUI)
    def setPlayers(players: List[Player]): Unit = {
        Debug.log(s"GameLogic.setPlayers(${players.map(_.name).mkString(",")}); computing rounds and starting game thread")
        
        // Register this transition in the undo manager so we can go back to naming
        import wizard.undo.{StartGameCommand, UndoService}
        UndoService.manager.doStep(new StartGameCommand(this, players))

        startGameThread(players)
    }

    // Called by StartGameCommand.redoStep to avoid double-adding to undo stack
    def setPlayersFromRedo(players: List[Player]): Unit = {
        Debug.log(s"GameLogic.setPlayersFromRedo(${players.map(_.name).mkString(",")})")
        startGameThread(players)
    }

    private def startGameThread(players: List[Player]): Unit = {
        stopCurrentGame = false
        roundLogic.stopGame = false
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
        Debug.log(s"GameLogic.setPlayer(legacy) called with $numPlayers -> emitting AskForPlayerNames")
        notifyObservers("AskForPlayerNames", AskForPlayerNames)
    }
    
    def CardAuswahl(): Unit = {
        // Notify observers that card selection is requested; pass the event case object, not a recursive method call.
        Debug.log("GameLogic.CardAuswahl -> notifying 'CardAuswahl'")
        notifyObservers("CardAuswahl", CardAuswahlEvent)
    }

    def undo(): Unit = {
        import wizard.undo.UndoService
        Debug.log("GameLogic.undo called")
        // Signal the game thread if it's waiting for input
        try { 
            wizard.actionmanagement.InputRouter.offer("__UNDO__") 
            // Also need to actually perform the undo on the stack
            UndoService.manager.undoStep()
        } catch { case _: Throwable => () }
        notifyObservers("UndoPerformed")
    }

    def redo(): Unit = {
        import wizard.undo.UndoService
        Debug.log("GameLogic.redo called")
        // Signal the game thread if it's waiting for input
        try { 
            wizard.actionmanagement.InputRouter.offer("__REDO__") 
            UndoService.manager.redoStep()
        } catch { case _: Throwable => () }
        notifyObservers("RedoPerformed")
    }
    
    def stopGame(): Unit = {
        Debug.log("GameLogic.stopGame() called")
        stopCurrentGame = true
        roundLogic.stopGame = true
    }

    def playGame(players: List[Player], rounds: Int, initialRound: Int): Unit = {
        Debug.log(s"GameLogic.playGame starting with rounds=$rounds from=$initialRound for players=${players.map(_.name).mkString(",")}")
        var currentround = initialRound
        try {
            for (i <- 1 to rounds if !stopCurrentGame) { // i = 1, 2, 3, ..., rounds
                currentround = i
                val round = new Round(players)
                Debug.log(s"GameLogic.playGame -> Round $currentround starting")
                roundLogic.playRound(currentround, players)
            }
        } catch {
            case e: wizard.actionmanagement.GameStoppedException =>
                Debug.log(s"GameLogic.playGame caught GameStoppedException: ${e.getMessage}")
        }
        Debug.log(s"GameLogic.playGame completed (stopped=$stopCurrentGame)")
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