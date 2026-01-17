package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.{Player, Human}
import wizard.model.cards.{Card, Color, Value, Dealer}
import wizard.actionmanagement.{InputRouter, GameStoppedException, Observer}
import scala.concurrent.{Promise, Await}
import scala.concurrent.duration.*

class GameIntegrationTest extends AnyWordSpec with Matchers {

  "GameLogic Integration" should {

    "simulate a full game cycle with multiple rounds" in {
      // WIZARD_INTERACTIVE = false für testing 
      // damit TextUI keine eigenen Threads startet
      System.setProperty("WIZARD_INTERACTIVE", "false")
      
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

      // Runde 1
      // Bids: P1 bids 0, P2 bids 1, P3 bids 0
      InputRouter.offer("0")
      InputRouter.offer("1")
      InputRouter.offer("0")
      
      Dealer.allCards = (for {
        c <- Color.values.toList
        v <- Value.values.toList
      } yield Card(v, c))
      
      // Runde 1
      // Trumpfkarte bei Index 1*3 = 3 -> Card(Value.Four, Color.Red)
      // P1 Hand: Card(Value.Chester, Color.Red)
      // P2 Hand: Card(Value.One, Color.Red)
      // P3 Hand: Card(Value.Two, Color.Red)
      
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")
      
      // Runde 2: 2 Karten pro Spieler
      // Bids: P1 bids 1, P2 bids 0, P3 bids 1
      InputRouter.offer("1")
      InputRouter.offer("0")
      InputRouter.offer("1")
      
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")
      
      InputRouter.offer("1")
      InputRouter.offer("1")
      InputRouter.offer("1")

      gameLogic.playGame(players, maxRounds, 0)
      
      roundsFinished should be(maxRounds)
      players.foreach(_.points should not be 0)
      
      // Cleanup
      System.setProperty("WIZARD_INTERACTIVE", "true")
    }

    "propagate GameStoppedException correctly" in {
      val gameLogic = new GameLogic
      val p1 = Human.create("P1").get
      val p2 = Human.create("P2").get
      val p3 = Human.create("P3").get
      val players = List(p1, p2, p3)
      
      InputRouter.offer("__GAME_STOPPED__")
      
      val promise = Promise[Unit]()
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          gameLogic.playGame(players, 5, 0)
          promise.success(())
        }
      })
      t.start()
      
      Await.result(promise.future, 2.seconds)
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
