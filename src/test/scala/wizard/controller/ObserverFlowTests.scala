package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.{Observer, CardsDealt, Debug}
import wizard.model.player.{PlayerFactory, PlayerType, Player}
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ObserverFlowTests extends AnyWordSpec with Matchers {
  "GameLogic and RoundLogic" should {
    "emit CardsDealt and print trump card after players are set" in {
      val controller = new GameLogic
      val latchDealt = new CountDownLatch(1)
      val latchTrump = new CountDownLatch(1)

      val observer = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "CardsDealt") latchDealt.countDown()
          if (updateMSG == "print trump card") latchTrump.countDown()
          ()
        }
      }
      controller.add(observer)

      // start controller in test thread to avoid GUI; it will just notify StartGame/AskForPlayerCount
      val t = new Thread(new Runnable { override def run(): Unit = controller.start() })
      t.setDaemon(true)
      t.start()

      // Provide players directly and let playGame run in background
      val players = List("A", "B", "C").map(n => PlayerFactory.createPlayer(Some(n), PlayerType.Human))
      controller.setPlayers(players)

      // Wait for events
      latchDealt.await(5, TimeUnit.SECONDS) shouldBe true
      latchTrump.await(5, TimeUnit.SECONDS) shouldBe true
    }
  }
}
