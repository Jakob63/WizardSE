package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.Controller.control.{GameLogic, RoundLogic}
import wizard.Model.cards.{Card, Color, Hand, Value}
import wizard.Model.player.Player
import wizard.Model.rounds.Round

class RoundLogicTest extends AnyWordSpec with Matchers {
    "RoundLogic" should {

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

//        "correct playRound with 3 players" in {
//            val players = List(
//                Player("Player 1"),
//                Player("Player 2"),
//                Player("Player 3")
//            )
//            RoundLogic.playRound(1, players)
//        }

        "correct trickwinner" in {
            val players = List(
                Player("Player 1"),
                Player("Player 2"),
                Player("Player 3")
            )

            // Initialize hands with cards
            players(0).hand = Hand(List(Card(Value.Two, Color.Red)))
            players(1).hand = Hand(List(Card(Value.Three, Color.Red)))
            players(2).hand = Hand(List(Card(Value.Four, Color.Red)))

            val round = new Round(players)
            val trick = List(
                (players(0), players(0).hand.cards.head),
                (players(1), players(1).hand.cards.head),
                (players(2), players(2).hand.cards.head)
            )
            val winner = RoundLogic.trickwinner(trick, round)
            winner shouldBe players(2) // Adjust the expected winner based on the cards
        }

    }
}
