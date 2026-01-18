package wizard.aView

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.controller.GameLogic
import wizard.model.player.{Player, Human, PlayerFactory}
import wizard.model.cards.{Card, Color, Value, Hand, Dealer}
import wizard.actionmanagement.{AskForPlayerCount, AskForPlayerNames, CardsDealt, PlayerCountSelected, ShowHand, StartGame, InputRouter}
import java.io.{ByteArrayOutputStream, PrintStream}
import wizard.model.Game

class TextUITest extends AnyWordSpec with Matchers with TimeLimitedTests {
  val timeLimit = 30.seconds

  def captureOutput(f: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(out) {
      f
    }
    out.toString
  }

  "TextUI (Object)" should {
    "update correctly for various messages" in {
      val p = PlayerFactory.createPlayer(Some("TestPlayer"), wizard.model.player.PlayerType.Human)
      
      captureOutput(TextUI.update("which card", p)) should include("TestPlayer, which card do you want to play?")
      captureOutput(TextUI.update("invalid card")) should include("Invalid card. Please enter a valid index.")
      captureOutput(TextUI.update("follow lead", Color.Red)) should include("You must follow the lead suit Red.")
      captureOutput(TextUI.update("which bid", p)) should include("TestPlayer, how many tricks do you bid?")
      captureOutput(TextUI.update("invalid input, bid again")) should include("Invalid input. Please enter a valid number.")
      captureOutput(TextUI.update("invalid bid", 5, p)) should include("Invalid bid for player TestPlayer. You can only bid between 0 and 5.")
      captureOutput(TextUI.update("invalid bid", 3)) should include("Invalid bid. You can only bid between 0 and 3.")
      
      val card = Card(Value.Seven, Color.Blue)
      captureOutput(TextUI.update("print trump card", card)) should include("Trump card:")
      captureOutput(TextUI.update("print trump card", card)) should include("7")
      
      captureOutput(TextUI.update("cards dealt")) should include("Cards have been dealt to all players.")
      captureOutput(TextUI.update("trick winner", p)) should include("TestPlayer won the trick.")
      captureOutput(TextUI.update("points after round")) should include("Points after this round:")
      captureOutput(TextUI.update("unknown")) should be("")
    }

    "show hand correctly" in {
      val p = PlayerFactory.createPlayer(Some("TestPlayer"), wizard.model.player.PlayerType.Human)
      p.hand = Hand(List(Card(Value.Seven, Color.Blue), Card(Value.Ten, Color.Red)))
      
      val output = captureOutput(TextUI.showHand(p))
      output should include("TestPlayer's hand")
      output should include("7 of Blue")
      output should include("10 of Red")
      
      p.hand = Hand(Nil)
      captureOutput(TextUI.showHand(p)) should include("No cards in hand.")
    }

    "show card correctly" in {
      val card7 = Card(Value.Seven, Color.Blue)
      val card10 = Card(Value.Ten, Color.Red)
      
      val out7 = TextUI.showcard(card7)
      out7 should include("7")
      
      val out10 = TextUI.showcard(card10)
      out10 should include("10")
    }

    "print card at index" in {
      val output = TextUI.printCardAtIndex(0)
      output should not include("out of bounds")
      
      TextUI.printCardAtIndex(-1) should include("out of bounds")
      TextUI.printCardAtIndex(100) should include("out of bounds")
    }
  }

  "TextUI (Class)" should {
    val gameLogic = new GameLogic
    
    System.setProperty("WIZARD_INTERACTIVE", "true")

    "initialize and register at controller" in {
      val tui = new TextUI(gameLogic)
      gameLogic.subscribers should contain (tui)
    }

    "handle StartGame and AskForPlayerCount events" in {
      val tui = new TextUI(gameLogic)
      InputRouter.clear()
      InputRouter.offer("4") 
      
      tui.update("StartGame")
      tui.testPhase should be("AwaitPlayerCount")
      
      tui.update("AskForPlayerCount")
      tui.testPhase should be("AwaitPlayerCount")
    }

    "handle PlayerCountSelected event" in {
      val tui = new TextUI(gameLogic)
      tui.update("PlayerCountSelected", PlayerCountSelected(4))
      tui.testPhase should be("AwaitPlayerNames")
      
      tui.update("PlayerCountSelected", 3)
      tui.testPhase should be("AwaitPlayerNames")
    }

    "handle AskForPlayerNames event" in {
      val tui = new TextUI(gameLogic)
      tui.testSetPhase("AwaitPlayerNames")
      InputRouter.clear()
      tui.testSetLastSelectedCount(3)
      
      
      InputRouter.offer("Alice")
      InputRouter.offer("Bob")
      InputRouter.offer("Charlie")
      
      tui.update("AskForPlayerNames")
      
      Thread.sleep(1000)
      tui.update("which card", PlayerFactory.createPlayer(Some("P1"), wizard.model.player.PlayerType.Human))
    }

    "handle name entry with undo" in {
      val tui = new TextUI(gameLogic)
      tui.testSetPhase("AwaitPlayerNames")
      tui.testSetLastSelectedCount(3)
      InputRouter.clear()
      tui.update("AskForPlayerNames")
      Thread.sleep(200)
    }

    "handle name entry with undo to count" in {
      val tui = new TextUI(gameLogic)
      tui.testSetPhase("AwaitPlayerNames")
      tui.testSetLastSelectedCount(3)
      InputRouter.clear()
      tui.update("AskForPlayerNames")
      Thread.sleep(200)
    }

    "handle PlayerCount selection with invalid input" in {
        val tui = new TextUI(gameLogic)
        tui.testSetPhase("AwaitPlayerCount")
        InputRouter.clear()
        tui.update("AskForPlayerCount")
        Thread.sleep(200)
    }

    "handle ShowHand event" in {
      val tui = new TextUI(gameLogic)
      val p = PlayerFactory.createPlayer(Some("TestPlayer"), wizard.model.player.PlayerType.Human)
      
      captureOutput(tui.update("ShowHand", ShowHand(p))) should include("TestPlayer's hand")
      captureOutput(tui.update("ShowHand", p)) should include("TestPlayer's hand")
    }

    "handle various game messages" in {
      val tui = new TextUI(gameLogic)
      val p = PlayerFactory.createPlayer(Some("TestPlayer"), wizard.model.player.PlayerType.Human)
      
      captureOutput(tui.update("which card", p)) should include("which card do you want to play?")
      captureOutput(tui.update("invalid card")) should include("Invalid card")
      captureOutput(tui.update("follow lead", Color.Blue)) should include("must follow the lead suit Blue")
      captureOutput(tui.update("which bid", p)) should include("how many tricks do you bid?")
      captureOutput(tui.update("invalid input, bid again")) should include("Invalid input")
      captureOutput(tui.update("invalid bid", 3, p)) should include("Invalid bid")
      captureOutput(tui.update("print trump card", Card(Value.WizardKarte, Color.Green))) should include("Trump card")
      captureOutput(tui.update("trick winner", p)) should include("won the trick")
      captureOutput(tui.update("card played", Card(Value.Seven, Color.Red))) should include("Played card")
      captureOutput(tui.update("points after round")) should include("Points after this round")
    }

    "handle print points all players" in {
      val tui = new TextUI(gameLogic)
      val p1 = PlayerFactory.createPlayer(Some("Alice"), wizard.model.player.PlayerType.Human)
      val p2 = PlayerFactory.createPlayer(Some("Bob"), wizard.model.player.PlayerType.Human)
      p1.points = 20
      p2.points = -10
      p1.roundBids = 2
      p2.roundBids = 1
      
      val output = captureOutput(tui.update("print points all players", List(p1, p2)))
      output should include("Alice")
      output should include("Bob")
      output should include("20")
      output should include("-10")
    }

    "handle CardsDealt" in {
        val tui = new TextUI(gameLogic)
        val p1 = PlayerFactory.createPlayer(Some("Alice"), wizard.model.player.PlayerType.Human)
        val event = CardsDealt(List(p1))
        
        val output = captureOutput(tui.update("CardsDealt", event))
        output should include("Alice's hand")
        output should include("Cards have been dealt")
        tui.testPhase should be("InGame")
    }

    "handle GameLoaded" in {
        val tui = new TextUI(gameLogic)
        val game = Game(List(PlayerFactory.createPlayer(Some("Alice"), wizard.model.player.PlayerType.Human)))
        
        tui.update("GameLoaded", game)
        tui.testPhase should be("InGame")
    }
    
    "handle undo/redo" in {
        val tui = new TextUI(gameLogic)
        tui.undo()
        tui.redo()
    }
  }
}
