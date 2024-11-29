package wizard.model.rounds

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.model.player.Player
import wizard.model.rounds.Round
import wizard.model.player.PlayerType.Human
import wizard.model.player.PlayerFactory

class RoundTest extends AnyWordSpec with Matchers {
    "Round" should {

        "setTrump should set the trump color" in {
            val players = List(PlayerFactory.createPlayer("Player1", Human), PlayerFactory.createPlayer("Player2", Human))
            val round = new Round(players)
            round.setTrump(Color.Yellow)
            round.trump shouldBe Color.Yellow
        }

        "nextPlayer should return the next player in the list" in {
            val players = List(PlayerFactory.createPlayer("Player1", Human), PlayerFactory.createPlayer("Player2", Human))
            val round = new Round(players)
            round.nextPlayer().name shouldBe "Player1"
        }

        "isOver should return true if all players have empty hands" in {
            val players = List(PlayerFactory.createPlayer("Player1", Human), PlayerFactory.createPlayer("Player2", Human))
            val round = new Round(players)
            round.isOver() shouldBe true
        }

        "isOver should return false if any player has cards in hand" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            player1.addHand(Hand(List(Card(Value.Seven, Color.Red))))
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player2.addHand(Hand(Nil))
            val players = List(player1, player2)
            val round = new Round(players)
            round.isOver() shouldBe false
        }

        "finalizeRound should update players' tricks" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player1.roundPoints = 10
            player1.roundTricks = 2
            player1.roundBids = 1
            player2.roundPoints = 20
            player2.roundTricks = 3
            player2.roundBids = 2
            val players = List(player1, player2)
            val round = new Round(players)
            round.finalizeRound()
            player2.roundTricks shouldBe 0
        }

        "finalizeRound should update players' points" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player1.roundPoints = 10
            player1.roundTricks = 2
            player1.roundBids = 1
            player2.roundPoints = 20
            player2.roundTricks = 3
            player2.roundBids = 2
            val players = List(player1, player2)
            val round = new Round(players)
            round.finalizeRound()
            player2.roundPoints shouldBe 0
        }

        "finalizeRound should reset players' roundPoints" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player1.roundPoints = 10
            player1.roundTricks = 2
            player1.roundBids = 1
            player2.roundPoints = 20
            player2.roundTricks = 3
            player2.roundBids = 2
            val players = List(player1, player2)
            val round = new Round(players)
            round.finalizeRound()
            player2.roundPoints shouldBe 0
        }

        "finalizeRound should reset players' roundTricks" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player1.roundPoints = 10
            player1.roundTricks = 2
            player1.roundBids = 1
            player2.roundPoints = 20
            player2.roundTricks = 3
            player2.roundBids = 2
            val players = List(player1, player2)
            val round = new Round(players)
            round.finalizeRound()
            player2.roundTricks shouldBe 0
        }

        "finalizeRound should reset players' roundBids" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player1.roundPoints = 10
            player1.roundTricks = 2
            player1.roundBids = 1
            player2.roundPoints = 20
            player2.roundTricks = 3
            player2.roundBids = 2
            val players = List(player1, player2)
            val round = new Round(players)
            round.finalizeRound()
            player2.roundBids shouldBe 0
        }

        "finalizeRound should reset players' bids" in {
            val player1 = PlayerFactory.createPlayer("Player1", Human)
            val player2 = PlayerFactory.createPlayer("Player2", Human)
            player1.roundPoints = 10
            player1.roundTricks = 2
            player1.roundBids = 1
            player2.roundPoints = 20
            player2.roundTricks = 3
            player2.roundBids = 2
            val players = List(player1, player2)
            val round = new Round(players)
            round.finalizeRound()
            player2.bids shouldBe 2
        }
    }
}