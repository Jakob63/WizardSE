package wizard.actionmanagement

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import scala.concurrent.duration.*

class ObserverTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  "An Observable" should {
    "allow adding and removing observers" in {
      val observable = new Observable
      val observer = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = ()
      }
      
      observable.add(observer)
      observable.subscribers should contain(observer)
      
      observable.remove(observer)
      observable.subscribers should not contain(observer)
    }

    "not add the same observer twice" in {
      val observable = new Observable
      val observer = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = ()
      }
      
      observable.add(observer)
      observable.add(observer)
      observable.subscribers.size should be(1)
    }

    "notify all observers" in {
      val observable = new Observable
      var callCount1 = 0
      var callCount2 = 0
      
      val obs1 = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          callCount1 += 1
          updateMSG should be("test")
          obj.head should be("data")
        }
      }
      val obs2 = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          callCount2 += 1
        }
      }
      
      observable.add(obs1)
      observable.add(obs2)
      
      observable.notifyObservers("test", "data")
      
      callCount1 should be(1)
      callCount2 should be(1)
    }

    "handle exceptions in observers gracefully" in {
      val observable = new Observable
      var nextCalled = false
      
      val failingObs = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          throw new RuntimeException("Test exception")
        }
      }
      val succeedingObs = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          nextCalled = true
        }
      }
      
      observable.add(failingObs)
      observable.add(succeedingObs)
      
      observable.notifyObservers("test")
      
      nextCalled should be(true)
    }
  }
}
