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
            val players = List(PlayerFactory.createPlayer("Player1", Human), PlayerFactory.createPlayer("Player2", Human), PlayerFactory.createPlayer("Player3", Human), PlayerFactory.createPlayer("Player4", Human), PlayerFactory.createPlayer("Player5", Human), PlayerFactory.createPlayer("Player6", Human), PlayerFactory.createPlayer("Player7", Human), PlayerFactory.createPlayer("Player8", Human), PlayerFactory.createPlayer("Player9", Human), PlayerFactory.createPlayer("Player10", Human), PlayerFactory.createPlayer("Player11", Human), PlayerFactory.createPlayer("Player12", Human), PlayerFactory.createPlayer("Player13", Human), PlayerFactory.createPlayer("Player14", Human), PlayerFactory.createPlayer("Player15", Human), PlayerFactory.createPlayer("Player16", Human), PlayerFactory.createPlayer("Player17", Human), PlayerFactory.createPlayer("Player18", Human), PlayerFactory.createPlayer("Player19", Human), PlayerFactory.createPlayer("Player20", Human), PlayerFactory.createPlayer("Player21", Human), PlayerFactory.createPlayer("Player22", Human), PlayerFactory.createPlayer("Player23", Human), PlayerFactory.createPlayer("Player24", Human), PlayerFactory.createPlayer("Player25", Human), PlayerFactory.createPlayer("Player26", Human), PlayerFactory.createPlayer("Player27", Human), PlayerFactory.createPlayer("Player28", Human), PlayerFactory.createPlayer("Player29", Human), PlayerFactory.createPlayer("Player30", Human), PlayerFactory.createPlayer("Player31", Human), PlayerFactory.createPlayer("Player32", Human), PlayerFactory.createPlayer("Player33", Human), PlayerFactory.createPlayer("Player34", Human), PlayerFactory.createPlayer("Player35", Human), PlayerFactory.createPlayer("Player36", Human), PlayerFactory.createPlayer("Player37", Human), PlayerFactory.createPlayer("Player38", Human), PlayerFactory.createPlayer("Player39", Human), PlayerFactory.createPlayer("Player40", Human), PlayerFactory.createPlayer("Player41", Human), PlayerFactory.createPlayer("Player42", Human), PlayerFactory.createPlayer("Player43", Human), PlayerFactory.createPlayer("Player44", Human), PlayerFactory.createPlayer("Player45", Human), PlayerFactory.createPlayer("Player46", Human), PlayerFactory.createPlayer("Player47", Human), PlayerFactory.createPlayer("Player48", Human), PlayerFactory.createPlayer("Player49", Human), PlayerFactory.createPlayer("Player50", Human), PlayerFactory.createPlayer("Player51", Human), PlayerFactory.createPlayer("Player52", Human), PlayerFactory.createPlayer("Player53", Human), PlayerFactory.createPlayer("Player54", Human), PlayerFactory.createPlayer("Player55", Human), PlayerFactory.createPlayer("Player56", Human), PlayerFactory.createPlayer("Player57", Human), PlayerFactory.createPlayer("Player58", Human), PlayerFactory.createPlayer("Player59", Human))
            val game = Game(players)

            TestUtil.simulateInput("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n") {
                GameLogic.playGame(game, players)
            }
        }

        "check if the game is over" in {
            val players = List(PlayerFactory.createPlayer("Player1", Human), PlayerFactory.createPlayer("Player2", Human), PlayerFactory.createPlayer("Player3", Human))
            val game = Game(players)
            game.rounds = 0
            GameLogic.isOver(game) shouldBe true

        }
    }
}