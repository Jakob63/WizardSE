package wizard.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.player.Player
import wizard.model.player.PlayerType.Human
import wizard.model.player.PlayerFactory

class GameTest extends AnyWordSpec with Matchers {
    "Game" should {

        "correct rounds" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            game.rounds shouldBe 20
        }

        "correct current round" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            game.currentround shouldBe 0
        }

        "correct points" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            players.foreach(player => player.points shouldBe 0)
        }

        "correct tricks" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            players.foreach(player => player.tricks shouldBe 0)
        }

        "correct bids" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            players.foreach(player => player.bids shouldBe 0)
        }

        "correct round points" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            players.foreach(player => player.roundPoints shouldBe 0)
        }

        "correct round bids" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            players.foreach(player => player.roundBids shouldBe 0)
        }

        "correct round tricks" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            players.foreach(player => player.roundTricks shouldBe 0)
        }
    }
}