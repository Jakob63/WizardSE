package wizard.model.fileIoComponent.fileIoXmlImpl

import wizard.model.fileIoComponent.FileIOInterface
import wizard.model.Game
import wizard.model.player.{Player, Human, AI}
import wizard.model.cards.{Card, Color, Value, Hand, Dealer}
import scala.xml.{NodeSeq, PrettyPrinter}
import java.io._

class FileIO extends FileIOInterface {

  override def load(path: String): (Game, Int, Option[Card], Int, Int) = {
    val file = scala.xml.XML.loadFile(path)
    val rounds = (file \\ "game" \ "@rounds").text.toInt
    val currentRound = (file \\ "game" \ "@currentRound").text.toInt
    val dealerIndex = (file \\ "game" \ "@dealerIndex").text.toInt
    val firstPlayerIdx = (file \\ "game" \ "@firstPlayerIdx").textOption.getOrElse("0").toInt
    
    val trumpCardOpt = (file \\ "game" \ "trumpCard" \ "card").headOption.map(nodeToCard)

    val playerNodes = (file \\ "game" \ "players" \ "player")
    val players = playerNodes.map(nodeToPlayer).toList

    val trickCards = (file \\ "game" \ "currentTrick" \ "card").map(nodeToCard).toList

    val game = Game(players)
    game.rounds = rounds
    game.currentround = currentRound
    game.currentTrick = trickCards
    game.firstPlayerIdx = firstPlayerIdx
    
    (game, currentRound, trumpCardOpt, dealerIndex, firstPlayerIdx)
  }

  private implicit class NodeSeqOps(ns: NodeSeq) {
    def textOption: Option[String] = if (ns.isEmpty) None else Some(ns.text)
  }

  private def nodeToCard(node: scala.xml.Node): Card = {
    val value = Value.valueOf((node \ "@value").text)
    val color = Color.valueOf((node \ "@color").text)
    Card(value, color)
  }

  private def nodeToPlayer(node: scala.xml.Node): Player = {
    val name = (node \ "@name").text
    val pType = (node \ "@type").text
    val points = (node \ "@points").text.toInt
    val tricks = (node \ "@tricks").text.toInt
    val bids = (node \ "@bids").text.toInt
    val roundPoints = (node \ "@roundPoints").text.toInt
    val roundBids = (node \ "@roundBids").text.toInt
    val roundTricks = (node \ "@roundTricks").text.toInt
    
    val handCards = (node \ "hand" \ "card").map(nodeToCard).toList
    
    val pTypeEnum = if (pType == "AI") wizard.model.player.PlayerType.AI else wizard.model.player.PlayerType.Human
    val player = wizard.model.player.PlayerFactory.createPlayer(Some(name), pTypeEnum)
    
    player.points = points
    player.tricks = tricks
    player.bids = bids
    player.roundPoints = roundPoints
    player.roundBids = roundBids
    player.roundTricks = roundTricks
    player.hand = Hand(handCards)
    player
  }

  override def save(game: Game, currentRound: Int, trumpCard: Option[Card], dealerIndex: Int, firstPlayerIdx: Int, path: String): Unit = {
    val pw = new PrintWriter(new File(path))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(gameToXml(game, currentRound, trumpCard, dealerIndex, firstPlayerIdx))
    pw.write(xml)
    pw.close()
  }

  private def gameToXml(game: Game, currentRound: Int, trumpCard: Option[Card], dealerIndex: Int, firstPlayerIdx: Int) = {
    <game rounds={game.rounds.toString} currentRound={currentRound.toString} dealerIndex={dealerIndex.toString} firstPlayerIdx={firstPlayerIdx.toString}>
      <trumpCard>
        {trumpCard.map(cardToXml).getOrElse(NodeSeq.Empty)}
      </trumpCard>
      <currentTrick>
        {game.currentTrick.map(cardToXml)}
      </currentTrick>
      <players>
        {game.players.map(playerToXml)}
      </players>
    </game>
  }

  private def playerToXml(player: Player) = {
    <player name={player.name} 
            type={if (player.isInstanceOf[AI]) "AI" else "Human"}
            points={player.points.toString}
            tricks={player.tricks.toString}
            bids={player.bids.toString}
            roundPoints={player.roundPoints.toString}
            roundBids={player.roundBids.toString}
            roundTricks={player.roundTricks.toString}>
      <hand>
        {player.hand.cards.map(cardToXml)}
      </hand>
    </player>
  }

  private def cardToXml(card: Card) = {
    <card value={card.value.toString} color={card.color.toString}/>
  }
}
