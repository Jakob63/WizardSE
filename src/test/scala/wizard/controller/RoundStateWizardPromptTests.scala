package wizard.controller

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import wizard.actionmanagement.Observer
import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.{PlayerFactory, PlayerType}
import wizard.model.rounds.Round

class RoundStateWizardPromptTests extends AnyWordSpec with Matchers {
  "WizardCardState" should {
    "notify observers to choose trump when a Wizard is drawn as trump card" in {
      val players = List(
        PlayerFactory.createPlayer(Some("P1"), PlayerType.Human),
        PlayerFactory.createPlayer(Some("P2"), PlayerType.Human),
        PlayerFactory.createPlayer(Some("P3"), PlayerType.Human)
      )
      val round = new Round(players)
      var received: List[String] = Nil
      val obs = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          received = received :+ updateMSG
        }
      }
      round.add(obs)
      val state = new WizardCardState
      state.handleTrump(round, Card(Value.WizardKarte, Color.Red), players)
      received should contain ("which trump")
    }
  }
}
