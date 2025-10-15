package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.actionmanagement.CardsDealt
import wizard.model.player.{PlayerFactory, PlayerType}
import wizard.controller.GameLogic
import java.io.{ByteArrayOutputStream, PrintStream}

class TextUICardsDealtTest extends AnyWordSpec with Matchers {
  "TextUI" should {
    "print all players' hands on CardsDealt before bidding" in {
      val controller = new GameLogic
      val tui = new TextUI(controller)
      val players = List("A", "B", "C").map(n => PlayerFactory.createPlayer(Some(n), PlayerType.Human))

      val baos = new ByteArrayOutputStream()
      val ps   = new PrintStream(baos)
      Console.withOut(ps) {
        tui.update("CardsDealt", CardsDealt(players))
      }
      val out = baos.toString("UTF-8")
      out should include ("Cards have been dealt to all players.")
      // With empty hands in this unit test, showHand prints "No cards in hand." per player
      out.split("No cards in hand.").length - 1 shouldBe players.length
    }
  }
}
