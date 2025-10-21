package wizard.aView

import wizard.actionmanagement.Observer
import wizard.controller.{GameLogic, aGameLogic}
import wizard.model.cards.*
import wizard.model.player.Player

object TextUI extends Observer {
  
  var gameLogic: aGameLogic = _

  override def update(updateMSG: String, obj: Any*): Unit = {
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
      case "main menu" => gameMenuTUI()
      case "input players" => inputPlayers()
      case "game started" => println("Game officially started.")
      case "player names" => playerNames(obj.head.asInstanceOf[Int], obj(1).asInstanceOf[Int], obj(2).asInstanceOf[List[Player]])
      case "handle choice" => handleChoice(obj.head.asInstanceOf[Int])
    }
    // Fetch new data von Controller und update die View
  }

  def gameMenuTUI(): Unit = {
    println("Welcome to Wizard!")
    println("1. Start Game")
    println()
    println("2. Exit")
    println("Please enter your choice (1 or 2): ")
    var choice = 0
    val input = scala.io.StdIn.readLine()
    choice = input.toInt
    if (choice.isInstanceOf[Int]) {
      handleChoice(choice)
    }
  }
  def handleChoice(choice: Int): Unit = {
    if (choice == 2) {
      println("Exiting the game. Goodbye!")
      System.exit(0)
    } else if (choice != 1) {
      println("Invalid choice. Please enter 1 or 2.")
      gameMenuTUI()
    } else {
      println("Starting the game...")
      gameLogic.askPlayerNumber()
    }
  }

  def inputPlayers():Unit = {
    var numPlayers = -1
    while (numPlayers < 3 || numPlayers > 6) {
      print("Enter the number of players (3-6): \n")
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
    gameLogic.createPlayers(numPlayers)
  }

  def playerNames(numPlayers: Int, current: Int, players: List[Player]): Unit = {
    var name = ""
    val pattern = "^[a-zA-Z0-9]+$".r
    while (name == "" || !pattern.pattern.matcher(name).matches()) {
      print(s"Enter the name of player ${current + 1}: ")
      name = scala.io.StdIn.readLine()
      if (name == "" || !pattern.pattern.matcher(name).matches()) {
        println("Invalid name. Please enter a name containing only letters and numbers.")
      }
    }
    val player = Player(name)

    gameLogic.createPlayers(numPlayers, current + 1, players.appended(player))
  }

  def showHand(player: Player): Unit = {
    println(s"${player.name}'s hand: ${player.hand.cards.mkString(", ")}")
    if (player.hand.cards.isEmpty) {
      println("No cards in hand.")
    } else {
      val cardLines = player.hand.cards.map(card => TextUI.showcard(card).split("\n"))
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