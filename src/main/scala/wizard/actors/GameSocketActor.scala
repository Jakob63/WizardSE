package wizard.actors

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import util.QueueInput
import wizard.aView.TextUI
import wizard.controller.controllerBaseImpl.{BaseGameLogic, BasePlayerLogic, BaseRoundLogic}
import wizard.model.player.Player
import wizard.model.cards.{Card, Value}
import play.api.libs.json._
import wizard.actors.Events._

class GameSocketActor(
  gameLogic: BaseGameLogic,
  out: ActorRef,
  playerLogic: BasePlayerLogic,
  roundLogic: BaseRoundLogic,
  input: QueueInput
) extends Actor {

  private sealed trait ClientCommand { def name: String }
  private case object StartCmd extends ClientCommand { val name = "start" }
  private case class ChoiceCmd(value: String) extends ClientCommand { val name = "choice" }
  private case class PlayersCmd(value: String) extends ClientCommand { val name = "players" }
  private case class NameCmd(value: String) extends ClientCommand { val name = "name" }
  private case class BidCmd(value: String) extends ClientCommand { val name = "bid" }
  private case class CardCmd(value: String) extends ClientCommand { val name = "card" }

  private case class Outgoing[E](event: String, data: E)

  override def preStart(): Unit = {
    playerLogic.userInput = input
    TextUI.userInput = input
    TextUI.init(gameLogic)
    gameLogic.add(TextUI)
    playerLogic.add(TextUI)
    roundLogic.add(TextUI)

    gameLogic.setGameSocketActor(self)
    playerLogic.setGameSocketActor(self)
    roundLogic.setGameSocketActor(self)
  }

  // {"name": <cmd>, "value": <string?>}
  private given Reads[ClientCommand] = Reads { js =>
    for {
      name <- (js \ "name").validate[String]
    } yield {
      val v = (js \ "value").asOpt[String].map(_.trim)
      name match {
        case "start"   => StartCmd
        case "choice"  => ChoiceCmd(v.getOrElse(""))
        case "players" => PlayersCmd(v.getOrElse(""))
        case "name"    => NameCmd(v.getOrElse(""))
        case "bid"     => BidCmd(v.getOrElse(""))
        case "card"    => CardCmd(v.getOrElse(""))
        case other      => new ClientCommand { val name = other }
      }
    }
  }
  private given outgoingWrites[E: Writes]: OWrites[Outgoing[E]] = Json.writes[Outgoing[E]]

  private def push(event: String, data: JsValue = Json.obj()): Unit = {
    val payload = Outgoing(event, data)
    out ! Json.stringify(Json.toJson(payload))
  }

  private def enqueue(cmd: String, rawValue: String): Unit = {
    val v = rawValue.trim
    if (v.isEmpty) {
      push(rejected(cmd), Json.obj("reason" -> "empty"))
    } else if (!input.offer(v)) {
      push(rejected(cmd), Json.obj("reason" -> "queue full"))
    } else {
      push(accepted(cmd), Json.obj("value" -> v))
    }
  }

  private def handleCommand(cmd: ClientCommand): Unit = cmd match {
    case StartCmd =>
      push(Info, Json.obj("message" -> "Starting game"))
      gameLogic.startGame()
    case ChoiceCmd(v)  => enqueue("choice", v)
    case PlayersCmd(v) => enqueue("players", v)
    case NameCmd(v)    => enqueue("name", v)
    case BidCmd(v)     => enqueue("bid", v)
    case CardCmd(v)    => enqueue("card", v)
    case other         => push(ErrorUnknownCommand, Json.obj("name" -> other.name))
  }

  private def parsePlaintext(txt: String): Option[ClientCommand] = txt match {
    case "start"                       => Some(StartCmd)
    case s if s.startsWith("choice:")  => Some(ChoiceCmd(s.stripPrefix("choice:").trim))
    case s if s.startsWith("players:") => Some(PlayersCmd(s.stripPrefix("players:").trim))
    case s if s.startsWith("name:")    => Some(NameCmd(s.stripPrefix("name:").trim))
    case s if s.startsWith("bid:")     => Some(BidCmd(s.stripPrefix("bid:").trim))
    case s if s.startsWith("card:")    => Some(CardCmd(s.stripPrefix("card:").trim))
    case _                              => None
  }

  private def parseCommand(txt: String): Either[(String, JsValue), ClientCommand] = {
    if (txt.startsWith("{")) {
      scala.util.Try(Json.parse(txt))
        .toEither
        .left
        .map(_ => ErrorInvalidPayload -> Json.obj("raw" -> txt))
        .flatMap { js =>
          js.validate[ClientCommand] match {
            case JsSuccess(cmd, _) =>
              cmd match {
                case StartCmd | _: ChoiceCmd | _: PlayersCmd | _: NameCmd | _: BidCmd | _: CardCmd => Right(cmd)
                case other => Left(ErrorUnknownCommand -> Json.obj("name" -> other.name))
              }
            case JsError(_) => Left(ErrorInvalidPayload -> Json.obj("raw" -> txt))
          }
        }
    } else {
      parsePlaintext(txt).toRight(ErrorUnknownCommand -> Json.obj("name" -> txt.takeWhile(_ != ':')))
    }
  }

  override def receive: Receive = {
    case msg: String =>
      val txt = msg.trim
      parseCommand(txt) match {
        case Right(cmd) => handleCommand(cmd)
        case Left((event, data)) => push(event, data)
      }

    case "gameStarted" =>
      push(GameStarted)

    case s: String if s.startsWith("roundStarted:") =>
      push(RoundStarted, Json.obj("round" -> s.stripPrefix("roundStarted:")))

    case s: String if s.startsWith("trumpCard:") =>
      val payload = s.stripPrefix("trumpCard:")
      val parts = payload.split(":").toList
      parts match {
        case color :: value :: Nil => push(TrumpCard, Json.obj("color" -> color, "value" -> value))
        case _ => push(TrumpCard, Json.obj("raw" -> payload))
      }

    case "playersHands" =>
      push(PlayersHandsUpdated)

    case s: String if s.startsWith("trickCards:") =>
      push(TrickCardPlayed, Json.obj("value" -> s.stripPrefix("trickCards:")))

    case s: String if s.startsWith("roundPlayed:") =>
      push(RoundFinished, Json.obj("round" -> s.stripPrefix("roundPlayed:")))

    case s: String if s.startsWith("player:") =>
      push(PlayerEvent, Json.obj("message" -> s))

    case ("player_names", playerNumber: Int, current: Int, playersList: List[Player]) =>
      push(PlayerNamesPrompt, Json.obj("current" -> (current + 1), "total" -> playerNumber))

    case ("player_hand", player: Player) =>
      val cards = player.hand.cards.map { c => Json.obj("color" -> c.color.toString, "value" -> c.value.toString) }
      push(PlayerHand, Json.obj("player" -> player.name, "cards" -> JsArray(cards)))

    case ("play_card", player: Player, card: Card) =>
      push(PlayerPlayCard, Json.obj(
        "player" -> player.name,
        "color" -> card.color.toString,
        "value" -> card.value.toString
      ))

    case _ =>
      ()
  }
}

object GameSocketActor {
  def props(
    gameLogic: BaseGameLogic,
    out: ActorRef,
    playerLogic: BasePlayerLogic,
    roundLogic: BaseRoundLogic,
    input: QueueInput
  ): Props = Props(new GameSocketActor(gameLogic, out, playerLogic, roundLogic, input))
}
