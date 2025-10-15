package wizard.aView

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.aView.TextUI
import wizard.model.cards.*
import wizard.model.player.PlayerType.Human
import wizard.model.player.{Player, PlayerFactory}
import wizard.model.rounds.Round
import wizard.testUtils.TestUtil
import wizard.undo.UndoManager

class TextUITest extends AnyWordSpec with Matchers {

    "TextUI" should {

        "showHand should display the player's hand" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            val hand = Hand(List(Card(Value.Seven, Color.Red), Card(Value.Eight, Color.Blue)))
            player.addHand(hand)
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
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
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
            val expected_round = new Round(List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human)))
            round.toString shouldBe expected_round.toString
        }
        "inputPlayers should wait for another input if the number of players is not between 3 and 6" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("2\n3\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human)))
            round.toString shouldBe expected_round.toString
        }
        "inputPlayers should wait for another input if no number is given" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("\n3\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human)))
            round.toString shouldBe expected_round.toString
        }
        "inputPlayers should wait for another input if player name is empty" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("3\n\nPlayer1\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            val round = new Round(players)
            val expected_round = new Round(List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human)))
            round.toString shouldBe expected_round.toString
        }
        "print the correct card at a valid index" in {
            Dealer.shuffleCards()
            val index = 0
            val expectedCard = Dealer.allCards(index)
            val expectedOutput = TextUI.showcard(expectedCard)
            val output = TextUI.printCardAtIndex(index)
            output shouldBe expectedOutput
        }

        "print an out of bounds message for an invalid index" in {
            val invalidIndex = Dealer.allCards.length
            val expectedOutput = s"Index $invalidIndex is out of bounds."
            val output = TextUI.printCardAtIndex(invalidIndex)
            output shouldBe expectedOutput
        }

        // Update tests
        "print the correct message for 'which card'" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("which card", player)
            }
            val output = out.toString.trim
            output shouldBe "TestPlayer, which card do you want to play?"
        }

        "print the correct message for 'invalid card'" in {
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("invalid card")
            }
            val output = out.toString.trim
            output shouldBe "Invalid card. Please enter a valid index."
        }

        "print the correct message for 'follow lead'" in {
            val color = Color.Red
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("follow lead", color)
            }
            val output = out.toString.trim
            output shouldBe "You must follow the lead suit Red."
        }

        "print the correct message for 'which bid'" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("which bid", player)
            }
            val output = out.toString.trim
            output shouldBe "TestPlayer, how many tricks do you bid?"
        }

        "print the correct message for 'invalid input, bid again'" in {
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("invalid input, bid again")
            }
            val output = out.toString.trim
            output shouldBe "Invalid input. Please enter a valid number."
        }

        "print the correct message for 'print trump card'" in {
            val card = Card(Value.Chester, Color.Blue)
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("print trump card", card)
            }
            val output = out.toString.trim
            output should include("Trump card:")
            output should include(TextUI.showcard(card))
        }

        "print the correct message for 'cards dealt'" in {
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("cards dealt")
            }
            val output = out.toString.trim
            output shouldBe "Cards have been dealt to all players."
        }

        "print the correct message for 'trick winner'" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("trick winner", player)
            }
            val output = out.toString.trim
            output shouldBe "TestPlayer won the trick."
        }

        "print the correct message for 'points after round'" in {
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TextUI.update("points after round")
            }
            val output = out.toString.trim
            output shouldBe "Points after this round:"
        }
        "inputPlayers should work with undo manager" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("3\nPlayer1\nundo\nJakob\nPlayer2\nPlayer3") {
                players = TextUI.inputPlayers()
            }
            players(0).name shouldBe "Jakob"
        }
        "inputPlayers should work with redo" in {
            var players: List[Player] = List()
            TestUtil.simulateInput("3\nPlayer1\nundo\nredo\nJakob\nPlayer2\n") {
                players = TextUI.inputPlayers()
            }
            players(0).name shouldBe "Jakob"
        }
//        "inputPlayers should work with redo" in {
//            var players: List[Player] = List()
//            TestUtil.simulateInput("3\nPlayer1\nundo\nredo\nJakob\nPlayer2\n") {
//                players = TextUI.inputPlayers()
//            }
//            players(0).name shouldBe "Player1"
//        }

        "inputPlayers should not print invalid number message when 'undo' entered at count prompt" in {
          var players: List[Player] = List()
          val out = new java.io.ByteArrayOutputStream()
          Console.withOut(out) {
            TestUtil.simulateInput("undo\n3\nPlayer1\nPlayer2\nPlayer3") {
              players = TextUI.inputPlayers()
            }
          }
          val output = out.toString
          output should not include ("Invalid number of players")
          players.length shouldBe 3
        }

        "inputPlayers should not print invalid number message after undoing from first name back to count and then entering names" in {
          var players: List[Player] = List()
          val out = new java.io.ByteArrayOutputStream()
          Console.withOut(out) {
            TestUtil.simulateInput("3\nundo\n3\nElena\nP2\nP3") {
              players = TextUI.inputPlayers()
            }
          }
          val output = out.toString
          output should not include ("Invalid number of players")
          players.map(_.name) shouldBe List("Elena", "P2", "P3")
        }
    }
}
