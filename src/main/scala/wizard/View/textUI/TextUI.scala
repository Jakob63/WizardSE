package wizard.View.textUI

import wizard.Controller.control.GameLogic
import wizard.Model.cards.{Card, Color, Value, valueToAnsi}
import wizard.Model.player.Player
import wizard.actionmanagement.Observer
import wizard.Model.cards.Dealer

object TextUI { //extends Observer

    //    override def update(string: String): Unit = {
    //        //showHand()
    //        if(string.startsWith("showHand 1")){
    //            val splited = string.split(" ")
    //            showHand(splited(1).toInt)
    //        }
    //    }

    // spieler eingeben
    def inputPlayers(): List[Player] = {
        print("Enter the number of players (3-6): ")
        val numPlayers = scala.io.StdIn.readInt() // TODO: string einlesen try catch
        if (!GameLogic.validGame(numPlayers)) {
            println("Invalid number of players. Please enter a number between 3 and 6.")
            inputPlayers()
        } else {
            val players = for (i <- 1 to numPlayers) yield {
                var name = ""
                val pattern = "^[a-zA-Z0-9]+$".r
                while (name == "" || !pattern.pattern.matcher(name).matches()) {
                    print(s"Enter the name of player $i: ")
                    name = scala.io.StdIn.readLine()
                    if (name == "" || !pattern.pattern.matcher(name).matches()) {
                        println("Invalid name. Please enter a name containing only letters.")
                    }
                }
                Player(name)
            }
            players.toList
        }
    }


    def showHand(player: Player): Unit = {
        println(s"${player.name}'s hand:") // todo: wenn name auf s endet dann mach das s weg
        if (player.hand.cards.isEmpty) {
            println("No cards in hand.")
        } else {
            val cardLines = player.hand.cards.map(card => TextUI.showcard(card).split("\n"))
            for (i <- cardLines.head.indices) {
                println(cardLines.map(_(i)).mkString(" "))
            }
            val handString = player.hand.cards.map(card => s"${card.value.cardType()} of ${card.color}").mkString(", ")
            println(s"($handString)")
            // ausgeben der indizes der karten
            val indices = player.hand.cards.zipWithIndex.map { case (card, index) => s"${index + 1}: ${card.value.cardType()} of ${card.color}" }
            println(s"Indices: ${indices.mkString(", ")}")
        }
    }

    def colorToAnsi(color: Color): String = color match {
        case Color.Red => Console.RED
        case Color.Green => Console.GREEN
        case Color.Blue => Console.BLUE
        case Color.Yellow => Console.YELLOW
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
        if (index >= 0 && index < Dealer.allCards.length) { // check if index is in bounds
            showcard(Dealer.allCards(index)) // print card at index
        } else { // if index is out of bounds
            s"Index $index is out of bounds."
        }
    }
    
    
    

}
    

