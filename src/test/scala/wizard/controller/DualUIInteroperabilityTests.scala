package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.{Observer, AskForPlayerCount, PlayerCountSelected}

class DualUIInteroperabilityTests extends AnyWordSpec with Matchers {
  "GameLogic with two observers (simulating TUI and GUI)" should {
    "broadcast AskForPlayerCount to both after start()" in {
      val controller = new GameLogic
      @volatile var tuiGotAsk = 0
      @volatile var guiGotAsk = 0

      val tuiObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "AskForPlayerCount") tuiGotAsk += 1
        }
      }
      val guiObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "AskForPlayerCount") guiGotAsk += 1
        }
      }

      controller.add(tuiObserver)
      controller.add(guiObserver)

      controller.start()

      tuiGotAsk shouldBe 1
      guiGotAsk shouldBe 1
    }

    "broadcast only one PlayerCountSelected when multiple views select; first valid wins" in {
      val controller = new GameLogic
      @volatile var tuiNotified = 0
      @volatile var guiNotified = 0
      @volatile var lastCount: Option[Int] = None

      val tuiObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "PlayerCountSelected") {
            tuiNotified += 1
            val c = obj.headOption match {
              case Some(pcs: PlayerCountSelected) => pcs.count
              case Some(i: Int) => i
              case _ => -1
            }
            lastCount = Some(c)
          }
        }
      }
      val guiObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "PlayerCountSelected") guiNotified += 1
        }
      }

      controller.add(tuiObserver)
      controller.add(guiObserver)

      controller.playerCountSelected(6)
      controller.playerCountSelected(4) // should be ignored

      tuiNotified shouldBe 1
      guiNotified shouldBe 1
      lastCount shouldBe Some(6)
    }

    "setPlayers should return quickly (asynchronous game loop)" in {
      val controller = new GameLogic
      val players = List.empty[wizard.model.player.Player]
      val start = System.nanoTime()
      controller.setPlayers(players)
      val elapsedMs = (System.nanoTime() - start) / 1000000
      // Should be effectively instantaneous; allow a small margin
      elapsedMs should be < 200L
    }
  }
}
