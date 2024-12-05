package wizard.aView

import wizard.actionmanagement.Observer
import wizard.model.cards.*
import wizard.model.player.PlayerType.Human
import wizard.model.player.{Player, PlayerFactory}
import wizard.undo.{UndoManager, SetPlayerNameCommand}

object TextUI extends Observer {
    private val undoManager = new UndoManager

    override def update(updateMSG: String, obj: Any*): Any = {
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
            case "print points all players" => obj.head.asInstanceOf[List[Player]].foreach(player => println(s"${player.name}: ${player.points} points"))
            case "bid einlesen" => scala.io.StdIn.readLine()
            case "card einlesen" => scala.io.StdIn.readLine()
            case "which trump" => {
                println(s"${obj.head.asInstanceOf[Player].name}, which color do you want to choose as trump?")
                scala.io.StdIn.readLine()
            }
        }
    }

    def printColorOptions(cards: List[Card]): Unit = {
        val cardLines = cards.map(showcard(_).split("\n"))
        for (i <- cardLines.head.indices) {
            println(cardLines.map(_(i)).mkString(" "))
        }
        val handString = cards.map(card => s"${card.value.cardType()} of ${card.color}").mkString(", ")
        println(s"($handString)")
        val indices = cards.zipWithIndex.map { case (card, index) => s"${index + 1}: ${card.value.cardType()} of ${card.color}" }
        println(s"Indices: ${indices.mkString(", ")}")
    }

    def inputPlayers(): List[Player] = {
        var numPlayers = -1
        while (numPlayers < 3 || numPlayers > 6) {
            print("Enter the number of players (3-6): ")
            try {
                val input = scala.io.StdIn.readLine()
                numPlayers = input.toInt
                if (numPlayers < 3 || numPlayers > 6) {
                    println("Invalid number of players. Please enter a number between 3 and 6.")
                    numPlayers = -1
                }
            } catch {
                case _: NumberFormatException =>
                    println("Invalid input. Please enter a valid number.")
            }
        }

        val players = for (i <- 1 to numPlayers) yield {
            var name = ""
            val pattern = "^[a-zA-Z0-9]+$".r
            while (name == "" || !pattern.pattern.matcher(name).matches()) {
                print(s"Enter the name of player $i: ")
                name = scala.io.StdIn.readLine()
                if (name == "" || !pattern.pattern.matcher(name).matches()) {
                    println("Invalid name. Please enter a name containing only letters and numbers.")
                }
            }
            val player = PlayerFactory.createPlayer(name, Human)
            undoManager.doStep(new SetPlayerNameCommand(player, name))
            player
        }
        players.toList
    }

    def undo(): Unit = {
        undoManager.undoStep()
    }

    def redo(): Unit = {
        undoManager.redoStep()
    }

    def showHand(player: Player): Unit = {
        println(s"${player.name}'s hand: ${player.hand.cards.mkString(", ")}")
        if (player.hand.cards.isEmpty) {
            println("No cards in hand.")
        } else {
            val cardLines = player.hand.cards.map(card => showcard(card).split("\n"))
            for (i <- cardLines.head.indices) {
                println(cardLines.map(_(i)).mkString(" "))
            }
            val handString = player.hand.cards.map(card => s"${card.value.cardType()} of ${card.color}").mkString(", ")
            println(s"($handString)")
            val indices = player.hand.cards.zipWithIndex.map { case (card, index) => s"${index + 1}: ${card.value.cardType()} of ${card.color}" }
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