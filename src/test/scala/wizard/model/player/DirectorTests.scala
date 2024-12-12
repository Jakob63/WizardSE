package wizard.model.player

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DirectorTests extends AnyWordSpec with Matchers {
    "Director" should {
        "create a player with a random name from the name list" in {
            val builder = new BuildAI()
            val player = Director.makeRandomNames(builder).asInstanceOf[AI]
            player.name should (be("Jakob") or be("Elena") or be("Janis") or be("Leon"))
        }
    }
}