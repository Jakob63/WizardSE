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
      game1.hashCode() should be(game2.hashCode())
      game1.toString should include("Game")
      
      val game3 = game1.copy(players = Nil)
      game3.players should be(Nil)

      game1.currentround = 10
      game1.currentTrick = List(Card(Value.Seven, Color.Blue))
      game1.firstPlayerIdx = 1
      
      val game4 = game1.copy()
      game4.players should be(game1.players)
      game4.currentround should be(0)
    }

    "calculate rounds for different player counts" in {
      Game(List.fill(3)(Human.create("P").get)).rounds should be(20)
      Game(List.fill(4)(Human.create("P").get)).rounds should be(15)
      Game(List.fill(5)(Human.create("P").get)).rounds should be(12)
      Game(List.fill(6)(Human.create("P").get)).rounds should be(10)
    }
  }
}
