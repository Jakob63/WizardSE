package wizard.model.player

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class BuildHumanElseTest extends AnyFunSuite with Matchers {
  test("BuildHuman.setName else branch updates existing unfinished player's name") {
    val builder = new BuildHuman()
    builder.setName("Alice")
    builder.setName("Bob")
    val player = builder.build()
    player.name should be ("Bob")
  }
}

