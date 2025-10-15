package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.controller.GameLogic
import java.io.{ByteArrayOutputStream, PrintStream}

class TextUINamePromptGuardTest extends AnyWordSpec with Matchers {
  "TextUI" should {
    "not prompt for player names during InGame phase when AskForPlayerNames event arrives" in {
      val controller = new GameLogic
      val tui = new TextUI(controller)

      // Force phase to InGame (using test helper) and send AskForPlayerNames
      tui.testSetPhase("InGame")

      val baos = new ByteArrayOutputStream()
      val ps   = new PrintStream(baos)
      Console.withOut(ps) {
        tui.update("AskForPlayerNames")
      }
      val out = baos.toString("UTF-8")
      out should not include ("Enter the name of player")
    }
  }
}
