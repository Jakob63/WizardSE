package wizard.aView

import wizard.actionmanagement.{CardsDealt, Observer, Debug, InputRouter}
import wizard.model.cards.*
import wizard.model.player.PlayerType.Human
import wizard.model.player.{Player, PlayerFactory}
import wizard.undo.{SetPlayerNameCommand, UndoManager}
import wizard.controller.GameLogic

import scala.util.{Success, Try}

object TextUI {
    val eol: String = sys.props.getOrElse("line.separator", "\n")

    def update(updateMSG: String, obj: Any*): Any = {
        Debug.log(s"TextUI.update('$updateMSG') called")
        updateMSG match {
            case "which card" => println(s"${obj.head.asInstanceOf[Player].name}, which card do you want to play?")
            case "invalid card" => println("Invalid card. Please enter a valid index.")
            case "follow lead" => println(s"You must follow the lead suit ${obj.head.asInstanceOf[Color].toString}.")
            case "which bid" => println(s"${obj.head.asInstanceOf[Player].name}, how many tricks do you bid?")
            case "invalid input, bid again" => println("Invalid input. Please enter a valid number.")
            case "print trump card" => println(s"Trump card: \n${showcard(obj.head.asInstanceOf[Card])}")
            case "cards dealt" => println("Cards have been dealt to all players.")
            case "trick winner" => println(s"${obj.head.asInstanceOf[Player].name} won the trick.")
            case "points after round" => println("Points after this round:")
            case _ => ()
        }
    }

    def inputPlayers(): List[Player] = {
        // Outer loop allows restarting the whole input if user types 'undo' at the first player's name
        var finished = false
        var result: List[Player] = List()
        while (!finished) {
            // 1) Ask for number of players
            var numPlayers = -1
            while (numPlayers < 3 || numPlayers > 6) {
                print("Enter the number of players (3-6): ")
                val input = scala.io.StdIn.readLine()
                numPlayers =
                  if (input == null || input.trim.isEmpty) {
                    println("Invalid number of players. Please enter a number between 3 and 6.")
                    -1
                  } else if (input == "undo" || input == "redo") {
                    // Treat undo/redo as no-ops at the count prompt (do not print error)
                    -1
                  } else {
                    Try(input.toInt) match {
                      case Success(number) if number >= 3 && number <= 6 => number
                      case _ =>
                        println("Invalid number of players. Please enter a number between 3 and 6.")
                        -1
                    }
                  }
            }

            // 2) Ask for player names (with undo/redo). If user types 'undo' at player 1, go back to step 1.
            var players = List[Player]()
            var i = 1
            val undoManager = new UndoManager
            var backToCount = false
            while (i <= numPlayers && !backToCount) {
                var name: Option[String] = None
                val pattern = "^[a-zA-Z0-9]+$".r
                while (!backToCount && (name.isEmpty || !pattern.pattern.matcher(name.getOrElse("")).matches())) {
                    print(s"Enter the name of player $i (or type 'undo'/'redo'): ")
                    val input = scala.io.StdIn.readLine()
                    input match {
                        case "undo" =>
                            if (i > 1) {
                                undoManager.undoStep()
                                i -= 1
                                players = players.dropRight(1)
                            } else {
                                // User wants to correct the player count
                                backToCount = true
                            }
                        case "redo" =>
                            undoManager.redoStep()
                            if (i <= players.length) {
                                players = players :+ players(i - 1)
                            }
                            if (i < numPlayers) {
                                i += 1
                            }
                        case _ =>
                            if (input == "" || !pattern.pattern.matcher(input).matches()) {
                                println("Invalid name. Please enter a name containing only letters and numbers.")
                            } else {
                                name = Some(input)
                                val player = PlayerFactory.createPlayer(name, Human)
                                undoManager.doStep(new SetPlayerNameCommand(player, input))
                                players = players :+ player
                                i += 1
                            }
                    }
                }
            }

            if (backToCount) {
                // Restart the entire input process to change player count
                () // simply continue while(!finished)
            } else {
                result = players
                finished = true
            }
        }
        result
    }

    def showHand(player: Player): Unit = {
        // Show numeric representation like "7 of Red" as required by tests
        val numericHand = player.hand.cards.map(c => s"${c.value.cardType()} of ${c.color}")
        println(s"${player.name}'s hand: ${numericHand.mkString(", ")}")
        if (player.hand.cards.isEmpty) {
            println("No cards in hand.")
        } else {
            val cardLines = player.hand.cards.map(card => showcard(card).split("\n"))
            for (i <- cardLines.head.indices) {
                println(cardLines.map(_(i)).mkString(" "))
            }
            val handString = player.hand.cards.map(_.toString).mkString(", ")
            println(s"($handString)")
            val indices = player.hand.cards.zipWithIndex.map { case (card, index) => s"${index + 1}: ${card.toString}" }
            println(s"Indices: ${indices.mkString(", ")}")
        }
    }

    def showcard(card: Card): String = {
        if (card.value == Value.Ten || card.value == Value.Eleven || card.value == Value.Twelve || card.value == Value.Thirteen) {
            s"┌─────────┐\n" +
                s"│ ${colorToAnsi(card.color)}${valueToAnsi(card.value)}${card.value.cardType()}${Console.RESET}      │\n" +
                s"│         │\n" +
                s"│         │\n" +
                s"│         │\n" +
                s"│      ${colorToAnsi(card.color)}${valueToAnsi(card.value)}${card.value.cardType()}${Console.RESET} │\n" +
                s"└─────────┘"

        } else {
            s"┌─────────┐\n" +
                s"│ ${colorToAnsi(card.color)}${valueToAnsi(card.value)}${card.value.cardType()}${Console.RESET}       │\n" +
                s"│         │\n" +
                s"│         │\n" +
                s"│         │\n" +
                s"│       ${colorToAnsi(card.color)}${valueToAnsi(card.value)}${card.value.cardType()}${Console.RESET} │\n" +
                s"└─────────┘"
        }
    }

    def printCardAtIndex(index: Int): String = {
        if (index >= 0 && index < Dealer.allCards.length) {
            showcard(Dealer.allCards(index))
        } else {
            s"Index $index is out of bounds."
        }
    }
}

// object zu class geändert
class TextUI(GameController: GameLogic) extends Observer {

    GameController.add(this)
    private val undoManager = new UndoManager
    @volatile private var phase: String = "Idle" // Idle, AwaitPlayerCount, AwaitPlayerNames, InGame
    @volatile private var lastSelectedCount: Int = 0
    @volatile private var countReaderStarted: Boolean = false
    @volatile private var cancelCountReader: Boolean = false
    @volatile private var skipNextAskForPlayerCountReader: Boolean = false
    // New: cooperative cancellation for player-name input and a state flag
    @volatile private var nameReaderStarted: Boolean = false
    @volatile private var cancelNameReader: Boolean = false
    
    // --- Test helpers (package-private) ---
    private[aView] def testPhase: String = phase
    private[aView] def testSetPhase(p: String): Unit = { phase = p }
    private[aView] def testSetNameReaderStarted(v: Boolean): Unit = { nameReaderStarted = v }
    private[aView] def testIsCountReaderStarted: Boolean = countReaderStarted
    private[aView] def testIsNameReaderStarted: Boolean = nameReaderStarted

    private def isInteractive: Boolean = {
        val prop = sys.props.get("WIZARD_INTERACTIVE").exists(v => v != "0" && v.toLowerCase != "false")
        prop || System.console() != null
    }

    override def update(updateMSG: String, obj: Any*): Any = this.synchronized {
        Debug.log(s"TextUI(class).update('$updateMSG') in phase=$phase")
        updateMSG match {
            case "StartGame" | "AskForPlayerCount" => {
                // If we are currently in name entry, cancel it and transition back to AwaitPlayerCount
                if (nameReaderStarted) {
                    cancelNameReader = true
                    // Unblock any pending InputRouter.readLine in name-entry thread
                    try { InputRouter.offer("__BACK_TO_COUNT__") } catch { case _: Throwable => () }
                }
                // Reset local selection snapshot; controller already cleared it
                lastSelectedCount = 0
                if (phase != "AwaitPlayerCount") phase = "AwaitPlayerCount"
                // Prompt for number of players without blocking the observer notification loop.
                if (isInteractive && (phase == "Idle" || phase == "AwaitPlayerCount")) {
                    if (skipNextAskForPlayerCountReader) {
                        // Consume this event without starting a background reader; a synchronous prompt will follow.
                        skipNextAskForPlayerCountReader = false
                    } else if (!countReaderStarted) {
                        countReaderStarted = true
                        cancelCountReader = false
                        // Spawn a background reader so GUI can still receive updates immediately.
                        val readerStarted = new Thread(new Runnable {
                            override def run(): Unit = {
                                var count = -1
                                while ((count < 3 || count > 6) && lastSelectedCount == 0 && !cancelCountReader) {
                                    print("Enter the number of players (3-6): ")
                                    val input = InputRouter.readLine()
                                    count =
                                      if (input == null || input.trim.isEmpty) {
                                        // No input provided (e.g., started from IDE without console focus).
                                        // Do not print an error; just keep waiting quietly.
                                        -1
                                      } else if (input == "undo" || input == "redo" || input == "__BACK_TO_COUNT__" || input == "__CANCEL_COUNT__") {
                                        // Ignore undo/redo and internal sentinels at count prompt; keep asking without error
                                        -1
                                      } else {
                                        scala.util.Try(input.toInt) match {
                                          case scala.util.Success(n) if n >= 3 && n <= 6 => n
                                          case _ =>
                                            println("Invalid number of players. Please enter a number between 3 and 6.")
                                            -1
                                          }
                                      }
                                }
                                // If GUI/TUI already set the count, skip; controller will ignore duplicates anyway.
                                try {
                                    if (!cancelCountReader && count >= 3 && count <= 6) {
                                        GameController.playerCountSelected(count)
                                    }
                                } finally {
                                    countReaderStarted = false
                                }
                            }
                        })
                        readerStarted.setDaemon(true)
                        readerStarted.start()
                    }
                }
            }
            case "PlayerCountSelected" => {
                // Record the selected count, and wait for AskForPlayerNames to actually read names to avoid blocking other observers.
                val count = obj.headOption match {
                    case Some(pcs: wizard.actionmanagement.PlayerCountSelected) => pcs.count
                    case Some(i: Int) => i
                    case _ => 0
                }
                if (count >= 3 && count <= 6) {
                    lastSelectedCount = count
                    phase = "AwaitPlayerNames"
                    // Ensure any background count reader thread stops and does not consume future inputs (like bids)
                    cancelCountReader = true
                    try { InputRouter.offer("__CANCEL_COUNT__") } catch { case _: Throwable => () }
                }
            }
            case "AskForPlayerNames" => {
                // Now prompt for player names using the last selected count. Doing it on this separate event avoids blocking the prior notification.
                val count = lastSelectedCount
                if (count >= 3 && count <= 6 && (phase == "AwaitPlayerNames" || phase == "InGame")) {
                    phase = "AwaitPlayerNames"
                    // Start name input on a background thread so we can cancel it if GUI requests going back
                    // Allow restart even if a previous name reader thread is still winding down after cancellation
                    if (isInteractive && (!nameReaderStarted || cancelNameReader)) {
                        nameReaderStarted = true
                        cancelNameReader = false
                        val t = new Thread(new Runnable {
                            override def run(): Unit = {
                                try {
                                    var players = List[Player]()
                                    var i = 1
                                    val pattern = "^[a-zA-Z0-9]+$".r
                                    var backToCount = false
                                    while (i <= count && !backToCount && !cancelNameReader) {
                                        print(s"Enter the name of player $i (or type 'undo'/'redo'): ")
                                        val input = InputRouter.readLine()
                                        if (cancelNameReader || input == "__BACK_TO_COUNT__") { backToCount = true }
                                        else input match {
                                            case s if s == null || s.isEmpty =>
                                                println("Invalid name. Please enter a name containing only letters and numbers.")
                                            case "undo" =>
                                                if (i > 1) {
                                                    try { undoManager.undoStep() } catch { case _: Throwable => () }
                                                    i -= 1
                                                    if (players.nonEmpty) players = players.dropRight(1)
                                                } else {
                                                    // Go back to player count selection (user-triggered)
                                                    lastSelectedCount = 0
                                                    phase = "AwaitPlayerCount"
                                                    countReaderStarted = false
                                                    cancelCountReader = true
                                                    skipNextAskForPlayerCountReader = true
                                                    try { GameController.resetPlayerCountSelection() } catch { case _: Throwable => () }
                                                    backToCount = true
                                                }
                                            case "redo" =>
                                                try { undoManager.redoStep() } catch { case _: Throwable => () }
                                            case other =>
                                                if (pattern.pattern.matcher(other).matches()) {
                                                    val player = PlayerFactory.createPlayer(Some(other), Human)
                                                    undoManager.doStep(new SetPlayerNameCommand(player, other))
                                                    players = players :+ player
                                                    i += 1
                                                } else {
                                                    println("Invalid name. Please enter a name containing only letters and numbers.")
                                                }
                                        }
                                    }
                                    if (!backToCount && !cancelNameReader) {
                                        GameController.setPlayers(players)
                                        phase = "InGame"
                                    }
                                } finally {
                                    nameReaderStarted = false
                                }
                            }
                        })
                        t.setDaemon(true)
                        t.start()
                    }
                }
            }
            case "ShowHand" => {
                obj.head match {
                    case sh: wizard.actionmanagement.ShowHand => TextUI.showHand(sh.player)
                    case p: Player => TextUI.showHand(p)
                    case _ => ()
                }
            }
            case "which card" => println(s"${obj.head.asInstanceOf[Player].name}, which card do you want to play?")
            case "invalid card" => println("Invalid card. Please enter a valid index.")
            case "follow lead" => println(s"You must follow the lead suit ${obj.head.asInstanceOf[Color].toString}.")
            case "which bid" => {
                // Ensure we are no longer in name entry when bidding starts
                phase = "InGame"
                if (nameReaderStarted) {
                    cancelNameReader = true
                    try { InputRouter.offer("__BACK_TO_COUNT__") } catch { case _: Throwable => () }
                }
                println(s"${obj.head.asInstanceOf[Player].name}, how many tricks do you bid?")
            }
            case "invalid input, bid again" => println("Invalid input. Please enter a valid number.")
            case "print trump card" => println(s"Trump card: \n${TextUI.showcard(obj.head.asInstanceOf[Card])}")
            case "CardsDealt" => {
                // Transition into game phase and ensure any name-entry prompt is stopped
                phase = "InGame"
                if (nameReaderStarted) {
                    cancelNameReader = true
                    try { InputRouter.offer("__BACK_TO_COUNT__") } catch { case _: Throwable => () }
                }
                // Print all players' hands first so that bidding happens after players saw their cards (TUI requirement)
                obj.headOption.collect { case cd: CardsDealt => cd.players }.foreach { players =>
                    players.foreach(player => TextUI.showHand(player))
                }
                println("Cards have been dealt to all players.")
            }
            case "trick winner" => println(s"${obj.head.asInstanceOf[Player].name} won the trick.")
            case "card played" => println(s"Played card: \n${TextUI.showcard(obj.head.asInstanceOf[Card])}")
            case "points after round" => println("Points after this round:")
            case "print points all players" => 
                val players = obj.head.asInstanceOf[List[Player]]
                val nameWidth = (players.map(_.name.length).maxOption.getOrElse(0) max "Name".length)
                val bidWidth = 5
                val pointWidth = 6
                
                val totalWidth = nameWidth + bidWidth + pointWidth + 10 // 3 separators + spaces
                val separator = "+" + "-" * (nameWidth + 2) + "+" + "-" * (bidWidth + 2) + "+" + "-" * (pointWidth + 2) + "+"
                
                println(separator)
                val headerFormat = "| %-" + nameWidth + "s | %-" + bidWidth + "s | %-" + pointWidth + "s |"
                println(headerFormat.format("Name", "Bids", "Points"))
                println(separator)
                players.foreach { player =>
                    println(headerFormat.format(player.name, player.roundBids.toString, player.points.toString))
                }
                println(separator)
            case "bid einlesen" => scala.io.StdIn.readLine()
            case "card einlesen" => scala.io.StdIn.readLine()
            case "which trump" => {
                println(s"${obj.head.asInstanceOf[Player].name}, which color do you want to choose as trump?")
                scala.io.StdIn.readLine()
            }
            case _ => ()
        }
    }

    def printColorOptions(cards: List[Card]): Unit = {
        val cardLines = cards.map(TextUI.showcard(_).split("\n"))
        for (i <- cardLines.head.indices) {
            println(cardLines.map(_(i)).mkString(" "))
        }
        val handString = cards.map(_.toString).mkString(", ")
        println(s"($handString)")
        val indices = cards.zipWithIndex.map { case (card, index) => s"${index + 1}: ${card.toString}" }
        println(s"Indices: ${indices.mkString(", ")}")
    }

    def undo(): Unit = {
        undoManager.undoStep()
    }

    def redo(): Unit = {
        undoManager.redoStep()
    }

}