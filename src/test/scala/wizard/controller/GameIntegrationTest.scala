package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import org.scalatest.BeforeAndAfterEach
import wizard.model.player.{Player, Human}
import wizard.model.cards.{Card, Color, Value, Dealer}
import wizard.actionmanagement.{InputRouter, GameStoppedException, Observer}
import scala.concurrent.{Promise, Await}
import scala.concurrent.duration.*

class GameIntegrationTest extends AnyWordSpec with Matchers with TimeLimitedTests with BeforeAndAfterEach {

  val timeLimit = 30.seconds

  override def beforeEach(): Unit = {
    InputRouter.clear()
    System.setProperty("WIZARD_INTERACTIVE", "false")
  }

  override def afterEach(): Unit = {
    System.setProperty("WIZARD_INTERACTIVE", "true")
  }

  "GameLogic Integration" should {

    "simulate a full game cycle with multiple rounds" in {
      val gameLogic = new GameLogic
      val p1 = Human.create("P1").get
      val p2 = Human.create("P2").get
      val p3 = Human.create("P3").get
      val players = List(p1, p2, p3)
      
      val maxRounds = 2
      
      var roundsFinished = 0
      val observer = new Observer {
        override def update(updateMSG: String, obj: Any*): Any = {
          if (updateMSG == "points after round") {
            roundsFinished += 1
          }
        }
      }
      gameLogic.add(observer)

      Dealer.index = 0
      Dealer.allCards = (for {
        v <- Value.values.toList
        c <- Color.values.toList
      } yield Card(v, c))
      InputRouter.offer("0")
      InputRouter.offer("1")
      InputRouter.offer("0")
      
      // Falls Wizard als Trumpf (4 Farben zur Wahl)
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")
      
      // Runde 2
      InputRouter.offer("1")
      InputRouter.offer("0")
      InputRouter.offer("1")

      // Falls Wizard als Trumpf
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      gameLogic.playGame(players, maxRounds, 0)
      
      roundsFinished should be(maxRounds)
    }

    "propagate GameStoppedException correctly" in {
      val gameLogic = new GameLogic
      val p1 = Human.create("P1").get
      val p2 = Human.create("P2").get
      val p3 = Human.create("P3").get
      val players = List(p1, p2, p3)
      
      InputRouter.clear()
      // We offer multiple stops just in case it hits multiple input prompts
      for (_ <- 1 to 10) InputRouter.offer("__GAME_STOPPED__")
      
      val promise = Promise[Unit]()
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          try {
            gameLogic.playGame(players, 5, 0)
            promise.success(())
          } catch {
            case _: wizard.actionmanagement.GameStoppedException => 
              promise.success(())
            case e: Throwable => 
              promise.failure(e)
          }
        }
      })
      t.start()
      
      try {
        Await.result(promise.future, 15.seconds)
      } catch {
        case e: Exception =>
          t.getStackTrace.foreach(ste => println(s"[DEBUG_LOG]   at $ste"))
          throw e
      }
    }
    
    "handle stopCurrentGame flag" in {
       val gameLogic = new GameLogic
       val p1 = Human.create("P1").get
       val p2 = Human.create("P2").get
       val p3 = Human.create("P3").get
       val players = List(p1, p2, p3)
       
       gameLogic.stopGame()
       
       gameLogic.playGame(players, 5, 0)
    }
  }
}
