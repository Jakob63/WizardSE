package wizard


import wizard.model.cards.Dealer
import wizard.model.rounds.Game
import wizard.aView.TextUI
import wizard.components.{Configuration, DefaultConfiguration}
import wizard.controller.{GameLogic, PlayerLogic, RoundLogic}

object Wizard {

    class Wizard {

    }
    
    def entry(config: Configuration) = {
      for (observer <- config.observables) {
        GameLogic.add(observer)
        PlayerLogic.add(observer)
        RoundLogic.add(observer)
      }
      GameLogic.startGame()
    }

    def main(args: Array[String]): Unit = {
      val config = DefaultConfiguration()
      entry(config)
//  2      println("Welcome to Wizard!")
//        Dealer.shuffleCards()
//        println(Dealer.allCards)
// 2       val players = TextUI.inputPlayers()
//  2      val game = new Game(players)
//    2    println("Game officially started.")
//    2    GameLogic.playGame(game, players)
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