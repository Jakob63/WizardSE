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
            case "invalid bid" => 
                val max = obj.head.asInstanceOf[Int]
                val playerMsg = if (obj.length > 1) s" for player ${obj(1).asInstanceOf[Player].name}" else ""
                println(s"Invalid bid$playerMsg. You can only bid between 0 and $max.")
            case "print trump card" => println(s"Trump card: \n${showcard(obj.head.asInstanceOf[Card])}")
            case "cards dealt" => println("Cards have been dealt to all players.")
            case "trick winner" => println(s"${obj.head.asInstanceOf[Player].name} won the trick.")
            case "points after round" => println("Points after this round:")
            case _ => ()
        }
    }

    def inputPlayers(): List[Player] = {
        var finished = false
        var result: List[Player] = List()
        while (!finished) {
            var numPlayers = -1
            while (numPlayers < 3 || numPlayers > 6) {
                print("Enter the number of players (3-6): ")
                val input = scala.io.StdIn.readLine()
                numPlayers =
                  if (input == null || input.trim.isEmpty) {
                    println("Invalid number of players. Please enter a number between 3 and 6.")
                    -1
                  } else if (input == "undo" || input == "redo") {
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
                ()
            } else {
                result = players
                finished = true
            }
        }
        result
    }

    def showHand(player: Player): Unit = {
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

class TextUI(GameController: GameLogic) extends Observer {

    GameController.add(this)
    private val undoManager = new UndoManager
    @volatile private var phase: String = "Idle"
    @volatile private var lastSelectedCount: Int = 0
    @volatile private var countReaderStarted: Boolean = false
    @volatile private var cancelCountReader: Boolean = false
    @volatile private var skipNextAskForPlayerCountReader: Boolean = false
    @volatile private var nameReaderStarted: Boolean = false
    @volatile private var cancelNameReader: Boolean = false
    
    private[aView] def testPhase: String = phase
    private[aView] def testSetPhase(p: String): Unit = { phase = p }
    private[aView] def testSetNameReaderStarted(v: Boolean): Unit = { nameReaderStarted = v }
    private[aView] def testIsCountReaderStarted: Boolean = countReaderStarted
    private[aView] def testIsNameReaderStarted: Boolean = nameReaderStarted
    private[aView] def testSetLastSelectedCount(c: Int): Unit = { lastSelectedCount = c }

    private def isInteractive: Boolean = {
        val prop = sys.props.get("WIZARD_INTERACTIVE").exists(v => v != "0" && v.toLowerCase != "false")
        prop || System.console() != null
    }

    override def update(updateMSG: String, obj: Any*): Any = this.synchronized {
        Debug.log(s"TextUI(class).update('$updateMSG') in phase=$phase")
        updateMSG match {
            case "StartGame" | "AskForPlayerCount" => {
                if (nameReaderStarted) {
                    cancelNameReader = true
                    try { InputRouter.offer("__BACK_TO_COUNT__") } catch { case _: Throwable => () }
                }
                lastSelectedCount = 0
                if (phase != "AwaitPlayerCount") phase = "AwaitPlayerCount"
                if (isInteractive && (phase == "Idle" || phase == "AwaitPlayerCount")) {
                    if (skipNextAskForPlayerCountReader) {
                        skipNextAskForPlayerCountReader = false
                    } else if (!countReaderStarted) {
                        countReaderStarted = true
                        cancelCountReader = false
                        val readerStarted = new Thread(new Runnable {
                            override def run(): Unit = {
                                var count = -1
                                while ((count < 3 || count > 6) && lastSelectedCount == 0 && !cancelCountReader) {
                                    print("Enter the number of players (3-6): ")
                                    val input = InputRouter.readLine()
                                    count =
                                      if (input == null || input.trim.isEmpty) {
                                        -1
                                      } else if (input == "undo" || input == "redo" || input == "__BACK_TO_COUNT__" || input == "__CANCEL_COUNT__") {
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
                val count = obj.headOption match {
                    case Some(pcs: wizard.actionmanagement.PlayerCountSelected) => pcs.count
                    case Some(i: Int) => i
                    case _ => 0
                }
                if (count >= 3 && count <= 6) {
                    lastSelectedCount = count
                    phase = "AwaitPlayerNames"
                    cancelCountReader = true
                    try { InputRouter.offer("__CANCEL_COUNT__") } catch { case _: Throwable => () }
                }
            }
            case "AskForPlayerNames" => {
                val count = lastSelectedCount
                if (count >= 3 && count <= 6 && (phase == "AwaitPlayerNames" || phase == "InGame")) {
                    phase = "AwaitPlayerNames"
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
                phase = "InGame"
                if (nameReaderStarted) {
                    cancelNameReader = true
                    try { InputRouter.offer("__BACK_TO_COUNT__") } catch { case _: Throwable => () }
                }
                println(s"${obj.head.asInstanceOf[Player].name}, how many tricks do you bid?")
            }
            case "invalid input, bid again" => println("Invalid input. Please enter a valid number.")
            case "invalid bid" => 
                val max = obj.head.asInstanceOf[Int]
                val playerMsg = if (obj.length > 1) s" for player ${obj(1).asInstanceOf[Player].name}" else ""
                println(s"Invalid bid$playerMsg. You can only bid between 0 and $max.")
            case "print trump card" => println(s"Trump card: \n${TextUI.showcard(obj.head.asInstanceOf[Card])}")
            case "CardsDealt" => {
                phase = "InGame"
                if (nameReaderStarted) {
                    cancelNameReader = true
                    try { InputRouter.offer("__BACK_TO_COUNT__") } catch { case _: Throwable => () }
                }
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
                
                val totalWidth = nameWidth + bidWidth + pointWidth + 10
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
            case "GameLoaded" => {
                val game = obj.head.asInstanceOf[wizard.model.Game]
                lastSelectedCount = game.players.length
                phase = "InGame"
                cancelCountReader = true
                cancelNameReader = true
                try { 
                    InputRouter.offer("__CANCEL_COUNT__")
                    InputRouter.offer("__BACK_TO_COUNT__")
                } catch { case _: Throwable => () }
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