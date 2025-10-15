package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.{Observer, PlayerCountSelected}

class GameLogicPlayerCountTests extends AnyWordSpec with Matchers {
  "GameLogic.playerCountSelected" should {
    "notify observers with PlayerCountSelected event" in {
      val controller = new GameLogic
      @volatile var received: Option[Int] = None
      val testObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "PlayerCountSelected") {
            val count = obj.headOption match {
              case Some(pcs: PlayerCountSelected) => pcs.count
              case Some(i: Int) => i
              case _ => -1
            }
            received = Some(count)
          }
        }
      }
      controller.add(testObserver)
      controller.playerCountSelected(4)
      received shouldBe Some(4)
    }

    "only notify once when called multiple times; first valid count wins" in {
      val controller = new GameLogic
      @volatile var notifications = 0
      @volatile var lastCount: Option[Int] = None
      val testObserver = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "PlayerCountSelected") {
            notifications += 1
            val count = obj.headOption match {
              case Some(pcs: PlayerCountSelected) => pcs.count
              case Some(i: Int) => i
              case _ => -1
            }
            lastCount = Some(count)
          }
        }
      }
      controller.add(testObserver)
      controller.playerCountSelected(3)
      controller.playerCountSelected(5)
      controller.playerCountSelected(2) // invalid, should be ignored regardless
      notifications shouldBe 1
      lastCount shouldBe Some(3)
    }
  }
}
