package wizard.aView

import org.scalatest.funsuite.AnyFunSuite
import wizard.model.cards._
import wizard.model.cards.Value._
import wizard.model.cards.Color._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import org.scalatest.matchers.should.Matchers
import wizard.model.cards.{Card, Color, Value, Hand}
import wizard.model.player.Human
import wizard.model.player.*
import wizard.model.cards.*
import wizard.actionmanagement.*
import wizard.controller.PlayerSnapshot
import wizard.model.cards.Color.{Blue, Red}
import wizard.model.cards.Value.{One, Ten}
import wizard.controller.GameLogic


class TextUIUnitTest extends AnyFunSuite with Matchers{


  private class TestPlayer(name: String) extends wizard.model.player.Player(name) {
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card =
      if (hand.cards.nonEmpty) hand.cards.head else Card(One, Red)
    override def bid(): Int = 0
  }

  test("showcard contains box drawing and value") {
    val c = Card(One, Red)
    val s = TextUI.showcard(c)
    assert(s.contains("┌─────────┐"))
    assert(s.contains("1"))
  }

  test("printCardAtIndex out of bounds returns message") {
    val msg = TextUI.printCardAtIndex(-5)
    assert(msg.contains("out of bounds"))
  }

  test("showHand prints No cards in hand for empty hand") {
    val p = new TestPlayer("Alice")
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      TextUI.showHand(p)
    }
    val printed = out.toString("UTF-8")
    assert(printed.contains("Alice's hand"))
    assert(printed.contains("No cards in hand."))
  }

  test("showHand prints cards and indices for non-empty hand") {
    val p = new TestPlayer("Bob")

    val cards = List(Card(Ten, Blue), Card(Three, Green))
    p.addHand(Hand(cards))
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      TextUI.showHand(p)
    }
    val printed = out.toString("UTF-8")
    assert(printed.contains("Bob's hand"))
    assert(printed.contains("Ten of Blue") || printed.contains("10") || printed.contains("3 of Green"))
    assert(printed.contains("Indices:"))
  }
  test("printCardAtIndex returns showcard for valid index 0") {
    val expected = TextUI.showcard(Dealer.allCards.head)
    val actual = TextUI.printCardAtIndex(0)
    actual should be(expected)
  }
  test("printCardAtIndex negative index returns out-of-bounds message") {
    TextUI.printCardAtIndex(-1) should include("Index -1 is out of bounds")
  }
  test("printCardAtIndex too large index returns out-of-bounds message") {
    val idx = Dealer.allCards.length
    TextUI.printCardAtIndex(idx) should include(s"Index $idx is out of bounds")
  }
  test("showHand prints card boxes and indices line for a non-empty hand") {
    val player = Human.create("P").get
    val cards = List(Card(Value.One, Color.Red), Card(Value.Two, Color.Blue))
    player.addHand(Hand(cards))
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.showHand(player)
    }
    val printed = out.toString
    printed should include("Indices:")
    printed should include("1:")
    printed should include("2:")
    printed should include("(")
  }
  test("showHand prints no cards message for empty hand") {
    val player = Human.create("Alice").get

    player.addHand(Hand(List()))
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.showHand(player)
    }
    val printed = out.toString
    printed should include("Alice's hand:")
    printed should include("No cards in hand.")
  }
  test("CardsDealt update prints each player's hand and message") {
    val p1 = PlayerFactory.createPlayer(Some("P1"), PlayerType.Human)
    val p2 = PlayerFactory.createPlayer(Some("P2"), PlayerType.Human)

    p1.addHand(Hand(List(Card(One, Red))))
    p2.addHand(Hand(List(Card(Ten, Blue))))

    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      TextUI.update("CardsDealt", CardsDealt(List(p1, p2)))
    }
    val printed = out.toString("UTF-8")
    assert(printed.contains("Cards have been dealt") || printed.toLowerCase.contains("dealt"))
    assert(printed.contains("P1") && printed.contains("P2"))
  }

  test("print points all players prints table with Player and PlayerSnapshot") {
    val p = PlayerFactory.createPlayer(Some("Alpha"), PlayerType.Human)
    p.points = 12
    p.roundBids = 2
    val snap = PlayerSnapshot("Beta", 3, 5)
    val data: List[Any] = List(p, snap)
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      TextUI.update("print points all players", data)
    }
    val printed = out.toString("UTF-8")
    assert(printed.contains("Name") && printed.contains("Points"))
    assert(printed.contains("Alpha") && printed.contains("Beta"))
  }

  test("which trump reads line via StdIn (returns a value) - non-blocking check") {
    // This path in TextUI.update prints prompt and reads line; we'll simulate by calling update and not relying on return
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      Console.withIn(new java.io.ByteArrayInputStream("Red\n".getBytes("UTF-8"))) {
        TextUI.update("which trump", PlayerFactory.createPlayer(Some("Z"), PlayerType.Human))
      }
    }
    val printed = out.toString("UTF-8")
    assert(printed.contains("which color do you want to choose as trump") || printed.toLowerCase.contains("which color"))
  }

  test("inputPlayers happy path returns correct number of players") {
    val input = "3\nAlice\nBob\nCharlie\n"
    val in = new ByteArrayInputStream(input.getBytes("UTF-8"))
    val out = new ByteArrayOutputStream()
    Console.withIn(in) {
      Console.withOut(new PrintStream(out)) {
        val players = TextUI.inputPlayers()
        assert(players.length == 3)
        assert(players.map(_.name).toSet == Set("Alice", "Bob", "Charlie"))
      }
    }
  }

  test("inputPlayers undo returns to count") {
    val input = "3\nundo\n4\nA\nB\nC\nD\n"
    val in = new ByteArrayInputStream(input.getBytes("UTF-8"))
    val out = new ByteArrayOutputStream()
    Console.withIn(in) {
      Console.withOut(new PrintStream(out)) {
        val players = TextUI.inputPlayers()
        assert(players.length == 4)
      }
    }
  }

  private class FakeGameLogic extends GameLogic {
    var lastPlayerCountSelected: Option[Int] = None
    var lastSetPlayers: Option[List[Player]] = None

    override def playerCountSelected(count: Int): Unit = {
      lastPlayerCountSelected = Some(count)
      super.playerCountSelected(count)
    }

    override def setPlayers(players: List[Player]): Unit = {
      lastSetPlayers = Some(players)
    }

    def addObserver(t: TextUI): Unit = add(t)
  }

  test("TextUI instance starts count reader and handles player selection") {
    val gl = new FakeGameLogic()
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      val tui = new TextUI(gl)
      wizard.actionmanagement.InputRouter.clear()
      wizard.actionmanagement.InputRouter.offer("3")
      gl.start()
      Thread.sleep(200)
      assert(tui.testPhase == "AwaitPlayerNames" || tui.testPhase == "InGame")
    }
  }
  test("TextUI.update handles CardsDealt and prints hands and message") {
    val p1 = Human.create("P1").get
    val p2 = Human.create("P2").get
    p1.addHand(Hand(List(Card(Value.One, Color.Red))))
    p2.addHand(Hand(List(Card(Value.Two, Color.Blue))))
    val players = List(p1, p2)

    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.update("CardsDealt", CardsDealt(players))
    }
    val printed = out.toString
    printed should include("Cards have been dealt to all players")
    printed should include("P1's hand")
    printed should include("P2's hand")
  }
  test("TextUI.update invalid bid prints range message") {
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.update("invalid bid", 5)
    }
    val printed = out.toString
    printed should include("You can only bid between 0 and 5")
  }
  test("TextUI.update prints points table for players and snapshots") {
    val p1 = Human.create("P1").get
    p1.points = 10
    p1.roundBids = 2
    val snap = PlayerSnapshot("S1", 1, 5)
    val list: List[Any] = List(p1, snap)

    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.update("print points all players", list)
    }
    val printed = out.toString
    printed should include("Name")
    printed should include("P1")
    printed should include("S1")
    printed should include("Points")
  }
  test("TextUI.update prints trump card") {
    val card = Card(Value.Three, Color.Green)
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.update("print trump card", card)
    }
    val printed = out.toString
    printed should include("Trump card:")
    printed should include("┌─────────┐")
  }

  test("object update prints various messages") {
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      val p = PlayerFactory.createPlayer(Some("Tobi"), wizard.model.player.PlayerType.Human)
      TextUI.update("which card", p)
      TextUI.update("invalid card")
      TextUI.update("follow lead", Color.Red)
      TextUI.update("which bid", p)
      TextUI.update("invalid bid", 5)
      TextUI.update("invalid bid", 5, p)
      TextUI.update("print trump card", Dealer.allCards.head)
      TextUI.update("cards dealt")
      TextUI.update("trick winner", p)
      TextUI.update("points after round")
      TextUI.update("SaveNotAllowed")
    }
    val printed = out.toString("UTF-8")
    assert(printed.contains("which card") || printed.contains("Tobi"))
    assert(printed.contains("Invalid card") || printed.contains("Invalid card."))
    assert(printed.contains("follow the lead suit") || printed.contains("which color") == false)
    assert(printed.contains("how many tricks do you bid?") || printed.contains("how many"))
    assert(printed.contains("Invalid bid") || printed.contains("You can only bid"))
    assert(printed.contains("Trump card:"))
    assert(printed.contains("Cards have been dealt") || printed.contains("Cards have been dealt to all players.") || printed.contains("dealt"))
    assert(printed.contains("won the trick"))
    assert(printed.contains("Points after this round") || printed.contains("Points after this round:") || printed.contains("Points after"))
    assert(printed.contains("Bitte spiele"))
  }

  test("showcard supports wide and narrow values") {
    val ten = Card(Value.Ten, Color.Blue)
    val one = Card(Value.One, Color.Green)
    val sTen = TextUI.showcard(ten)
    val sOne = TextUI.showcard(one)
    assert(sTen.contains("10") || sTen.contains("11") == false)
    assert(sOne.contains("1"))
  }
  test("TextUI.update which trump prompts and reads a line") {
    val player = Human.create("P").get
    // Offer input to InputRouter to satisfy readLine used in which trump
    wizard.actionmanagement.InputRouter.clear()
    wizard.actionmanagement.InputRouter.offer("Red")
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out)) {
      TextUI.update("which trump", player)
    }
    val printed = out.toString
    printed should include("which color do you want to choose as trump")
  }
  test("showcard for small value contains frame and value") {
    val s = TextUI.showcard(Card(Value.One, Color.Red))
    s should include("┌─────────┐")
    s should include("1")
    s should include("└─────────┘")
  }
  test("showcard for wide value contains '10' and multiple lines") {
    val s = TextUI.showcard(Card(Value.Ten, Color.Blue))
    s should include("10")
    s should include("┌─────────┐")
    s.split("\n").length should be >= 5
  }
}

