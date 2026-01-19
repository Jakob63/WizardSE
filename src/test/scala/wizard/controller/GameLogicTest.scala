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

    "handle save and load" in {
      val gl = new GameLogic
      var lastMsg = ""
      var lastObj: Any = null
      gl.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          if (obj.nonEmpty) lastObj = obj.head
          ()
        }
      })

      // Test save not allowed
      gl.setCanSave(false)
      gl.save("test_game")
      lastMsg should be("SaveNotAllowed")

      // Setup for successful save/load
      val p1 = Human.create("Alice").get
      val p2 = Human.create("Bob").get
      val p3 = Human.create("Charlie").get
      val players = List(p1, p2, p3)
      
      // We need to set up the state of GameLogic manually since we don't want to run the full game thread
      // However, currentPlayers and currentRoundNum are private. 
      // But we can use setPlayers to initialize some state.
      gl.setPlayers(players)
      gl.setCanSave(true)
      
      val title = "test_save_logic"
      gl.save(title)
      // Since it uses the real FileIO (XML by default), it should create a file.
      val extension = ".xml" // Default in WizardModule
      val file = new java.io.File(title + extension)
      file.exists() should be(true)

      // Test load
      gl.load(title)
      lastMsg should be("GameLoaded")
      lastObj shouldBe a [wizard.model.Game]
      
      // Clean up
      file.delete()
    }

    "handle load failure" in {
      val gl = new GameLogic
      var lastMsg = ""
      gl.add(new wizard.actionmanagement.Observer {
        override def update(msg: String, obj: Any*): Any = {
          lastMsg = msg
          ()
        }
      })

      gl.load("non_existent_file_12345")
      lastMsg should be("LoadFailed")
    }
  }
}
