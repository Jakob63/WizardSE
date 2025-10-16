package wizard.aView

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import wizard.actionmanagement.InputRouter
import wizard.controller.GameLogic

class TextUIQuietOnEmptyNameInputTest extends AnyWordSpec with Matchers {
  "TextUI name prompt" should {
    "not print an error when an empty line arrives before user starts typing" in {
      val gl = new GameLogic
      val tui = new TextUI(gl)

      // Prepare: controller will ask for player count on start
      // Feed a valid count so we proceed to AskForPlayerNames
      InputRouter.clear()
      InputRouter.offer("3")

      // Now simulate an empty line arriving (e.g., stray feeder artifact)
      InputRouter.offer("")

      // Then provide three valid names
      InputRouter.offer("Alice")
      InputRouter.offer("Bob")
      InputRouter.offer("Cara")

      // Capture output briefly to ensure no immediate invalid-name error was printed
      val baos = new java.io.ByteArrayOutputStream()
      Console.withOut(baos) {
        // Trigger start
        gl.start()
        // Wait a little for background name-reader to consume inputs
        Thread.sleep(150)
      }
      val out = baos.toString("UTF-8")

      // It should contain the prompts but not the invalid name error
      out should include ("Enter the number of players (3-6):")
      out should include ("Enter the name of player 1 (or type 'undo'/'redo'):")
      out should not include ("Invalid name. Please enter a name containing only letters and numbers.")
    }
  }
}
