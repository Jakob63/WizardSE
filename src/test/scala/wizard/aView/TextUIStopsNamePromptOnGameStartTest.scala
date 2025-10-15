package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import wizard.controller.GameLogic
import wizard.actionmanagement.{CardsDealt, InputRouter}
import wizard.model.player.{PlayerFactory, PlayerType}

class TextUIStopsNamePromptOnGameStartTest extends AnyWordSpec with Matchers {
  "TextUI" should {
    "stop name prompts once the game starts (CardsDealt) and not print further name asks" in {
      System.setProperty("WIZARD_INTERACTIVE", "1")
      val controller = new GameLogic
      val tui = new TextUI(controller)

      // Prepare simulated players and set TUI as if waiting for names
      val players = List("A", "B", "C").map(n => PlayerFactory.createPlayer(Some(n), PlayerType.Human))
      tui.testSetPhase("AwaitPlayerNames")
      tui.testSetNameReaderStarted(true)

      val out = new java.io.ByteArrayOutputStream()
      Console.withOut(out) {
        // Simulate that cards have been dealt (game started) and then a bid is requested
        tui.update("CardsDealt", CardsDealt(players))
        tui.update("which bid", players.head)
        // Allow any background prints to flush
        Thread.sleep(20)
      }

      // After CardsDealt, TUI should be in game phase and must not keep asking for names
      tui.testPhase mustBe "InGame"
      val output = out.toString
      output.contains("Enter the name of player") mustBe false
    }
  }
}
