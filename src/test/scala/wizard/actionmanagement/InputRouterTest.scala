package wizard.actionmanagement

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InputRouterTest extends AnyWordSpec with Matchers {

  "InputRouter" should {
    "store and retrieve lines" in {
      InputRouter.clear()
      InputRouter.offer("test input")
      InputRouter.readLine() should be("test input")
    }

    "handle integer inputs" in {
      InputRouter.clear()
      InputRouter.offer("42")
      InputRouter.readInt() should be(42)
    }

    "ignore non-integer inputs when reading an int" in {
      InputRouter.clear()
      InputRouter.offer("abc")
      InputRouter.offer("123")
      InputRouter.readInt() should be(123)
    }

    "throw UndoException when __UNDO__ is received" in {
      InputRouter.clear()
      InputRouter.offer("__UNDO__")
      intercept[InputRouter.UndoException] {
        InputRouter.readLine()
      }
    }

    "throw RedoException when __REDO__ is received" in {
      InputRouter.clear()
      InputRouter.offer("__REDO__")
      intercept[InputRouter.RedoException] {
        InputRouter.readLine()
      }
    }

    "return __GAME_STOPPED__ when offered" in {
      InputRouter.clear()
      InputRouter.offer("__GAME_STOPPED__")
      InputRouter.readLine() should be("__GAME_STOPPED__")
    }

    "be thread-safe for concurrent offers and reads" in {
      InputRouter.clear()
      val count = 100
      val futures = (1 to count).map { i =>
        Future {
          InputRouter.offer(i.toString)
        }
      }
      
      val results = (1 to count).map { _ =>
        var line = InputRouter.readLine()
        while (!line.forall(_.isDigit)) {
           line = InputRouter.readLine()
        }
        line.toInt
      }
      
      results should have size count
      results.sorted should be((1 to count).toList)
    }
    
    "handle null inputs gracefully in offer" in {
        InputRouter.clear()
        InputRouter.offer(null)
        InputRouter.offer("valid")
        InputRouter.readLine() should be("valid")
    }
  }
}
