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
      val myCards = (for {
        v <- Value.values.toList
        c <- Color.values.toList
      } yield Card(v, c))
      Dealer.allCards = myCards
      
      // Round 1
      // Trump card at index 3: (1 * 3) % 60 = 3. myCards(3) is Chester of Yellow.
      // Dealer.dealCards(1, Some(Chester of Yellow))
      // P1: myCards(0) - Chester of Red
      // P2: myCards(1) - Chester of Green
      // P3: myCards(2) - Chester of Blue
      // Dealer.index = 3, but skipped 3 because it matches trump card? 
      // No, Dealer.dealCards(1, Some(trump)):
      // it takes myCards(0), if 0 != trump, it's done.
      // So Round 1 hands: P1: myCards(0), P2: myCards(1), P3: myCards(2). Dealer.index = 3.

      // Bids for Round 1
      InputRouter.offer("0") // P1
      InputRouter.offer("0") // P2
      InputRouter.offer("0") // P3
      
      // Trick 1 for Round 1
      InputRouter.offer("1") // P1 plays card 1
      InputRouter.offer("1") // P2 plays card 1
      InputRouter.offer("1") // P3 plays card 1
      
      // Round 2
      // Dealer.shuffleCards() called, Dealer.index = 0.
      // Trump card index = (2 * 3) % 60 = 6. myCards(6) is Wizard of Blue.
      // Since it's a Wizard, WizardCardState.handleTrump is called.
      // It expects one more input for trump color choice.
      InputRouter.offer("1") // Choose Red as trump
      
      // Hands for Round 2 (2 cards each)
      // P1: myCards(0), myCards(1)
      // P2: myCards(2), myCards(3)
      // P3: myCards(4), myCards(5)
      // (myCards(6) is trump, so it's excluded if drawn, but it's at index 6, so not drawn yet)
      
      // Bids for Round 2
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      // Trick 1 for Round 2
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      // Trick 2 for Round 2
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      // Ensure we have enough inputs just in case
      for (_ <- 1 to 50) InputRouter.offer("1")

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
      for (_ <- 1 to 20) InputRouter.offer("__GAME_STOPPED__")
      
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
        Await.result(promise.future, 30.seconds)
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
