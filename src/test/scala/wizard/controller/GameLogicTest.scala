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

    "handle setPlayers with empty list for coverage" in {
        val gl = new GameLogic
        noException should be thrownBy gl.setPlayers(Nil)
        noException should be thrownBy gl.setPlayersFromRedo(Nil)
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

      gl.setCanSave(false)
      gl.save("test_game")
      lastMsg should be("SaveNotAllowed")

      val p1 = Human.create("Alice").get
      val p2 = Human.create("Bob").get
      val p3 = Human.create("Charlie").get
      val players = List(p1, p2, p3)

      gl.setPlayers(players)
      gl.setCanSave(true)
      
      val title = "test_save_logic"
      gl.save(title)
      val extension = ".xml"
      val file = new java.io.File(title + extension)
      file.exists() should be(true)

      gl.load(title)
      lastMsg should be("GameLoaded")
      lastObj shouldBe a [wizard.model.Game]
      
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

    "check interactive status correctly" in {
      val gl = new GameLogic
      val oldProp = sys.props.get("WIZARD_INTERACTIVE")
      
      try {
        sys.props("WIZARD_INTERACTIVE") = "true"
        gl.isInteractive should be(true)
        
        sys.props("WIZARD_INTERACTIVE") = "0"
        val expected = (System.console() != null && sys.env.get("GITHUB_ACTIONS").isEmpty)
        gl.isInteractive should be(expected)

        sys.props("WIZARD_INTERACTIVE") = "false"
        gl.isInteractive should be(expected)
      } finally {
        oldProp match {
          case Some(v) => sys.props("WIZARD_INTERACTIVE") = v
          case None => sys.props.remove("WIZARD_INTERACTIVE")
        }
      }
    }

    "cover additional lines in playGame and save" in {
      val gl = new GameLogic

      val jsonGl = new GameLogic {
        override val fileIo: wizard.model.fileIoComponent.FileIOInterface = new wizard.model.fileIoComponent.fileIoJsonImpl.FileIO
      }
      jsonGl.setCanSave(true)
      jsonGl.save("test_json_branch")
      new java.io.File("test_json_branch.json").exists() should be (true)
      new java.io.File("test_json_branch.json").delete()

      val emptyGl = new GameLogic
      emptyGl.setCanSave(true)
      noException should be thrownBy emptyGl.save("test_empty_players")
      new java.io.File("test_empty_players.xml").delete()

      val players = List(Human.create("P1").get, Human.create("P2").get, Human.create("P3").get)
      gl.stopGame()
      noException should be thrownBy gl.playGame(players, 1, 1)
    }

    "cover methods in GameLogic companion object" in {
      GameLogic.validGame(4) should be(true)
      GameLogic.validGame(2) should be(false)

      val mockGame = wizard.model.Game(Nil)
      mockGame.rounds = 0
      GameLogic.isOver(mockGame) should be(true)
      mockGame.rounds = 5
      GameLogic.isOver(mockGame) should be(false)

      val players = List(wizard.model.player.Human.create("Test").get)
      noException should be thrownBy GameLogic.playGame(mockGame, players)
    }

    "cover undo/redo catch blocks with failing commands" in {
        import wizard.undo.{Command, UndoService}
        val gl = new GameLogic
        
        val failingUndoCommand = new Command {
            override def doStep(): Unit = ()
            override def undoStep(): Unit = throw new RuntimeException("Fail in undoStep")
            override def redoStep(): Unit = ()
        }
        
        UndoService.manager.doStep(failingUndoCommand)
        noException should be thrownBy gl.undo()
        
        var failDoStep = false
        val customCommand = new Command {
            override def doStep(): Unit = if (failDoStep) throw new RuntimeException("Fail in doStep")
            override def undoStep(): Unit = ()
            override def redoStep(): Unit = ()
        }
        
        UndoService.manager.doStep(customCommand)
        gl.undo()
        failDoStep = true
        noException should be thrownBy gl.redo()
    }
  }
}
