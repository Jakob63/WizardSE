package wizard.aView

import wizard.actionmanagement.Observer

import wizard.actionmanagement.{CardsDealt, Observer, Debug, InputRouter}
import wizard.controller.aGameLogic
import wizard.model.cards.*
import wizard.model.player.PlayerType.Human
import wizard.model.player.{Player, PlayerFactory}
import wizard.undo.{SetPlayerNameCommand, UndoManager}
import wizard.controller.GameLogic

import scala.util.{Success, Try}

object TextUI extends Observer {
    var gameLogic: aGameLogic = _
    val eol: String = sys.props.getOrElse("line.separator", "\n")

  override def update(updateMSG: String, obj: Any*): Unit = {
    Debug.log(s"TextUI.update('$updateMSG') called")
    updateMSG match {
      case "which card" => println(s"${obj.head.asInstanceOf[Player].name}, which card do you want to play?")
      case "invalid card" => println("Invalid card. Please enter a valid index.")
      case "follow lead" => println(s"You must follow the lead suit ${obj.head.asInstanceOf[Color].toString}.")
      case "which bid" => println(s"${obj.head.asInstanceOf[Player].name}, how many tricks do you bid?")
      case "invalid input, bid again" => println("Invalid input. Please enter a valid number.")
      case "print trump card" => println(s"Trump card: \n${showcard(obj.head.asInstanceOf[Card])}")
      case "CardsDealt" | "cards dealt" => println("Cards have been dealt to all players.")
      case "trick winner" => println(s"${obj.head.asInstanceOf[Player].name} won the trick.")
      case "points after round" => println("Points after this round:")
      case "print points all players" => obj.head.asInstanceOf[List[Player]].foreach(player => println(s"${player.name}: ${player.points} points"))
      case "input players" => println("Enter the number of players (3-6):")
      case "game started" => println("Game officially started.")
      case "invalid name" => println("Invalid name. Please enter a name containing only letters and numbers.")
      case "player names" =>
        val current = obj(1).asInstanceOf[Int]
        print(s"Enter the name of player ${current + 1}: ")
      case _ => Debug.log(s"TextUI: Unhandled message '$updateMSG'")
    }
    // Fetch new data von Controller und update die View
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