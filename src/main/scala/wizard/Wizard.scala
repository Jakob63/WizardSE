package wizard

import wizard.cards.Dealer
import wizard.player.Player
import wizard.rounds.Game
import wizard.textUI.TextUI.{inputPlayers, showHand}
import wizard.controll.GameLogic

object Wizard {

    class Wizard {

    }

    def main(args: Array[String]): Unit = {
        println("Welcome to Wizard!")
        Dealer.shuffleCards()
        println(Dealer.allCards)
        val players = inputPlayers()
        val game = new Game(players)
        println("Game officially started.")
        GameLogic.playGame(game, players)
//        val player = Player("Player1")
//        val player2 = Player("Player2")
//        val player3 = Player("Player3")
//        val players = List(player, player2, player3)
        //val game = Game(players)
        //game.playGame()
//        players.foreach { player =>
//            val hand = Dealer.dealCards(3)
//            player.addHand(hand)
//        }
//        println("Cards dealt to all players.")
//        players.foreach(showHand) // scala ist toll
//        println("Trump card:")
//        // Eigentlich CurrentRound * PlayerCount müssen wir noch machen
//        Dealer.printCardAtIndex(3*3)
    }

    val eol = sys.props("line.separator")
    def bar(cellWidth: Int = 4, cellNum: Int = 2) =
        (("-" * cellWidth) * cellNum) + "-" + eol

    def bar2(cellWidth: Int = 16, cellNum: Int = 2) =
        (("-" * cellWidth) * cellNum) + "-" + eol

    def cells(cellWidth: Int = 7, cellNum: Int = 1) =
        ("|" + " " * cellWidth) * cellNum + "|" + eol

    def cells2() =
        "|" + " game  " + "|" + eol

    def cells3() =
        "|" + " trump " + "|" + eol

    def cells4() =
        ("|" + "Set win" + "|" + "\t") * 3 + eol

    def cells5(cellWidth: Int = 7, cellNum: Int = 1) =
        (("|" + " " * cellWidth) * cellNum + "|" + "\t") * 3 + eol


    //def mesh =
    //    (bar() + cells() * 3) + bar()

    def mesh2: String =
        bar() + cells() + cells2() + cells() + bar()

    def mesh3: String =
        bar() + cells() + cells3() + cells() + bar()

    def mesh4: String =
        bar2() + cells5() + cells4() + cells5() + bar2()

    println(mesh2)
    println(mesh3)
    println(mesh4)


}