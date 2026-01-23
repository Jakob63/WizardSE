package wizard.model.player

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class BuildAIElseTest extends AnyFunSuite with Matchers {
  test("BuildAI.setName else branch updates existing unfinished AI's name") {
    val builder = new BuildAI()
    builder.setName("AI_Alice")
    builder.setName("AI_Bob")
    val player = builder.build()
    player.name should be ("AI_Bob")
  }
}

