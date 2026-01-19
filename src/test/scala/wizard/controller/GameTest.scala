package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Human

class GameTest extends AnyWordSpec with Matchers {
  "The Controller Game object" should {
    "create a ModelGame using apply" in {
      val p1 = Human.create("P1").get
      val players = List(p1)
      val game = Game(players)
      
      game shouldBe a [wizard.model.Game]
      game.players should be(players)
    }
  }
}
