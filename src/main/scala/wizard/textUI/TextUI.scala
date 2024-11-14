package wizard.textUI

import wizard.actionmanagement.Observer
import wizard.player.Player
import wizard.controll.GameLogic

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
        val numPlayers = scala.io.StdIn.readInt()
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
            val cardLines = player.hand.cards.map(_.showcard().split("\n"))
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
}
    

