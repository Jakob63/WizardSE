package wizard.controller

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.Observer
import wizard.controller.GameLogic

class GameLogicStartAndPlayerCountTest extends AnyFunSuite with Matchers {
  test("start notifies StartGame and AskForPlayerCount") {
    val gl = new GameLogic()
    var received = List[String]()
    val obs = new Observer {
      override def update(updateMSG: String, obj: Any*): Any = { received = received :+ updateMSG; null }
    }
    gl.add(obs)
    gl.start()
    received should contain ("StartGame")
    received should contain ("AskForPlayerCount")
  }

  test("playerCountSelected notifies PlayerCountSelected and AskForPlayerNames for valid count") {
    val gl = new GameLogic()
    var received = List[String]()
    val obs = new Observer {
      override def update(updateMSG: String, obj: Any*): Any = { received = received :+ updateMSG; null }
    }
    gl.add(obs)
    gl.playerCountSelected(4)
    received should contain ("PlayerCountSelected")
    received should contain ("AskForPlayerNames")
  }

  test("playerCountSelected with invalid count does nothing") {
    val gl = new GameLogic()
    var received = List[String]()
    val obs = new Observer {
      override def update(updateMSG: String, obj: Any*): Any = { received = received :+ updateMSG; null }
    }
    gl.add(obs)
    gl.playerCountSelected(2)
    received should not contain ("PlayerCountSelected")
  }
}

