package wizard.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.cards.{Color}

class AITest extends AnyWordSpec with Matchers {

  "An AI" should {
    "be created with a name" in {
      val ai = new AI("Robo")
      ai.name should be("Robo")
    }

    "throw NotImplementedError when bidding (as not yet implemented)" in {
      val ai = new AI("Robo")
      intercept[NotImplementedError] {
        ai.bid()
      }
    }

    "throw NotImplementedError when playing a card (as not yet implemented)" in {
      val ai = new AI("Robo")
      intercept[NotImplementedError] {
        ai.playCard(None, None, 0)
      }
    }

    "support backward compatible playCard" in {
        val ai = new AI("Robo")
        intercept[NotImplementedError] {
            ai.playCard(Color.Red, Color.Blue, 0)
        }
    }
  }
}
