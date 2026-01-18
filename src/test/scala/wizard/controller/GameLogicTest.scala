package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.model.player.{Player, Human}
import wizard.actionmanagement.{AskForPlayerCount, AskForPlayerNames, StartGame}

class GameLogicTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  "GameLogic" should {
    val gameLogic = new GameLogic

    "validate player count correctly" in {
      gameLogic.validGame(2) should be(false)
      gameLogic.validGame(3) should be(true)
      gameLogic.validGame(4) should be(true)
      gameLogic.validGame(5) should be(true)
      gameLogic.validGame(6) should be(true)
      gameLogic.validGame(7) should be(false)
    }

    "handle player count selection" in {
      var lastMsg = ""
      gameLogic.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })

      gameLogic.playerCountSelected(4)
      lastMsg should be("AskForPlayerNames")
      
      gameLogic.playerCountSelected(2)
      lastMsg should be("AskForPlayerNames") 
    }

    "reset player count selection" in {
      var lastMsg = ""
      gameLogic.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })
      
      gameLogic.playerCountSelected(4)
      gameLogic.resetPlayerCountSelection()
      lastMsg should be("AskForPlayerCount")
    }

    "start game correctly" in {
      var lastMsg = ""
      gameLogic.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })
      
      gameLogic.start()
      lastMsg should be("AskForPlayerCount")
      
      // Coverage for line 52: if (started) return
      lastMsg = ""
      gameLogic.start()
      lastMsg should be("")
    }

    "handle setPlayer" in {
      var lastMsg = ""
      gameLogic.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })
      gameLogic.setPlayer(3)
      lastMsg should be("AskForPlayerNames")
    }

    "handle CardAuswahl" in {
      var lastMsg = ""
      gameLogic.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })
      gameLogic.CardAuswahl()
      lastMsg should be("CardAuswahl")
    }

    "handle undo/redo with notifications" in {
      var lastMsg = ""
      gameLogic.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })
      
      gameLogic.undo()
      lastMsg should be("UndoPerformed")
      
      gameLogic.redo()
      lastMsg should be("RedoPerformed")
    }
    
    "handle setPlayers (briefly)" in {
        val players = List(Human.create("P1").get, Human.create("P2").get, Human.create("P3").get)
        noException should be thrownBy gameLogic.setPlayers(players)
    }
  }
}
