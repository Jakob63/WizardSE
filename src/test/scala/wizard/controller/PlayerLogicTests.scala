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
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            val hand = Hand(List(Card(Value.Chester, Color.Red), Card(Value.Two, Color.Blue)))
            player.addHand(hand)
            var card: Option[Card] = None
            TestUtil.simulateInput("1\n") {
                card = Some(PlayerLogic.playCard(Some(Color.Red), Some(Color.Blue), 0, player))
            }
            card shouldBe Some(Card(Value.Chester, Color.Red))
        }
        "bid correctly" in { // wrong bid and right bid
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            val out = new java.io.ByteArrayOutputStream()
            Console.withOut(out) {
                TestUtil.simulateInput("\n3\n") {
                    val bid = PlayerLogic.bid(player)
                    bid shouldBe 3
                }
            }
        }
        "add points correctly when bids match tricks" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            player.roundBids = 2
            player.roundTricks = 2
            PlayerLogic.addPoints(player)
            player.points shouldBe 40
        }

        "subtract points correctly when bids do not match tricks" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            player.roundBids = 2
            player.roundTricks = 1
            PlayerLogic.addPoints(player)
            player.points shouldBe -10
        }

        "calculate points correctly when bids match tricks" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            player.roundBids = 2
            player.roundTricks = 2
            val points = PlayerLogic.calculatePoints(player)
            points shouldBe 40
        }

        "calculate points correctly when bids do not match tricks" in {
            val player = PlayerFactory.createPlayer(Some("TestPlayer"), Human)
            player.roundBids = 2
            player.roundTricks = 1
            val points = PlayerLogic.calculatePoints(player)
            points shouldBe -10
        }
    }
}