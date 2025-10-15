package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.InputRouter
import wizard.actionmanagement.Debug

class TextUIQuietOnEmptyCountInputTest extends AnyWordSpec with Matchers {
  "TextUI count prompt" should {
    "not print 'Invalid number of players' on empty input and proceed when valid input arrives" in {
      System.setProperty("WIZARD_INTERACTIVE", "1")
      val controller = new GameLogic
      val tui = new TextUI(controller)

      val out = new java.io.ByteArrayOutputStream()
      Console.withOut(out) {
        // Start game to trigger AskForPlayerCount
        controller.start()
        // Simulate an empty line first (e.g., no console input yet)
        InputRouter.offer("")
        // Then provide a valid player count
        InputRouter.offer("3")
        // Allow background reader to process
        Thread.sleep(100)
      }

      // TextUI should have advanced to AwaitPlayerNames phase
      tui.testPhase mustBe "AwaitPlayerNames"
      // Ensure that the classic invalid message is not printed for the empty input
      val output = out.toString
      output.contains("Invalid number of players. Please enter a number between 3 and 6.") mustBe false
    }
  }
}
