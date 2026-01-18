package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value}
import wizard.model.rounds.Round
import wizard.actionmanagement.InputRouter

class RoundStateTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  class TestPlayer(name: String) extends Player(name) {
    override def bid(): Int = 0
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = 
      Card(Value.One, Color.Red)
  }

  "RoundStates" should {
    val p1 = new TestPlayer("P1")
    val players = List(p1)
    val round = new Round(players)

    "NormalCardState should set trump to card color" in {
      val state = new NormalCardState
      val trumpCard = Card(Value.Seven, Color.Blue)
      state.handleTrump(round, trumpCard, players)
      round.trump should be(Some(Color.Blue))
    }

    "ChesterCardState should set trump to None" in {
      val state = new ChesterCardState
      val trumpCard = Card(Value.Chester, Color.Red)
      state.handleTrump(round, trumpCard, players)
      round.trump should be(None)
    }

    "WizardCardState should allow choosing trump" in {
      val state = new WizardCardState
      val trumpCard = Card(Value.WizardKarte, Color.Yellow)
      
      InputRouter.clear()
      InputRouter.offer("3")
      state.handleTrump(round, trumpCard, players)
      
      round.trump should be(Some(Color.Blue))
    }

    "WizardCardState should default to Red on invalid input" in {
      val state = new WizardCardState
      val trumpCard = Card(Value.WizardKarte, Color.Yellow)
      
      InputRouter.clear()
      InputRouter.offer("abc")
      state.handleTrump(round, trumpCard, players)
      round.trump should be(Some(Color.Red))
    }
  }
}
