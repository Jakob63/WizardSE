package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.InputRouter

class TextUIIgnoresBackToCountSentinelAtCountPromptTest extends AnyWordSpec with Matchers {
  "TextUI count prompt" should {
    "ignore internal __BACK_TO_COUNT__ sentinel and not print invalid message" in {
      System.setProperty("WIZARD_INTERACTIVE", "1")
      val controller = new GameLogic
      val tui = new TextUI(controller)

      val out = new java.io.ByteArrayOutputStream()
      Console.withOut(out) {
        controller.start()
        // Simulate that a back-to-count sentinel appears (e.g., due to a previous name reader cancel)
        InputRouter.offer("__BACK_TO_COUNT__")
        // Then provide a valid player count to continue
        InputRouter.offer("3")
        Thread.sleep(120)
      }

      // After valid input, TextUI should transition to asking for player names
      tui.testPhase mustBe "AwaitPlayerNames"
      val output = out.toString
      output.contains("Invalid number of players. Please enter a number between 3 and 6.") mustBe false
    }
  }
}
