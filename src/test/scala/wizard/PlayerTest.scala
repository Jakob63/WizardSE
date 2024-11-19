package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.Hand
import wizard.model.player.Player

class PlayerTest extends AnyWordSpec with Matchers {
    "Player" should {

        "correct Hand" in {
            val player = Player("Player1")
            player.hand shouldBe Hand(List())
        }
        
        "correct points" in {
            val player = Player("Player1")
            player.points shouldBe 0
        }
        
        "correct tricks" in {
            val player = Player("Player1")
            player.tricks shouldBe 0
        }
        
        "correct bids" in {
            val player = Player("Player1")
            player.bids shouldBe 0
        }
        
        "correct round points" in {
            val player = Player("Player1")
            player.roundPoints shouldBe 0
        }
        
        "correct round bids" in {
            val player = Player("Player1")
            player.roundBids shouldBe 0
        }
        
        "correct round tricks" in {
            val player = Player("Player1")
            player.roundTricks shouldBe 0
        }
        
        "correct addHand" in {
            val player = Player("Player1")
            player.addHand(Hand(List()))
            player.hand shouldBe Hand(List())
        }
        
        "correct addTricks" in {
            val player = Player("Player1")
            player.addTricks(1)
            player.tricks shouldBe 1
        }

    }
}
