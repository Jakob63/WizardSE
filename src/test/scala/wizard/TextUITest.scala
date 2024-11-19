package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.aView.TextUI
import wizard.controller.GameLogic
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.model.player.Player

class TextUITest extends AnyWordSpec with Matchers {

    "TextUI" should {

        "inputPlayers should return a list of players" in {
            // Simulate user input for number of players and player names
            val input = "3\nPlayer1\nPlayer2\nPlayer3\n"
            val in = new java.io.ByteArrayInputStream(input.getBytes)
            Console.withIn(in) {
                val players = TextUI.inputPlayers(GameLogic)
                assert(players.length == 3)
                assert(players.map(_.name) == List("Player1", "Player2", "Player3"))
            }
        }

        "showHand should display the player's hand" in {
            val player = Player("TestPlayer")
            val hand = Hand(List(Card(Value.Seven, Color.Red), Card(Value.Eight, Color.Blue)))
            player.addHand(hand)
            // Capture the output of showHand
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.showHand(player)
            }

            val output = out.toString
            output should include ("TestPlayer's hand:")
            output should include ("7 of Red")
            output should include ("8 of Blue")
        }
    }
}