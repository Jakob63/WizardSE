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
  wizard.actionmanagement.Debug.enabled = false // hier für Debug Logs auf true setzen

    def isInteractive: Boolean = {
        val prop = sys.props.get("WIZARD_INTERACTIVE").exists(v => v != "0" && v.toLowerCase != "false")
        prop || (System.console() != null && sys.env.get("GITHUB_ACTIONS").isEmpty)
    }

    val injector: Injector = Guice.createInjector(new WizardModule)
    val fileIo: FileIOInterface = injector.getInstance(classOf[FileIOInterface])

    private val roundLogic = new RoundLogic
    roundLogic.gameLogic = Some(this)
    @volatile private var started: Boolean = false
    @volatile private var selectedPlayerCount: Option[Int] = None
    @volatile private var stopCurrentGame: Boolean = false
    @volatile var canSave: Boolean = false

    private var currentPlayers: List[Player] = Nil
    private var currentRoundNum: Int = 0
    var currentTrumpCard: Option[Card] = None
    var currentFirstPlayerIdx: Int = 0

    override def add(s: Observer): Unit = {
        super.add(s)
        roundLogic.add(s)
    }

    def setCanSave(value: Boolean): Unit = {
        canSave = value
    }

    def validGame(number: Int): Boolean = {
        number >= 3 && number <= 6
    }

    def start(): Unit = {
        if (started) { Debug.log("GameLogic.start called but already started; returning"); return }
        started = true
        selectedPlayerCount = None
        wizard.model.cards.Dealer.shuffleCards()
        Debug.log("GameLogic.start -> notifying StartGame and AskForPlayerCount")
        notifyObservers("StartGame", StartGame)
        notifyObservers("AskForPlayerCount", wizard.actionmanagement.AskForPlayerCount)
    }

    def playerCountSelected(count: Int): Unit = {
        Debug.log(s"GameLogic.playerCountSelected($count) called; current selected = $selectedPlayerCount")
        if (!validGame(count)) {
            Debug.log("GameLogic.playerCountSelected ignored (invalid count)")
            return
        }
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

    def resetPlayerCountSelection(): Unit = {
        if (selectedPlayerCount.isDefined) {
            Debug.log("GameLogic.resetPlayerCountSelection -> clearing previously selected player count")
            selectedPlayerCount = None
            notifyObservers("AskForPlayerCount", wizard.actionmanagement.AskForPlayerCount)
        } else {
            Debug.log("GameLogic.resetPlayerCountSelection ignored (no player count selected)")
        }
    }

    def setPlayers(players: List[Player]): Unit = {
        Debug.log(s"GameLogic.setPlayers(${players.map(_.name).mkString(",")}); computing rounds and starting game thread")

        import wizard.undo.{StartGameCommand, UndoService}
        UndoService.manager.doStep(new StartGameCommand(this, players))

        val rounds = if (players.nonEmpty) 60 / players.length else 0
        startGameThread(players, rounds)
    }

    def setPlayersFromRedo(players: List[Player]): Unit = {
        Debug.log(s"GameLogic.setPlayersFromRedo(${players.map(_.name).mkString(",")})")
        val rounds = if (players.nonEmpty) 60 / players.length else 0
        startGameThread(players, rounds)
    }


    def setPlayer(numPlayers: Int): Unit = {
        Debug.log(s"GameLogic.setPlayer(legacy) called with $numPlayers -> emitting AskForPlayerNames")
        notifyObservers("AskForPlayerNames", AskForPlayerNames)
    }
    
    def CardAuswahl(): Unit = {
        Debug.log("GameLogic.CardAuswahl -> notifying 'CardAuswahl'")
        notifyObservers("CardAuswahl", CardAuswahlEvent)
    }

    def undo(): Unit = {
        import wizard.undo.UndoService
        Debug.log("GameLogic.undo called")
        try { 
            wizard.actionmanagement.InputRouter.offer("__UNDO__")
            UndoService.manager.undoStep()
        } catch { case _: Throwable => () }
        notifyObservers("UndoPerformed")
    }

    def redo(): Unit = {
        import wizard.undo.UndoService
        Debug.log("GameLogic.redo called")
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
            val startRound = if (initialRound == 0) 1 else initialRound
            for (i <- startRound to rounds if !stopCurrentGame) {
                currentround = i
                currentRoundNum = i
                val round = new Round(players)
                Debug.log(s"GameLogic.playGame -> Round $currentround starting")

                val isResumed = i == initialRound && initialRound != 0
                roundLogic.playRound(currentround, players, isResumed, currentFirstPlayerIdx)
                currentTrumpCard = roundLogic.lastTrumpCard
                currentFirstPlayerIdx = currentround % players.length
            }
        } catch {
            case e: wizard.actionmanagement.GameStoppedException =>
                Debug.log(s"GameLogic.playGame caught GameStoppedException: ${e.getMessage}")
        }
        Debug.log(s"GameLogic.playGame completed (stopped=$stopCurrentGame)")
    }

    def save(title: String): Unit = {
        if (!canSave) {
            Debug.log("Save not allowed at this moment")
            notifyObservers("SaveNotAllowed", wizard.actionmanagement.SaveNotAllowed)
            return
        }
        val extension = if (fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO]) ".xml" else ".json"
        val filename = title + extension
        val game = Game(currentPlayers)
        game.rounds = if (currentPlayers.nonEmpty) 60 / currentPlayers.length else 0
        game.currentround = currentRoundNum
        game.currentTrick = roundLogic.currentTrickCards
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
                
                Thread.sleep(100)
                
                started = true
                canSave = roundNum != 0 && game.currentTrick.isEmpty

                if (game.currentTrick.isEmpty && game.players.forall(_.roundTricks == 0)) {
                    val allHaveBids = game.players.forall(_.roundBids >= 0)
                    if (allHaveBids) {
                        Debug.log("Resume at round start detected; resetting bids to trigger new bidding phase")
                        game.players.foreach(_.roundBids = -1)
                    }
                }

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
        val t = new Thread(() => playGame(players, rounds, startFromRound))
        t.setDaemon(true)
        t.start()
    }
}

object GameLogic {
  import wizard.model.player.Player

  def validGame(number: Int): Boolean = number >= 3 && number <= 6

  def isOver(game: wizard.model.Game): Boolean = game.rounds <= 0

  def playGame(game: wizard.model.Game, players: List[Player]): Unit = {
    game.rounds = game.rounds
  }
}