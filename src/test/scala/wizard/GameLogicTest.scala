package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.controller.GameLogic
import wizard.model.player.Player
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
            val players = List(Player("Player1"), Player("Player2"), Player("Player3"), Player("Player4"), Player("Player5"), Player("Player6"), Player("Player7"), Player("Player8"), Player("Player9"), Player("Player10"), Player("Player11"), Player("Player12"), Player("Player13"), Player("Player14"), Player("Player15"), Player("Player16"), Player("Player17"), Player("Player18"), Player("Player19"), Player("Player20"), Player("Player21"), Player("Player22"), Player("Player23"), Player("Player24"), Player("Player25"), Player("Player26"), Player("Player27"), Player("Player28"), Player("Player29"), Player("Player30"), Player("Player31"), Player("Player32"), Player("Player33"), Player("Player34"), Player("Player35"), Player("Player36"), Player("Player37"), Player("Player38"), Player("Player39"), Player("Player40"), Player("Player41"), Player("Player42"), Player("Player43"), Player("Player44"), Player("Player45"), Player("Player46"), Player("Player47"), Player("Player48"), Player("Player49"), Player("Player50"), Player("Player51"), Player("Player52"), Player("Player53"), Player("Player54"), Player("Player55"), Player("Player56"), Player("Player57"), Player("Player58"), Player("Player59"))
            val game = Game(players)

            TestUtil.simulateInput("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n") {
                GameLogic.playGame(game, players)
            }
        }

        "check if the game is over" in {
            val players = List(Player("Player1"), Player("Player2"), Player("Player3"))
            val game = Game(players)
            game.rounds = 0
            GameLogic.isOver(game) shouldBe true

        }
    }
}