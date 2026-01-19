package wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.model.player.Human
import wizard.model.cards.{Card, Color, Value}

class GameTest extends AnyWordSpec with Matchers with TimeLimitedTests {
  val timeLimit = 30.seconds

  "A Game" should {
    "calculate rounds correctly based on player count" in {
      val p1 = Human.create("P1").get
      val p2 = Human.create("P2").get
      val p3 = Human.create("P3").get
      
      val game3 = Game(List(p1, p2, p3))
      game3.rounds should be(20)
      
      val game4 = Game(List(p1, p2, p3, p2))
      game4.rounds should be(15)
    }

    "handle empty players list" in {
      val game = Game(Nil)
      game.rounds should be(0)
    }

    "allow setting and getting game state fields" in {
      val p1 = Human.create("P1").get
      val game = Game(List(p1))
      
      game.currentround = 5
      game.currentround should be(5)
      
      val trick = List(Card(Value.Seven, Color.Red), Card(Value.WizardKarte, Color.Blue))
      game.currentTrick = trick
      game.currentTrick should be(trick)
      
      game.firstPlayerIdx = 2
      game.firstPlayerIdx should be(2)
    }

    "be a case class and support copy/equals" in {
      val p1 = Human.create("P1").get
      val game1 = Game(List(p1))
      val game2 = Game(List(p1))
      
      game1 should be(game2)
      
      val game3 = game1.copy(players = Nil)
      game3.players should be(Nil)
    }
  }
}
