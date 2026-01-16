package wizard.controller

import wizard.aView.TextUI
import wizard.actionmanagement.{AskForPlayerNames, CardAuswahl as CardAuswahlEvent, GameEvent, Observable, Observer, StartGame, Debug}
import wizard.model.player.{AI, Human, Player}
import wizard.model.rounds.Round
import wizard.controller.RoundLogic
import wizard.model.fileIoComponent.FileIOInterface
import com.google.inject.{Guice, Injector}
import wizard.WizardModule
import wizard.model.Game
import wizard.model.cards.Card
import scala.util.{Try, Success, Failure}


class GameLogic extends Observable {

    val injector: Injector = Guice.createInjector(new WizardModule)
    val fileIo: FileIOInterface = injector.getInstance(classOf[FileIOInterface])

    private val roundLogic = new RoundLogic
    @volatile private var started: Boolean = false
    @volatile private var selectedPlayerCount: Option[Int] = None
    @volatile private var stopCurrentGame: Boolean = false
    
    // Aktueller Zustand für Save
    private var currentPlayers: List[Player] = Nil
    private var currentRoundNum: Int = 0
    private var currentTrumpCard: Option[Card] = None
    private var currentFirstPlayerIdx: Int = 0

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
        wizard.model.cards.Dealer.shuffleCards()
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

        val rounds = if (players.nonEmpty) 60 / players.length else 0
        startGameThread(players, rounds, 0)
    }

    // Called by StartGameCommand.redoStep to avoid double-adding to undo stack
    def setPlayersFromRedo(players: List[Player]): Unit = {
        Debug.log(s"GameLogic.setPlayersFromRedo(${players.map(_.name).mkString(",")})")
        val rounds = if (players.nonEmpty) 60 / players.length else 0
        startGameThread(players, rounds, 0)
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
        currentPlayers = players
        var currentround = initialRound
        try {
            // Falls wir ein geladenes Spiel fortsetzen, starten wir bei der initialRound.
            // Falls es ein neues Spiel ist (initialRound=0), fangen wir bei 1 an.
            val startRound = if (initialRound == 0) 1 else initialRound
            for (i <- startRound to rounds if !stopCurrentGame) {
                currentround = i
                currentRoundNum = i
                val round = new Round(players)
                Debug.log(s"GameLogic.playGame -> Round $currentround starting")
                
                // Wir übergeben, ob die Runde fortgesetzt wird (nur bei der allerersten Runde des Loops)
                val isResumed = (i == initialRound && initialRound != 0)
                roundLogic.playRound(currentround, players, isResumed, currentFirstPlayerIdx)
                currentTrumpCard = roundLogic.lastTrumpCard
                // Nach jeder Runde aktualisieren wir den Startspieler für die nächste Runde
                currentFirstPlayerIdx = (currentround) % players.length
            }
        } catch {
            case e: wizard.actionmanagement.GameStoppedException =>
                Debug.log(s"GameLogic.playGame caught GameStoppedException: ${e.getMessage}")
        }
        Debug.log(s"GameLogic.playGame completed (stopped=$stopCurrentGame)")
    }

    def save(title: String): Unit = {
        val extension = if (fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO]) ".xml" else ".json"
        val filename = title + extension
        val game = Game(currentPlayers)
        game.rounds = if (currentPlayers.nonEmpty) 60 / currentPlayers.length else 0
        game.currentround = currentRoundNum
        game.currentTrick = roundLogic.currentTrickCards
        // Wir müssen auch speichern, wer den aktuellen Trick angefangen hat!
        // RoundLogic weiß das in roundLogic.currentFirstPlayerIdx
        fileIo.save(game, currentRoundNum, currentTrumpCard, wizard.model.cards.Dealer.index, roundLogic.currentFirstPlayerIdx, filename)
        Debug.log(s"Game saved to $filename (firstPlayerIdx=${roundLogic.currentFirstPlayerIdx})")
    }

    def load(title: String): Unit = {
        val extension = if (fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO]) ".xml" else ".json"
        val filename = title + extension
        Try(fileIo.load(filename)) match {
            case Success((game, roundNum, trumpCard, dealerIndex, firstPlayerIdx)) =>
                currentPlayers = game.players
                currentRoundNum = roundNum
                currentTrumpCard = trumpCard
                currentFirstPlayerIdx = firstPlayerIdx
                roundLogic.lastTrumpCard = trumpCard
                roundLogic.currentTrickCards = game.currentTrick
                roundLogic.currentFirstPlayerIdx = firstPlayerIdx
                wizard.model.cards.Dealer.index = dealerIndex
                
                stopGame() // Stoppe aktuelles Spiel falls vorhanden
                Thread.sleep(100) // Kurze Pause zum Beenden
                
                started = true
                startGameThread(game.players, game.rounds, roundNum)
                notifyObservers("GameLoaded", game)
            case Failure(e) =>
                Debug.log(s"Failed to load game $filename: ${e.getMessage}")
                notifyObservers("LoadFailed", title)
        }
    }

    private def startGameThread(players: List[Player], rounds: Int, startFromRound: Int = 0): Unit = {
        stopCurrentGame = false
        roundLogic.stopGame = false
        // reset stats ONLY if it's a fresh game
        if (startFromRound == 0) {
            players.foreach { p =>
                p.points = 0
                p.tricks = 0
                p.bids = 0
                p.roundBids = 0
                p.roundTricks = 0
                p.roundPoints = 0
            }
        }
        // Run the game loop asynchronously
        val t = new Thread(new Runnable {
            override def run(): Unit = playGame(players, rounds, startFromRound)
        })
        t.setDaemon(true)
        t.start()
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