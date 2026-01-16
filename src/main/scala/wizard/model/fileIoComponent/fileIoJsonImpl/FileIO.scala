package wizard.model.fileIoComponent.fileIoJsonImpl

import wizard.model.fileIoComponent.FileIOInterface
import wizard.model.Game
import wizard.model.player.{Player, Human, AI}
import wizard.model.cards.{Card, Color, Value, Hand}
import play.api.libs.json._
import java.io._

class FileIO extends FileIOInterface {

  override def load(path: String): (Game, Int, Option[Card], Int, Int) = {
    val source: String = scala.io.Source.fromFile(path).getLines.mkString
    val json: JsValue = Json.parse(source)
    
    val rounds = (json \ "rounds").as[Int]
    val currentRound = (json \ "currentRound").as[Int]
    val dealerIndex = (json \ "dealerIndex").as[Int]
    val firstPlayerIdx = (json \ "firstPlayerIdx").asOpt[Int].getOrElse(0)
    
    val trumpCardOpt = (json \ "trumpCard").asOpt[JsValue].flatMap {
      case JsNull => None
      case other => Some(jsonToCard(other))
    }
    
    val players = (json \ "players").as[List[JsValue]].map(jsonToPlayer)
    
    val currentTrick = (json \ "currentTrick").asOpt[List[JsValue]].getOrElse(Nil).map(jsonToCard)
    
    val game = Game(players)
    game.rounds = rounds
    game.currentround = currentRound
    game.currentTrick = currentTrick
    game.firstPlayerIdx = firstPlayerIdx
    
    (game, currentRound, trumpCardOpt, dealerIndex, firstPlayerIdx)
  }

  private def jsonToCard(json: JsValue): Card = {
    val value = Value.valueOf((json \ "value").as[String])
    val color = Color.valueOf((json \ "color").as[String])
    Card(value, color)
  }

  private def jsonToPlayer(json: JsValue): Player = {
    val name = (json \ "name").as[String]
    val pType = (json \ "type").as[String]
    val points = (json \ "points").as[Int]
    val tricks = (json \ "tricks").as[Int]
    val bids = (json \ "bids").as[Int]
    val roundPoints = (json \ "roundPoints").as[Int]
    val roundBids = (json \ "roundBids").as[Int]
    val roundTricks = (json \ "roundTricks").as[Int]
    
    val handCards = (json \ "hand").as[List[JsValue]].map(jsonToCard)
    
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
    pw.write(Json.prettyPrint(gameToJson(game, currentRound, trumpCard, dealerIndex, firstPlayerIdx)))
    pw.close()
  }

  private def gameToJson(game: Game, currentRound: Int, trumpCard: Option[Card], dealerIndex: Int, firstPlayerIdx: Int): JsObject = {
    Json.obj(
      "rounds" -> game.rounds,
      "currentRound" -> currentRound,
      "dealerIndex" -> dealerIndex,
      "firstPlayerIdx" -> firstPlayerIdx,
      "trumpCard" -> trumpCard.map(cardToJson),
      "currentTrick" -> game.currentTrick.map(cardToJson),
      "players" -> game.players.map(playerToJson)
    )
  }

  private def playerToJson(player: Player): JsObject = {
    Json.obj(
      "name" -> player.name,
      "type" -> (if (player.isInstanceOf[AI]) "AI" else "Human"),
      "points" -> player.points,
      "tricks" -> player.tricks,
      "bids" -> player.bids,
      "roundPoints" -> player.roundPoints,
      "roundBids" -> player.roundBids,
      "roundTricks" -> player.roundTricks,
      "hand" -> player.hand.cards.map(cardToJson)
    )
  }

  private def cardToJson(card: Card): JsObject = {
    Json.obj(
      "value" -> card.value.toString,
      "color" -> card.color.toString
    )
  }
}
