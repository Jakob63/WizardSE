package wizard.model.player

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Human
import scala.util.{Success, Failure}

class HumanCreateTest extends AnyFunSuite with Matchers {
  test("Human.create with non-empty name returns Success") {
    val res = Human.create("Bob")
    res.isSuccess shouldBe true
  }
  test("Human.create with empty name returns Failure") {
    val res = Human.create("")
    res.isFailure shouldBe true
    res.failed.get shouldBe a [IllegalArgumentException]
  }
}

