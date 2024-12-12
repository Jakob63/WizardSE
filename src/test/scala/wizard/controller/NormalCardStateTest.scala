package wizard.controller

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Card, Color, Value}
import wizard.model.player.Player
import wizard.model.player.PlayerFactory
import wizard.model.player.PlayerType.Human
import wizard.model.rounds.Round

class NormalCardStateTest extends AnyWordSpec with Matchers {

    "NormalCardState" should {

        "handle trump correctly" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human))
            val round = new Round(players)
            val trumpCard = Card(Value.One, Color.Red)

            val state = new NormalCardState()
            state.handleTrump(round, trumpCard, players)

            round.trump shouldBe Some(Color.Red)
        }
    }
}