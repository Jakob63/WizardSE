package wizard.controller

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Human

class PlayerLogicPointsTest extends AnyFunSuite with Matchers {
  test("addPoints adds correct points when bids equal tricks") {
    val p = Human.create("P").get
    p.points = 0
    p.roundBids = 2
    p.roundTricks = 2
    PlayerLogic.addPoints(p)
    p.points should be (20 + 10 * 2)
  }

  test("addPoints subtracts when bids differ") {
    val p = Human.create("P").get
    p.points = 50
    p.roundBids = 1
    p.roundTricks = 3
    PlayerLogic.addPoints(p)
    p.points should be (50 - 10 * Math.abs(1 - 3))
  }

  test("calculatePoints returns expected int") {
    val p = Human.create("P").get
    p.points = 5
    p.roundBids = 1
    p.roundTricks = 1
    PlayerLogic.calculatePoints(p) should be (5 + 20 + 10 * 1)
  }
}

