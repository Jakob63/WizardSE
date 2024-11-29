package wizard.model.player

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.Hand
import wizard.model.player.Player
import wizard.model.player.PlayerFactory
import wizard.model.player.PlayerType.Human

class PlayerTest extends AnyWordSpec with Matchers {
    "Player" should {

        "correct Hand" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.hand shouldBe Hand(List())
        }
        
        "correct points" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.points shouldBe 0
        }
        
        "correct tricks" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.tricks shouldBe 0
        }
        
        "correct bids" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.bids shouldBe 0
        }
        
        "correct round points" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.roundPoints shouldBe 0
        }
        
        "correct round bids" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.roundBids shouldBe 0
        }
        
        "correct round tricks" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.roundTricks shouldBe 0
        }
        
        "correct addHand" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.addHand(Hand(List()))
            player.hand shouldBe Hand(List())
        }
        
        "correct addTricks" in {
            val player = PlayerFactory.createPlayer("Player1", Human)
            player.addTricks(1)
            player.tricks shouldBe 1
        }

    }
}
