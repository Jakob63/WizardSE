package wizard.controller

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.controller.GameLogic
import wizard.model.player.PlayerType.Human
import wizard.model.player.{Player, PlayerFactory}
import wizard.model.rounds.Game
import wizard.testUtils.TestUtil

class GameLogicTest extends AnyWordSpec with Matchers {
    "GameLogic" should {

        "should be valid with 3 to 6 players" in {
            GameLogic.validGame(3) shouldBe true
        }

        "should be invalid if not 3, 4, 5, 6" in {
            GameLogic.validGame(2) shouldBe false
        }

        "should be invalid if the number is negative" in {
            GameLogic.validGame(-5) shouldBe false
        }

        "should be invalid if the number is 0" in {
            GameLogic.validGame(0) shouldBe false
        }

        "play a game correctly" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human),
                PlayerFactory.createPlayer(Some("Player2"), Human),
                PlayerFactory.createPlayer(Some("Player3"), Human),
                PlayerFactory.createPlayer(Some("Player4"), Human),
                PlayerFactory.createPlayer(Some("Player5"), Human),
                PlayerFactory.createPlayer(Some("Player6"), Human),
                PlayerFactory.createPlayer(Some("Player7"), Human),
                PlayerFactory.createPlayer(Some("Player8"), Human),
                PlayerFactory.createPlayer(Some("Player9"), Human),
                PlayerFactory.createPlayer(Some("Player10"), Human),
                PlayerFactory.createPlayer(Some("Player11"), Human),
                PlayerFactory.createPlayer(Some("Player12"), Human),
                PlayerFactory.createPlayer(Some("Player13"), Human),
                PlayerFactory.createPlayer(Some("Player14"), Human),
                PlayerFactory.createPlayer(Some("Player15"), Human),
                PlayerFactory.createPlayer(Some("Player16"), Human),
                PlayerFactory.createPlayer(Some("Player17"), Human),
                PlayerFactory.createPlayer(Some("Player18"), Human),
                PlayerFactory.createPlayer(Some("Player19"), Human),
                PlayerFactory.createPlayer(Some("Player20"), Human),
                PlayerFactory.createPlayer(Some("Player21"), Human),
                PlayerFactory.createPlayer(Some("Player22"), Human),
                PlayerFactory.createPlayer(Some("Player23"), Human),
                PlayerFactory.createPlayer(Some("Player24"), Human),
                PlayerFactory.createPlayer(Some("Player25"), Human),
                PlayerFactory.createPlayer(Some("Player26"), Human),
                PlayerFactory.createPlayer(Some("Player27"), Human),
                PlayerFactory.createPlayer(Some("Player28"), Human),
                PlayerFactory.createPlayer(Some("Player29"), Human),
                PlayerFactory.createPlayer(Some("Player30"), Human),
                PlayerFactory.createPlayer(Some("Player31"), Human),
                PlayerFactory.createPlayer(Some("Player32"), Human),
                PlayerFactory.createPlayer(Some("Player33"), Human),
                PlayerFactory.createPlayer(Some("Player34"), Human),
                PlayerFactory.createPlayer(Some("Player35"), Human),
                PlayerFactory.createPlayer(Some("Player36"), Human),
                PlayerFactory.createPlayer(Some("Player37"), Human),
                PlayerFactory.createPlayer(Some("Player38"), Human),
                PlayerFactory.createPlayer(Some("Player39"), Human),
                PlayerFactory.createPlayer(Some("Player40"), Human),
                PlayerFactory.createPlayer(Some("Player41"), Human),
                PlayerFactory.createPlayer(Some("Player42"), Human),
                PlayerFactory.createPlayer(Some("Player43"), Human),
                PlayerFactory.createPlayer(Some("Player44"), Human),
                PlayerFactory.createPlayer(Some("Player45"), Human),
                PlayerFactory.createPlayer(Some("Player46"), Human),
                PlayerFactory.createPlayer(Some("Player47"), Human),
                PlayerFactory.createPlayer(Some("Player48"), Human),
                PlayerFactory.createPlayer(Some("Player49"), Human),
                PlayerFactory.createPlayer(Some("Player50"), Human),
                PlayerFactory.createPlayer(Some("Player51"), Human),
                PlayerFactory.createPlayer(Some("Player52"), Human),
                PlayerFactory.createPlayer(Some("Player53"), Human),
                PlayerFactory.createPlayer(Some("Player54"), Human),
                PlayerFactory.createPlayer(Some("Player55"), Human),
                PlayerFactory.createPlayer(Some("Player56"), Human),
                PlayerFactory.createPlayer(Some("Player57"), Human),
                PlayerFactory.createPlayer(Some("Player58"), Human),
                PlayerFactory.createPlayer(Some("Player59"), Human))
            val game = Game(players)

            TestUtil.simulateInput("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n") {
                GameLogic.playGame(game, players)
            }
        }

        "check if the game is over" in {
            val players = List(PlayerFactory.createPlayer(Some("Player1"), Human), PlayerFactory.createPlayer(Some("Player2"), Human), PlayerFactory.createPlayer(Some("Player3"), Human))
            val game = Game(players)
            game.rounds = 0
            GameLogic.isOver(game) shouldBe true

        }
    }
}