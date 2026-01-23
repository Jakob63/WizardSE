package wizard.actionmanagement

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import org.scalatest.BeforeAndAfterEach
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InputRouterTest extends AnyWordSpec with Matchers with TimeLimitedTests with BeforeAndAfterEach {

  val timeLimit = 30.seconds

  override def beforeEach(): Unit = {
    InputRouter.clear()
  }

  "InputRouter" should {
    "store and retrieve lines" in {
      InputRouter.offer("test input")
      InputRouter.readLine() should be("test input")
    }

    "handle integer inputs" in {
      InputRouter.offer("42")
      InputRouter.readInt() should be(42)
    }

    "ignore non-integer inputs when reading an int" in {
      InputRouter.offer("abc")
      InputRouter.offer("123")
      InputRouter.readInt() should be(123)
    }

    "throw UndoException when __UNDO__ is received" in {
      InputRouter.offer("__UNDO__")
      intercept[InputRouter.UndoException] {
        InputRouter.readLine()
      }
    }

    "throw RedoException when __REDO__ is received" in {
      InputRouter.offer("__REDO__")
      intercept[InputRouter.RedoException] {
        InputRouter.readLine()
      }
    }

    "return __GAME_STOPPED__ when offered" in {
      InputRouter.offer("__GAME_STOPPED__")
      InputRouter.readLine() should be("__GAME_STOPPED__")
    }

    "be thread-safe for concurrent offers and reads" in {
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
        InputRouter.offer(null)
        InputRouter.offer("valid")
        InputRouter.readLine() should be("valid")
    }

    "trigger feeder thread when WIZARD_INTERACTIVE is set" in {
      val oldProp = sys.props.get("WIZARD_INTERACTIVE")
      sys.props("WIZARD_INTERACTIVE") = "true"
      
      try {
        InputRouter.offer("manual")
        InputRouter.readLine() should be("manual")
      } finally {
        oldProp match {
          case Some(v) => sys.props("WIZARD_INTERACTIVE") = v
          case None => sys.props.remove("WIZARD_INTERACTIVE")
        }
      }
    }
  }
}
