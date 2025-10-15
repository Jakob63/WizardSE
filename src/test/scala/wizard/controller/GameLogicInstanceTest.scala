package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement._
import wizard.model.player.{PlayerFactory, PlayerType}

class GameLogicInstanceTest extends AnyWordSpec with Matchers {

  class TestObserver extends Observer {
    case class Event(msg: String, payload: Seq[Any])
    var events: Vector[Event] = Vector.empty
    override def update(updateMSG: String, obj: Any*): Any = {
      events = events :+ Event(updateMSG, obj.toVector)
    }
  }

  "GameLogic (instance)" should {
    "validate player count via instance method" in {
      val gl = new GameLogic
      gl.validGame(3) shouldBe true
      gl.validGame(2) shouldBe false
      gl.validGame(6) shouldBe true
      gl.validGame(7) shouldBe false
    }

    "emit StartGame and AskForPlayerCount on start" in {
      val gl = new GameLogic
      val obs = new TestObserver
      gl.add(obs)

      gl.start()

      // Expect two events in order
      obs.events.map(_.msg) shouldBe Vector("StartGame", "AskForPlayerCount")
      // Verify payload types
      obs.events(0).payload.headOption.exists(_.isInstanceOf[StartGame.type]) shouldBe true
      obs.events(1).payload.headOption.exists(_.isInstanceOf[AskForPlayerCount.type]) shouldBe true
    }

    "emit PlayerCountSelected when playerCountSelected is called" in {
      val gl = new GameLogic
      val obs = new TestObserver
      gl.add(obs)

      gl.playerCountSelected(4)

      obs.events should have size 1
      obs.events.head.msg shouldBe "PlayerCountSelected"
      obs.events.head.payload.headOption match {
        case Some(e: PlayerCountSelected) => e.count shouldBe 4
        case other => fail(s"Unexpected payload: $other")
      }
    }

    "emit CardAuswahl event when CardAuswahl is requested" in {
      val gl = new GameLogic
      val obs = new TestObserver
      gl.add(obs)

      gl.CardAuswahl()

      obs.events should have size 1
      obs.events.head.msg shouldBe "CardAuswahl"
      obs.events.head.payload.headOption.exists(_.isInstanceOf[CardAuswahl.type]) shouldBe true
    }

    "reset player stats in setPlayers before game thread starts" in {
      val players = List(
        PlayerFactory.createPlayer(Some("P1"), PlayerType.Human),
        PlayerFactory.createPlayer(Some("P2"), PlayerType.Human),
        PlayerFactory.createPlayer(Some("P3"), PlayerType.Human)
      )
      // set some non-zero values to verify reset
      players.foreach { p =>
        p.points = 5; p.tricks = 2; p.bids = 1; p.roundBids = 1; p.roundTricks = 1; p.roundPoints = 10
      }
      val gl = new GameLogic

      gl.setPlayers(players)

      // Immediately after call, stats should be reset synchronously
      players.foreach { p =>
        withClue(s"Player ${p.name} should have been reset: ") {
          p.points shouldBe 0
          p.tricks shouldBe 0
          p.bids shouldBe 0
          p.roundBids shouldBe 0
          p.roundTricks shouldBe 0
          p.roundPoints shouldBe 0
        }
      }
    }
  }
}
