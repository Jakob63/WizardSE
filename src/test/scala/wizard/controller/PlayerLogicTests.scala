package wizard.controller

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import wizard.controller.PlayerLogic
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.testUtils.TestUtil
import wizard.model.player.PlayerFactory
import wizard.model.player.PlayerType.Human

class PlayerLogicTests extends AnyWordSpec with Matchers {

    "PlayerLogic" should {

        "play a valid card" in {
            val player = PlayerFactory.createPlayer("TestPlayer", Human)
            val hand = Hand(List(Card(Value.Chester, Color.Red), Card(Value.Two, Color.Blue)))
            player.addHand(hand)
            var card: Card = null
            TestUtil.simulateInput("1\n") {
                card = PlayerLogic.playCard(Color.Red, Color.Blue, 0, player)
            }
            card shouldBe Card(Value.Chester, Color.Red)
        }
//        "notify invalid card and retry" in {
//            val player = Player("TestPlayer")
//            val hand = Hand(List(Card(Value.Chester, Color.Red), Card(Value.Two, Color.Blue)))
//            player.addHand(hand)
//            val out = new java.io.ByteArrayOutputStream()
//            Console.withOut(out) {
//                TestUtil.simulateInput("3\n1\n") {
//                    PlayerLogic.playCard(Color.Red, Color.Blue, 0, player)
//                }
//            }
//            val output = out.toString
//            output should include("Invalid card. Please enter a valid index.")
//        }
//
//        "notify follow lead and retry" in {
//            val player = Player("TestPlayer")
//            val hand = Hand(List(Card(Value.Chester, Color.Red), Card(Value.Two, Color.Blue)))
//            player.addHand(hand)
//            val out = new java.io.ByteArrayOutputStream()
//            Console.withOut(out) {
//                TestUtil.simulateInput("2\n1\n") {
//                    PlayerLogic.playCard(Color.Red, Color.Blue, 0, player)
//                }
//            }
//            val output = out.toString
//            output should include("You must follow the lead suit Red.")
//        }

        "bid correctly" in {
            val player = PlayerFactory.createPlayer("TestPlayer", Human)
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TestUtil.simulateInput("3\n") {
                    val bid = PlayerLogic.bid(player)
                    bid shouldBe 3
                }
            }
        }

//        "notify invalid input and retry bid" in {
//            val player = Player("TestPlayer")
//            val out = new java.io.ByteArrayOutputStream()
//            Console.withOut(out) {
//                TestUtil.simulateInput("abc\n3\n") {
//                    PlayerLogic.bid(player)
//                }
//            }
//            val output = out.toString
//            output should include("Invalid input. Please enter a valid number.")
//        }
//
        "add points correctly when bids match tricks" in {
            val player = PlayerFactory.createPlayer("TestPlayer", Human)
            player.roundBids = 2
            player.roundTricks = 2
            PlayerLogic.addPoints(player)
            player.points shouldBe 40
        }

        "subtract points correctly when bids do not match tricks" in {
            val player = PlayerFactory.createPlayer("TestPlayer", Human)
            player.roundBids = 2
            player.roundTricks = 1
            PlayerLogic.addPoints(player)
            player.points shouldBe -10
        }

        "calculate points correctly when bids match tricks" in {
            val player = PlayerFactory.createPlayer("TestPlayer", Human)
            player.roundBids = 2
            player.roundTricks = 2
            val points = PlayerLogic.calculatePoints(player)
            points shouldBe 40
        }

        "calculate points correctly when bids do not match tricks" in {
            val player = PlayerFactory.createPlayer("TestPlayer", Human)
            player.roundBids = 2
            player.roundTricks = 1
            val points = PlayerLogic.calculatePoints(player)
            points shouldBe -10
        }
    }
}