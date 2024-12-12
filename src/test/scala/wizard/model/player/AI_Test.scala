package wizard.model.player

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Director.names

import scala.util.Random

class AI_Test extends AnyWordSpec with Matchers {
    "AI" should {
        "be created with a name" in {
            val ai = new AI("TestAI")
            ai.name shouldBe "TestAI"
        }

        "bid should be implemented" in {
            val ai = new AI("TestAI")
            an [NotImplementedError] should be thrownBy ai.bid()
        }

        "playCard should be implemented" in {
            val ai = new AI("TestAI")
            an [NotImplementedError] should be thrownBy ai.playCard(Color.Red, Color.Blue, 0)
        }
    }

    "BuildAI" should {
        "set the name of the AI" in {
            val builder = new BuildAI()
            builder.setName("TestAI")
            val ai = builder.build().asInstanceOf[AI]
            ai.name shouldBe "TestAI"
        }

        "reset the builder" in {
            val builder = new BuildAI()
            builder.setName("TestAI")
            builder.reset()
            an [Exception] should be thrownBy builder.build()
        }

        "throw an exception if build is called before setting a name" in {
            val builder = new BuildAI()
            an [Exception] should be thrownBy builder.build()
        }
    }
}
