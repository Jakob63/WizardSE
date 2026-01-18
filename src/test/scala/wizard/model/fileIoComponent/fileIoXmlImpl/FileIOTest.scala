package wizard.model.fileIoComponent.fileIoXmlImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import wizard.model.Game
import wizard.model.cards.{Card, Color, Value, Hand}
import wizard.model.player.{Player, PlayerFactory, PlayerType}
import java.io.File

class FileIOTest extends AnyWordSpec with Matchers with TimeLimitedTests {

  val timeLimit = 30.seconds

  "An XML FileIO" should {
    val fileIo = new FileIO
    val path = "test_game.xml"

    val p1 = PlayerFactory.createPlayer(Some("Alice"), PlayerType.Human)
    p1.points = 100
    p1.roundBids = 2
    p1.hand = Hand(List(Card(Value.Seven, Color.Red), Card(Value.WizardKarte, Color.Blue)))
    
    val p2 = PlayerFactory.createPlayer(Some("Bot"), PlayerType.AI)
    p2.points = 50
    p2.roundBids = 1
    p2.hand = Hand(List(Card(Value.One, Color.Green)))

    val players = List(p1, p2)
    val game = Game(players)
    game.rounds = 20
    game.currentround = 5
    game.currentTrick = List(Card(Value.Eight, Color.Red))
    game.firstPlayerIdx = 1
    
    val trumpCard = Some(Card(Value.Ten, Color.Yellow))
    val dealerIndex = 0

    "save and load a game correctly" in {
      fileIo.save(game, game.currentround, trumpCard, dealerIndex, game.firstPlayerIdx, path)
      
      val (loadedGame, loadedRound, loadedTrump, loadedDealer, loadedFirstPlayer) = fileIo.load(path)
      
      loadedRound should be(game.currentround)
      loadedDealer should be(dealerIndex)
      loadedFirstPlayer should be(game.firstPlayerIdx)
      loadedTrump should be(trumpCard)
      
      loadedGame.rounds should be(game.rounds)
      loadedGame.players.size should be(players.size)
      
      val lp1 = loadedGame.players.head
      lp1.name should be("Alice")
      lp1.points should be(100)
      lp1.roundBids should be(2)
      lp1.hand.cards should contain allOf (Card(Value.Seven, Color.Red), Card(Value.WizardKarte, Color.Blue))
      
      val lp2 = loadedGame.players(1)
      lp2.name should be("Bot")
      lp2.points should be(50)
      lp2.roundBids should be(1)
      lp2.isInstanceOf[wizard.model.player.AI] should be(true)

      loadedGame.currentTrick should be(game.currentTrick)

      // Cleanup
      new File(path).delete()
    }

    "handle loading with missing firstPlayerIdx" in {
      val minimalXml = <game rounds="10" currentRound="1" dealerIndex="0">
        <trumpCard></trumpCard>
        <currentTrick></currentTrick>
        <players></players>
      </game>
      val pw = new java.io.PrintWriter(new File("minimal.xml"))
      pw.write(minimalXml.toString())
      pw.close()

      val (loadedGame, _, _, _, loadedFirstPlayer) = fileIo.load("minimal.xml")
      loadedFirstPlayer should be(0)
      
      new File("minimal.xml").delete()
    }

    "handle no trump card" in {
       val gameNoTrump = Game(players)
       fileIo.save(gameNoTrump, 1, None, 0, 0, "notrump.xml")
       val (_, _, loadedTrump, _, _) = fileIo.load("notrump.xml")
       loadedTrump should be(None)
       new File("notrump.xml").delete()
    }
  }
}
