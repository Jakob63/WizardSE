package wizard.aView

import wizard.actionmanagement.{CardsDealt, Observer}
import wizard.model.cards.*
import wizard.model.player.PlayerType.Human
import wizard.model.player.{Player, PlayerFactory}
import wizard.undo.{SetPlayerNameCommand, UndoManager}
import wizard.controller.GameLogic

import scala.util.{Success, Try}

object TextUI {
    val eol: String = sys.props.getOrElse("line.separator", "\n")

    def update(updateMSG: String, obj: Any*): Any = {
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
                numPlayers = Try(input.toInt) match {
                    case Success(number) if number >= 3 && number <= 6 => number
                    case _ =>
                        println("Invalid number of players. Please enter a number between 3 and 6.")
                        -1
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

    override def update(updateMSG: String, obj: Any*): Any = this.synchronized {
        updateMSG match {
            case "StartGame" | "AskForPlayerCount" => {
                // Avoid starting a blocking prompt that can race with the GUI. Just set phase and wait.
                if (phase == "Idle" || phase == "AwaitPlayerCount") {
                    phase = "AwaitPlayerCount"
                } else {
                    ()
                }
                ()
            }
            case "PlayerCountSelected" => {
                // Continue with name input when GUI selected the count
                val count = obj.headOption match {
                    case Some(pcs: wizard.actionmanagement.PlayerCountSelected) => pcs.count
                    case Some(i: Int) => i
                    case _ => 0
                }
                if (count >= 3 && count <= 6) {
                    phase = "AwaitPlayerNames"
                    var players = List[Player]()
                    var i = 1
                    val pattern = "^[a-zA-Z0-9]+$".r
                    while (i <= count) {
                        print(s"Enter the name of player $i: ")
                        val input = scala.io.StdIn.readLine()
                        if (input != null && input.nonEmpty && pattern.pattern.matcher(input).matches()) {
                            val player = PlayerFactory.createPlayer(Some(input), Human)
                            undoManager.doStep(new SetPlayerNameCommand(player, input))
                            players = players :+ player
                            i += 1
                        } else {
                            println("Invalid name. Please enter a name containing only letters and numbers.")
                        }
                    }
                    GameController.setPlayers(players)
                    phase = "InGame"
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
            case "which bid" => println(s"${obj.head.asInstanceOf[Player].name}, how many tricks do you bid?")
            case "invalid input, bid again" => println("Invalid input. Please enter a valid number.")
            case "print trump card" => println(s"Trump card: \n${TextUI.showcard(obj.head.asInstanceOf[Card])}")
            case "CardsDealt" => {
                val players = obj.head.asInstanceOf[CardsDealt].players
                println("Cards have been dealt to all players.")
                players.foreach(player => TextUI.showHand(player))
            }
            case "trick winner" => println(s"${obj.head.asInstanceOf[Player].name} won the trick.")
            case "points after round" => println("Points after this round:")
            case "print points all players" => obj.head.asInstanceOf[List[Player]].foreach(player => println(s"${player.name}: ${player.points} points"))
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