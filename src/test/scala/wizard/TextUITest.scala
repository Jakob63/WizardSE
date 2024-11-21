package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.aView.TextUI
import wizard.controller.GameLogic
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.model.player.Player
import wizard.testUtils.TestUtil
import wizard.model.rounds.Round

class TextUITest extends AnyWordSpec with Matchers {

    "TextUI" should {

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
        "showHand should display an exception message if the player's hand is empty" in {
            val player = Player("TestPlayer")
            // Capture the output of showHand
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.showHand(player)
            }

            val output = out.toString
            output should include ("No cards in hand.")
        }

        "inputPlayers should return a List of Players" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("3\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(Player("Player1"), Player("Player2"), Player("Player3")))
            round.toString shouldBe expected_round.toString
        }
        "inputPlayers should wait for another input if the number of players is not between 3 and 6" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("2\n3\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(Player("Player1"), Player("Player2"), Player("Player3")))
            round.toString shouldBe expected_round.toString
        }
        "inputPlayers should wait for another input if no number is given" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("\n3\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(Player("Player1"), Player("Player2"), Player("Player3")))
            round.toString shouldBe expected_round.toString
        }
        "inputPlayers should wait for another input if player name is empty" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("3\n\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(Player("Player1"), Player("Player2"), Player("Player3")))
            round.toString shouldBe expected_round.toString
        }
    }
}